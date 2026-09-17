package com.example.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.PacAlertPrefs

class PacAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.i("PacAlarmReceiver", "Received action: $action")

        when (action) {
            ACTION_PAC_PRE_ALERT -> handlePreOutageAlert(context, intent)
            ACTION_PAC_RESTORE_ALERT -> handleRestoreAlert(context, intent)
            ACTION_STOP_ALARM -> handleStopAlarm(context)
            Intent.ACTION_BOOT_COMPLETED -> handleBoot(context)
        }
    }

    private fun handlePreOutageAlert(context: Context, intent: Intent) {
        val block = intent.getStringExtra(EXTRA_BLOCK) ?: "C"
        val sectorName = intent.getStringExtra(EXTRA_SECTOR_NAME) ?: "Tu sector"
        val minutesUntil = intent.getIntExtra(EXTRA_MINUTES_UNTIL, 10)
        val timeLabel = intent.getStringExtra(EXTRA_TIME_LABEL) ?: ""

        val settings = PacAlertPrefs.getSettings(context)
        if (!settings.isNotificationEnabled) return

        if (settings.isAlarmEnabled) {
            PacAlarmPlayer.startAlarm(context, settings.isVibrationEnabled)
        }

        showAlarmNotification(context, block, sectorName, minutesUntil, timeLabel, isAlarm = settings.isAlarmEnabled)
        PacAlarmScheduler.scheduleNextAlarm(context)
    }

    private fun handleRestoreAlert(context: Context, intent: Intent) {
        val sectorName = intent.getStringExtra(EXTRA_SECTOR_NAME) ?: "Tu sector"
        val settings = PacAlertPrefs.getSettings(context)
        if (!settings.isRestoreAlarmEnabled) return

        PacAlarmPlayer.startAlarm(context, settings.isVibrationEnabled)
        showRestoreNotification(context, sectorName)
        PacAlarmScheduler.scheduleNextAlarm(context)
    }

    private fun handleStopAlarm(context: Context) {
        PacAlarmPlayer.stopAlarm(context)
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_PAC_ALARM)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun handleBoot(context: Context) {
        PacAlarmScheduler.scheduleNextAlarm(context)
    }

    private fun showAlarmNotification(
        context: Context,
        block: String,
        sectorName: String,
        minutesUntil: Int,
        timeLabel: String,
        isAlarm: Boolean
    ) {
        NotificationHelper.createNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, PacAlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 101, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (minutesUntil == 0) {
            "⚡ ¡Inicia Corte PAC ahora!"
        } else {
            "⚡ Corte PAC en $minutesUntil min (Bloque $block)"
        }

        val body = "$sectorName ($timeLabel). ¡Carga tus dispositivos y desconecta equipos sensibles!"

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bolt)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText("$body\n\n🔋 Asegura carga en celulares y linternas\n🔌 Desconecta electrodomésticos para protegerlos")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        if (isAlarm) {
            builder.addAction(R.drawable.ic_power_off, "🔕 Detener Alarma", stopPendingIntent)
            builder.setOngoing(true)
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PAC_ALARM, builder.build())
        } catch (e: SecurityException) {
            Log.w("PacAlarmReceiver", "Permission POST_NOTIFICATIONS missing")
        }
    }

    private fun showRestoreNotification(context: Context, sectorName: String) {
        NotificationHelper.createNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, PacAlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 102, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_power_on)
            .setContentTitle("💡 ¡Luz restablecida según cronograma!")
            .setContentText("El turno PAC para $sectorName ha finalizado.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_power_on, "🔕 Detener Alarma", stopPendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PAC_ALARM, builder.build())
        } catch (e: SecurityException) {
            Log.w("PacAlarmReceiver", "Permission POST_NOTIFICATIONS missing")
        }
    }

    companion object {
        const val ACTION_PAC_PRE_ALERT = "com.example.pacbarinas.ACTION_PAC_PRE_ALERT"
        const val ACTION_PAC_RESTORE_ALERT = "com.example.pacbarinas.ACTION_PAC_RESTORE_ALERT"
        const val ACTION_STOP_ALARM = "com.example.pacbarinas.ACTION_STOP_ALARM"

        const val EXTRA_BLOCK = "extra_block"
        const val EXTRA_SECTOR_NAME = "extra_sector_name"
        const val EXTRA_MINUTES_UNTIL = "extra_minutes_until"
        const val EXTRA_TIME_LABEL = "extra_time_label"

        const val NOTIFICATION_ID_PAC_ALARM = 2001
    }
}
