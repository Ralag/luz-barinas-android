package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.PacAlertTargetResolver
import com.example.data.PacSchedulePrefs
import com.example.data.model.PacAlertPrefs
import com.example.data.model.PacScheduleData
import com.example.notification.PacAlarmScheduler
import com.example.notification.PacAlertNotifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/** Periodic best-effort fallback; exact alarms and this worker share delivery deduplication. */
class PredictiveNotificationWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            PacSchedulePrefs.loadSchedule(applicationContext)
            PacAlarmScheduler.scheduleNextAlarmAndWait(applicationContext)
            val settings = PacAlertPrefs.getSettings(applicationContext)
            if (!settings.isNotificationEnabled && !settings.isAlarmEnabled) return@withContext Result.success()
            val target = PacAlertTargetResolver.resolve(applicationContext) ?: return@withContext Result.success()
            val now = System.currentTimeMillis()
            val window = PacScheduleData.findScheduledWindowForBlock(target.block, now, includeActive = false)
                ?: return@withContext Result.success()
            // Use the window's actual start date in Caracas, including tomorrow across midnight.
            if (window.startMillis - now in 1..settings.advanceMinutes.coerceIn(1, 180) * 60_000L) {
                PacAlertNotifier.showPreAlert(applicationContext, target, window, now)
            }
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "predictive_notification_work"
        fun enqueue(context: Context) {
            val work = PeriodicWorkRequestBuilder<PredictiveNotificationWorker>(30, TimeUnit.MINUTES, 15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, work)
        }
    }
}
