package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.model.PacScheduleData
import com.example.engine.OutagePredictionEngine
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that checks for upcoming PAC outages and
 * sends predictive notifications 15-30 minutes in advance.
 * 
 * Runs every 30 minutes to check if the user's sector has a
 * scheduled outage coming up soon.
 */
class PredictiveNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userSectorId = prefs.getString("selected_sector_id", null) ?: return@withContext Result.success()
            val userSectorName = prefs.getString("selected_sector_name", "Tu sector") ?: "Tu sector"
            val userBlock = prefs.getString("selected_sector_block", "A") ?: "A"

            val cal = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas"))
            val currentHour = cal.get(Calendar.HOUR_OF_DAY)
            val currentMinute = cal.get(Calendar.MINUTE)
            val currentDayIdx = PacScheduleData.getDayIndex(cal.get(Calendar.DAY_OF_WEEK))

            val alertSettings = com.example.data.model.PacAlertPrefs.getSettings(applicationContext)
            if (!alertSettings.isNotificationEnabled) return@withContext Result.success()
            val targetAdvance = alertSettings.advanceMinutes

            // Ensure exact AlarmManager is armed
            com.example.notification.PacAlarmScheduler.scheduleNextAlarm(applicationContext)

            // Check each upcoming slot
            for (slot in PacScheduleData.SLOTS) {
                val minutesUntilSlot = calculateMinutesUntil(currentHour, currentMinute, slot.startHour)

                // Match user's configured advance notice window (within +/- 10 min window of check)
                val minThreshold = (targetAdvance - 5).coerceAtLeast(1)
                val maxThreshold = targetAdvance + 15
                if (minutesUntilSlot in minThreshold..maxThreshold) {
                    val slotIdx = PacScheduleData.SLOTS.indexOf(slot)
                    if (slotIdx >= 0) {
                        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                        val blockForSlot = PacScheduleData.getBlockForDate(slotIdx, currentDayIdx, dayOfMonth)

                        if (blockForSlot.equals(userBlock, ignoreCase = true)) {
                            NotificationHelper.showPredictiveAlert(
                                context = applicationContext,
                                sectorName = userSectorName,
                                sectorId = userSectorId,
                                minutesUntil = minutesUntilSlot,
                                slotLabel = slot.timeLabel
                            )
                            break
                        }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.success() // Don't retry on failure, just skip this cycle
        }
    }

    private fun calculateMinutesUntil(currentHour: Int, currentMinute: Int, targetHour: Int): Int {
        val currentTotalMinutes = currentHour * 60 + currentMinute
        val targetTotalMinutes = targetHour * 60
        val diff = targetTotalMinutes - currentTotalMinutes
        return if (diff >= 0) diff else diff + 24 * 60 // Handle day wrap
    }

    companion object {
        const val WORK_NAME = "predictive_notification_work"

        fun enqueue(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<PredictiveNotificationWorker>(
                30, TimeUnit.MINUTES,
                15, TimeUnit.MINUTES // Flex interval
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
