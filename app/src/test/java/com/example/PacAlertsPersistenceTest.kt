package com.example

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PacAlertTarget
import com.example.data.PacAlertTargetResolver
import com.example.data.PacSchedulePrefs
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SectorEntity
import com.example.data.model.PacAlertPrefs
import com.example.data.model.PacAlertSettings
import com.example.data.model.PacScheduleData
import com.example.data.model.PacSlot
import com.example.notification.PacAlarmReceiver
import com.example.notification.PacAlarmScheduler
import com.example.notification.PacAlertNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PacAlertsPersistenceTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private val now = GregorianCalendar(TimeZone.getTimeZone("America/Caracas")).apply {
        clear()
        set(2026, Calendar.OCTOBER, 5, 0, 0, 0)
    }.timeInMillis

    @Before

    fun setUp() = runBlocking(Dispatchers.IO) {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getInstance(context)
        database.clearAllTables()
        for (name in listOf("luz_barinas_user_prefs", "pac_schedule_prefs", "pac_alert_settings", "pac_alert_deliveries")) {
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
        }
        PacAlarmScheduler.cancelAlarms(context)
        PacScheduleData.applyDoubleTurnPreset()
    }

    @After

    fun tearDown() {
        PacAlarmScheduler.cancelAlarms(context)
        PacScheduleData.applyDoubleTurnPreset()
    }

    private suspend fun insertSector(id: String, block: String) {
        database.sectorDao().insertOrUpdateSector(SectorEntity(id, id, "circuit", "NORMAL", 118f, 0, 0, now, block, ""))
    }

    private fun selectProfile(profile: String, sectorId: String?) {
        context.getSharedPreferences("luz_barinas_user_prefs", Context.MODE_PRIVATE).edit()
            .putString("active_profile", profile)
            .putString("profile_${profile}_sector_id", sectorId)
            .putString("saved_sector_id", sectorId)
            .putString("saved_sector_block", "B") // Original migration bug: the B in BLOQUE.
            .putString("selected_sector_block", "B")
            .apply()
    }

    @Test

    fun repairsLegacyCachedBlockFromTheSelectedRoomSector() = runBlocking(Dispatchers.IO) {
        insertSector("sector-c", "Bloque C")
        selectProfile("home", "sector-c")
        val result = PacAlertTargetResolver.resolve(context, database)!!
        assertEquals("C", result.block)
        val prefs = context.getSharedPreferences("luz_barinas_user_prefs", Context.MODE_PRIVATE)
        for (key in listOf("saved_block", "saved_sector_block", "selected_sector_block", "profile_home_block")) {
            assertEquals("C", prefs.getString(key, null))
        }
        insertSector("sector-c", "unassigned")
        assertNull(PacAlertTargetResolver.resolve(context, database))
    }

    @Test

    fun coldStartLoadsPersistedScheduleBeforeArmingActualAlarmTimes() = runBlocking(Dispatchers.IO) {
        insertSector("sector-c", "Bloque C")
        selectProfile("home", "sector-c")
        PacScheduleData.activeSlots.clear()
        PacScheduleData.activeSlots.add(PacSlot(0, "05–06", 5, 6))
        PacScheduleData.activeMatrix = arrayOf(Array(7) { "C" })
        PacSchedulePrefs.saveSchedule(context)
        PacScheduleData.applyDoubleTurnPreset() // Fresh process memory differs from saved configuration.
        PacScheduleData.scheduleVersion = 0L // A new process has no applied in-memory revision.
        PacAlarmScheduler.scheduleNextAlarmAndWait(context, now)
        val alarms = shadowOf(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).scheduledAlarms
        assertEquals(setOf(now + 5 * 3_600_000L - 600_000, now + 5 * 3_600_000L), alarms.map { it.triggerAtTime }.toSet())
        assertTrue(alarms.all { shadowOf(it.operation).savedIntent.getStringExtra(PacAlarmReceiver.EXTRA_BLOCK) == "C" })
    }

    @Test
    fun olderPersistedScheduleCannotOverrideANewerInMemorySnapshot() {
        PacScheduleData.activeSlots.clear()
        PacScheduleData.activeSlots.add(PacSlot(0, "05–06", 5, 6))
        PacScheduleData.activeMatrix = arrayOf(Array(7) { "C" })
        PacSchedulePrefs.saveSchedule(context)
        val newer = PacScheduleData.getScheduleSnapshot().copy(slots = listOf(PacSlot(0, "09–10", 9, 10)))
        PacScheduleData.applySnapshot(newer, PacScheduleData.scheduleVersion + 1)
        assertTrue(PacSchedulePrefs.loadSchedule(context))
        assertEquals(9, PacScheduleData.getSlotsSnapshot().first().startHour)
    }

    @Test

    fun changingProfileReplacesOldAlarmsAndRejectsAnAlreadyQueuedOldIntent() = runBlocking(Dispatchers.IO) {
        insertSector("home-a", "Bloque A")
        insertSector("work-c", "Bloque C")
        selectProfile("home", "home-a")
        PacScheduleData.activeSlots.clear()
        PacScheduleData.activeSlots.addAll(listOf(PacSlot(0, "05–06", 5, 6), PacSlot(1, "09–10", 9, 10)))
        PacScheduleData.activeMatrix = arrayOf(Array(7) { "A" }, Array(7) { "C" })
        PacSchedulePrefs.saveSchedule(context)
        PacAlarmScheduler.scheduleNextAlarmAndWait(context, now)
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val oldIntent = shadowOf(shadowOf(manager).scheduledAlarms.first().operation).savedIntent
        selectProfile("work", "work-c")
        PacAlarmScheduler.scheduleNextAlarmAndWait(context, now)
        val alarms = shadowOf(manager).scheduledAlarms
        assertEquals(2, alarms.size)
        assertTrue(alarms.all {
            val intent = shadowOf(it.operation).savedIntent
            intent.getStringExtra(PacAlarmReceiver.EXTRA_PROFILE) == "work" &&
                intent.getStringExtra(PacAlarmReceiver.EXTRA_SECTOR_ID) == "work-c" &&
                intent.getStringExtra(PacAlarmReceiver.EXTRA_BLOCK) == "C"
        })
        assertFalse(PacAlarmReceiver().handleScheduledAlert(context, oldIntent, now + 5 * 3_600_000L - 600_000))
        selectProfile("work", null)
        PacAlarmScheduler.scheduleNextAlarmAndWait(context, now)
        assertTrue(shadowOf(manager).scheduledAlarms.isEmpty())
    }

    @Test

    fun disabledSettingsCancelPreviouslyScheduledAlarmsAndUnscheduledDArmsNothing() = runBlocking(Dispatchers.IO) {
        insertSector("sector-a", "Bloque A")
        selectProfile("home", "sector-a")
        PacAlarmScheduler.scheduleNextAlarmAndWait(context, now)
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        assertTrue(shadowOf(manager).scheduledAlarms.isNotEmpty())
        PacAlertPrefs.saveSettings(context, PacAlertSettings(isNotificationEnabled = false, isAlarmEnabled = false, isRestoreAlarmEnabled = false))
        PacAlarmScheduler.scheduleNextAlarmAndWait(context, now)
        assertTrue(shadowOf(manager).scheduledAlarms.isEmpty())
        PacAlertPrefs.saveSettings(context, PacAlertSettings())
        insertSector("sector-d", "Bloque D")
        selectProfile("home", "sector-d")
        PacAlarmScheduler.scheduleNextAlarmAndWait(context, now)
        assertTrue(shadowOf(manager).scheduledAlarms.isEmpty())
    }

    @Test

    fun exactAndPeriodicDeliveryShareADurableKeyButDifferentProfilesAreIndependent() {
        val home = PacAlertTarget("home", "same-sector", "Casa", "A")
        assertTrue(PacAlertNotifier.claim(context, home, now, "pre"))
        assertFalse(PacAlertNotifier.claim(context, home, now, "pre"))
        assertTrue(PacAlertNotifier.claim(context, home, now, "restore"))
        assertTrue(PacAlertNotifier.claim(context, home.copy(profile = "work"), now, "pre"))
    }

    @Test

    fun planHonorsIndependentAlertSwitchesAndSchedulesFutureWhileCurrentCutIsActive() {
        val activeAtEight = now + 8 * 3_600_000L
        val silent = PacAlertSettings(isNotificationEnabled = false, isAlarmEnabled = false, isRestoreAlarmEnabled = false)
        assertTrue(PacAlarmScheduler.buildPlan("A", silent, activeAtEight).isEmpty())
        val restoreOnly = PacAlarmScheduler.buildPlan("A", silent.copy(isRestoreAlarmEnabled = true), activeAtEight)
        assertEquals(listOf(PacAlarmReceiver.ACTION_PAC_RESTORE_ALERT), restoreOnly.map { it.action })
        assertEquals(now + 11 * 3_600_000L, restoreOnly.single().triggerAtMillis)
        val alarmOnly = PacAlarmScheduler.buildPlan("A", silent.copy(isAlarmEnabled = true), activeAtEight)
        assertEquals(listOf(PacAlarmReceiver.ACTION_PAC_PRE_ALERT), alarmOnly.map { it.action })
        assertEquals(now + 19 * 3_600_000L - 600_000L, alarmOnly.single().triggerAtMillis)
    }
}
