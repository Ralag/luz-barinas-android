package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.PacAlertPrefs
import com.example.data.model.PacScheduleData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PacAlarmScheduler {
    private const val TAG = "PacAlarmScheduler"
    private const val REQUEST_CODE_PRE_ALERT = 5001
    private const val REQUEST_CODE_RESTORE_ALERT = 5002

    fun scheduleNextAlarm(context: Context) {
        val prefs = context.getSharedPreferences("luz_barinas_user_prefs", Context.MODE_PRIVATE)
        val rawBlock = prefs.getString("saved_sector_block", null)
            ?: prefs.getString("saved_block", null)
            ?: prefs.getString("selected_sector_block", "A")
            ?: "A"
        val cleanMatch = Regex("[ABCD]").find(rawBlock.uppercase())
        val userBlock = cleanMatch?.value ?: "A"
        val userSectorName = prefs.getString("saved_address", null)
            ?: prefs.getString("selected_sector_name", "Mi Sector")
            ?: "Mi Sector"

        val settings = PacAlertPrefs.getSettings(context)
        if (!settings.isNotificationEnabled && !settings.isAlarmEnabled && !settings.isRestoreAlarmEnabled) {
            cancelAlarms(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        // Find the next scheduled outage window for this user's block
        val nextWindow = PacScheduleData.findNextWindowForBlock(userBlock, now)

        val advanceMs = settings.advanceMinutes * 60 * 1000L
        val preAlertTime = nextWindow.startMillis - advanceMs

        if (preAlertTime > now) {
            val preIntent = Intent(context, PacAlarmReceiver::class.java).apply {
                action = PacAlarmReceiver.ACTION_PAC_PRE_ALERT
                putExtra(PacAlarmReceiver.EXTRA_BLOCK, userBlock)
                putExtra(PacAlarmReceiver.EXTRA_SECTOR_NAME, userSectorName)
                putExtra(PacAlarmReceiver.EXTRA_MINUTES_UNTIL, settings.advanceMinutes)
                putExtra(PacAlarmReceiver.EXTRA_TIME_LABEL, nextWindow.timeLabel)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_PRE_ALERT,
                preIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        preAlertTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        preAlertTime,
                        pendingIntent
                    )
                }
                val fmt = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(Date(preAlertTime))
                Log.i(TAG, "Scheduled pre-outage alarm for $fmt (${settings.advanceMinutes} min before ${nextWindow.timeLabel})")
            } catch (e: SecurityException) {
                Log.w(TAG, "SCHEDULE_EXACT_ALARM permission not granted: ${e.message}")
            }
        }

        // Schedule restore alert if enabled
        if (settings.isRestoreAlarmEnabled && nextWindow.endMillis > now) {
            val restoreIntent = Intent(context, PacAlarmReceiver::class.java).apply {
                action = PacAlarmReceiver.ACTION_PAC_RESTORE_ALERT
                putExtra(PacAlarmReceiver.EXTRA_SECTOR_NAME, userSectorName)
            }
            val restorePendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_RESTORE_ALERT,
                restoreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextWindow.endMillis,
                        restorePendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        nextWindow.endMillis,
                        restorePendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Cannot schedule restore alarm: ${e.message}")
            }
        }
    }

    fun cancelAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val preIntent = Intent(context, PacAlarmReceiver::class.java).apply {
            action = PacAlarmReceiver.ACTION_PAC_PRE_ALERT
        }
        val p1 = PendingIntent.getBroadcast(
            context, REQUEST_CODE_PRE_ALERT, preIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        p1?.let { alarmManager.cancel(it) }

        val restoreIntent = Intent(context, PacAlarmReceiver::class.java).apply {
            action = PacAlarmReceiver.ACTION_PAC_RESTORE_ALERT
        }
        val p2 = PendingIntent.getBroadcast(
            context, REQUEST_CODE_RESTORE_ALERT, restoreIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        p2?.let { alarmManager.cancel(it) }
        Log.i(TAG, "All PAC alarms cancelled")
    }
}
