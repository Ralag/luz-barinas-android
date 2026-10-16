package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.data.PacAlertTarget
import com.example.data.PacAlertTargetResolver
import com.example.data.PacSchedulePrefs
import com.example.data.model.PacAlertPrefs
import com.example.data.model.PacAlertSettings
import com.example.data.model.PacScheduleData
import com.example.data.model.PacScheduleWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class PacScheduledAlert(val action: String, val triggerAtMillis: Long, val window: PacScheduleWindow)

object PacAlarmScheduler {
    private const val TAG = "PacAlarmScheduler"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    private var generation = 0L
    private val requestCodes = mapOf(
        PacAlarmReceiver.ACTION_PAC_PRE_ALERT to 5001,
        PacAlarmReceiver.ACTION_PAC_RESTORE_ALERT to 5002,
        PacAlarmReceiver.ACTION_PAC_VALIDATION_ALERT to 5003
    )

    fun scheduleNextAlarm(context: Context) {
        val app = context.applicationContext
        val ticket = beginReschedule(app)
        scope.launch { schedule(app, ticket, System.currentTimeMillis()) }
    }

    suspend fun scheduleNextAlarmAndWait(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        val app = context.applicationContext
        val ticket = beginReschedule(app)
        schedule(app, ticket, nowMillis)
    }

    private fun beginReschedule(context: Context): Long = synchronized(lock) {
        generation++
        cancelPendingAlarms(context)
        generation
    }

    private suspend fun schedule(context: Context, ticket: Long, nowMillis: Long): Unit = withContext(Dispatchers.IO) {
        try {
            // Receivers and workers can be the first entry into a fresh Android process.
            PacSchedulePrefs.loadSchedule(context)
            val settings = PacAlertPrefs.getSettings(context)
            val target = PacAlertTargetResolver.resolve(context)
            if (target == null) return@withContext
            val alerts = buildPlan(target.block, settings, nowMillis)
            synchronized(lock) {
                if (ticket != generation) return@synchronized
                if (!settings.isAlarmEnabled) PacAlarmPlayer.stopAlarm(context)
                val notifications = NotificationManagerCompat.from(context)
                if (!settings.isNotificationEnabled) notifications.cancel(NotificationHelper.NOTIFICATION_ID_ALERT)
                if (!settings.isNotificationEnabled && !settings.isAlarmEnabled) notifications.cancel(PacAlarmReceiver.NOTIFICATION_ID_PAC_ALARM)
                if (!settings.isRestoreAlarmEnabled) notifications.cancel(NotificationHelper.NOTIFICATION_ID_RESTORE)
                for (alert in alerts) arm(context, target, settings, alert)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not schedule PAC alerts", e)
        }
    }

    internal fun buildPlan(block: String, settings: PacAlertSettings, nowMillis: Long): List<PacScheduledAlert> {
        val future = PacScheduleData.findScheduledWindowForBlock(block, nowMillis, includeActive = false)
        val currentOrNext = PacScheduleData.findScheduledWindowForBlock(block, nowMillis)
        return buildList {
            if (future != null) {
                val preTime = future.startMillis - settings.advanceMinutes.coerceIn(1, 180) * 60_000L
                if ((settings.isNotificationEnabled || settings.isAlarmEnabled) && preTime > nowMillis) {
                    add(PacScheduledAlert(PacAlarmReceiver.ACTION_PAC_PRE_ALERT, preTime, future))
                }
                if (settings.isNotificationEnabled) {
                    add(PacScheduledAlert(PacAlarmReceiver.ACTION_PAC_VALIDATION_ALERT, future.startMillis, future))
                }
            }
            if (settings.isRestoreAlarmEnabled && currentOrNext != null) {
                add(PacScheduledAlert(PacAlarmReceiver.ACTION_PAC_RESTORE_ALERT, currentOrNext.endMillis, currentOrNext))
            }
        }
    }

    private fun arm(context: Context, target: PacAlertTarget, settings: PacAlertSettings, alert: PacScheduledAlert) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PacAlarmReceiver::class.java).apply {
            action = alert.action
            putExtra(PacAlarmReceiver.EXTRA_PROFILE, target.profile)
            putExtra(PacAlarmReceiver.EXTRA_BLOCK, target.block)
            putExtra(PacAlarmReceiver.EXTRA_SECTOR_ID, target.sectorId)
            putExtra(PacAlarmReceiver.EXTRA_SECTOR_NAME, target.sectorName)
            putExtra(PacAlarmReceiver.EXTRA_MINUTES_UNTIL, settings.advanceMinutes)
            putExtra(PacAlarmReceiver.EXTRA_TIME_LABEL, alert.window.timeLabel)
            putExtra(PacAlarmReceiver.EXTRA_WINDOW_START, alert.window.startMillis)
            putExtra(PacAlarmReceiver.EXTRA_WINDOW_END, alert.window.endMillis)
        }
        val pending = PendingIntent.getBroadcast(
            context, requestCodes.getValue(alert.action), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alert.triggerAtMillis, pending)
            } else {
                // Preserve a best-effort alert when exact-alarm access has not been granted.
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alert.triggerAtMillis, pending)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Alarm permission changed while scheduling", e)
        }
    }

    fun cancelAlarms(context: Context) {
        synchronized(lock) {
            generation++
            cancelPendingAlarms(context)
        }
        PacAlarmPlayer.stopAlarm(context)
        val notifications = NotificationManagerCompat.from(context)
        listOf(PacAlarmReceiver.NOTIFICATION_ID_PAC_ALARM, NotificationHelper.NOTIFICATION_ID_ALERT,
            NotificationHelper.NOTIFICATION_ID_PREDICTIVE, NotificationHelper.NOTIFICATION_ID_RESTORE)
            .forEach { notifications.cancel(it) }
    }

    private fun cancelPendingAlarms(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for ((action, requestCode) in requestCodes) {
            val intent = Intent(context, PacAlarmReceiver::class.java).setAction(action)
            val pending = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            if (pending != null) {
                manager.cancel(pending)
                pending.cancel()
            }
        }
    }
}
