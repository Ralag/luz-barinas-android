package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.data.PacAlertTargetResolver
import com.example.data.PacSchedulePrefs
import com.example.data.model.PacBlock
import com.example.data.model.PacScheduleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PacAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == ACTION_STOP_ALARM) {
            PacAlarmPlayer.stopAlarm(context)
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_PAC_ALARM)
            NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIFICATION_ID_RESTORE)
            return
        }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (action != Intent.ACTION_BOOT_COMPLETED) handleScheduledAlert(context, intent)
                PacAlarmScheduler.scheduleNextAlarmAndWait(context)
            } catch (e: Exception) {
                Log.e("PacAlarmReceiver", "Could not handle PAC alert", e)
            } finally {
                pending.finish()
            }
        }
    }

    internal suspend fun handleScheduledAlert(context: Context, intent: Intent, nowMillis: Long = System.currentTimeMillis()): Boolean {
        PacSchedulePrefs.loadSchedule(context)
        val target = PacAlertTargetResolver.resolve(context) ?: return false
        // Reject already queued intents from a previous profile, sector, block or schedule.
        if (intent.getStringExtra(EXTRA_PROFILE) != target.profile ||
            intent.getStringExtra(EXTRA_SECTOR_ID) != target.sectorId ||
            PacBlock.normalize(intent.getStringExtra(EXTRA_BLOCK)) != target.block) return false
        val start = intent.getLongExtra(EXTRA_WINDOW_START, Long.MIN_VALUE)
        if (start == Long.MIN_VALUE) return false
        val window = PacScheduleData.findScheduledWindowForBlock(target.block, start) ?: return false
        if (window.startMillis != start || window.endMillis != intent.getLongExtra(EXTRA_WINDOW_END, Long.MIN_VALUE)) return false
        return when (intent.action) {
            ACTION_PAC_PRE_ALERT -> PacAlertNotifier.showPreAlert(context, target, window, nowMillis)
            ACTION_PAC_VALIDATION_ALERT -> if (nowMillis >= window.startMillis && nowMillis < window.endMillis)
                PacAlertNotifier.showValidationAlert(context, target, window) else false
            ACTION_PAC_RESTORE_ALERT -> if (nowMillis >= window.endMillis && nowMillis < window.endMillis + 30 * 60_000L)
                PacAlertNotifier.showRestoreAlert(context, target, window) else false
            else -> false
        }
    }

    companion object {
        const val ACTION_PAC_PRE_ALERT = "com.example.pacbarinas.ACTION_PAC_PRE_ALERT"
        const val ACTION_PAC_RESTORE_ALERT = "com.example.pacbarinas.ACTION_PAC_RESTORE_ALERT"
        const val ACTION_PAC_VALIDATION_ALERT = "com.example.pacbarinas.ACTION_PAC_VALIDATION_ALERT"
        const val ACTION_STOP_ALARM = "com.example.pacbarinas.ACTION_STOP_ALARM"
        const val EXTRA_PROFILE = "extra_profile"
        const val EXTRA_BLOCK = "extra_block"
        const val EXTRA_SECTOR_ID = "extra_sector_id"
        const val EXTRA_SECTOR_NAME = "extra_sector_name"
        const val EXTRA_MINUTES_UNTIL = "extra_minutes_until"
        const val EXTRA_TIME_LABEL = "extra_time_label"
        const val EXTRA_WINDOW_START = "extra_window_start"
        const val EXTRA_WINDOW_END = "extra_window_end"
        const val NOTIFICATION_ID_PAC_ALARM = 2001
    }
}
