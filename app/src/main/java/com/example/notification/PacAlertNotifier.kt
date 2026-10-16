package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.PacAlertTarget
import com.example.data.model.PacAlertPrefs
import com.example.data.model.PacScheduleWindow

/** Shared by exact alarms and the periodic fallback, with one durable delivery per event. */
object PacAlertNotifier {
    @Synchronized
    internal fun claim(context: Context, target: PacAlertTarget, startMillis: Long, kind: String): Boolean {
        val prefs = context.getSharedPreferences("pac_alert_deliveries", Context.MODE_PRIVATE)
        val key = "$kind:${target.profile}:${target.sectorId}:${target.block}"
        if (prefs.getLong(key, Long.MIN_VALUE) == startMillis) return false
        return prefs.edit().putLong(key, startMillis).commit()
    }

    fun showPreAlert(context: Context, target: PacAlertTarget, window: PacScheduleWindow, nowMillis: Long): Boolean {
        val settings = PacAlertPrefs.getSettings(context)
        if (!isCurrentTarget(context, target) || (!settings.isNotificationEnabled && !settings.isAlarmEnabled) ||
            nowMillis >= window.startMillis || window.startMillis - nowMillis > settings.advanceMinutes * 60_000L) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (!claim(context, target, window.startMillis, "pre")) return false
        val minutes = ((window.startMillis - nowMillis + 59_999) / 60_000).toInt()
        val builder = base(context, restore = false, vibrate = settings.isVibrationEnabled)
            .setContentTitle("⚡ Corte PAC en $minutes min (Bloque ${target.block})")
            .setContentText("${target.sectorName} (${window.timeLabel}). Carga tus dispositivos.")
        if (settings.isAlarmEnabled) addSiren(context, builder, settings.isVibrationEnabled)
        return post(context, PacAlarmReceiver.NOTIFICATION_ID_PAC_ALARM, builder)
    }

    fun showRestoreAlert(context: Context, target: PacAlertTarget, window: PacScheduleWindow): Boolean {
        val settings = PacAlertPrefs.getSettings(context)
        if (!isCurrentTarget(context, target) || !settings.isRestoreAlarmEnabled || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (!claim(context, target, window.startMillis, "restore")) return false
        val builder = base(context, restore = true, vibrate = settings.isVibrationEnabled)
            .setContentTitle("💡 Finalizó el turno PAC de tu sector")
            .setContentText("${target.sectorName}: verifica si regresó el suministro antes de reconectar equipos.")
        if (settings.isAlarmEnabled) addSiren(context, builder, settings.isVibrationEnabled)
        return post(context, NotificationHelper.NOTIFICATION_ID_RESTORE, builder)
    }

    fun showValidationAlert(context: Context, target: PacAlertTarget, window: PacScheduleWindow): Boolean {
        val settings = PacAlertPrefs.getSettings(context)
        if (!isCurrentTarget(context, target) || !settings.isNotificationEnabled || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (!claim(context, target, window.startMillis, "validation")) return false
        val builder = base(context, restore = false, vibrate = settings.isVibrationEnabled)
            .setContentTitle("⏱️ Comenzó el turno PAC de tu bloque")
            .setContentText("¿Se fue la luz en ${target.sectorName}?")
        for ((hasPower, requestCode) in listOf(true to 201, false to 202)) {
            val action = Intent(context, NotificationActionReceiver::class.java).apply {
                this.action = if (hasPower) NotificationActionReceiver.ACTION_POWER_ON else NotificationActionReceiver.ACTION_POWER_OFF
                putExtra(NotificationActionReceiver.EXTRA_SECTOR_ID, target.sectorId)
                putExtra(NotificationActionReceiver.EXTRA_SECTOR_NAME, target.sectorName)
                putExtra(NotificationActionReceiver.EXTRA_HAS_POWER, hasPower)
            }
            builder.addAction(
                if (hasPower) R.drawable.ic_power_on else R.drawable.ic_power_off,
                if (hasPower) "💡 Sí hay luz" else "🔌 No hay luz",
                PendingIntent.getBroadcast(context, requestCode, action, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
        }
        return post(context, NotificationHelper.NOTIFICATION_ID_ALERT, builder)
    }

    private fun isCurrentTarget(context: Context, target: PacAlertTarget): Boolean {
        val prefs = context.getSharedPreferences("luz_barinas_user_prefs", Context.MODE_PRIVATE)
        if (prefs.getString("active_profile", "home") != target.profile) return false
        val id = prefs.getString("profile_${target.profile}_sector_id", null)
            ?: if (target.profile == "home") prefs.getString("saved_sector_id", null)
                ?: prefs.getString("selected_sector_id", null) else null
        return id == target.sectorId
    }

    private fun base(context: Context, restore: Boolean, vibrate: Boolean): NotificationCompat.Builder {
        NotificationHelper.createNotificationChannel(context)
        val channel = if (restore) NotificationHelper.CHANNEL_ID_RESTORE else NotificationHelper.CHANNEL_ID_OUTAGE
        val channelId = if (vibrate) channel else "${channel}_sin_vibracion"
        if (!vibrate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(channelId,
                if (restore) "Fin del turno PAC sin vibración" else "Cortes PAC sin vibración",
                NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(false)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT).build())
            })
        }
        val open = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(if (restore) R.drawable.ic_power_on else R.drawable.ic_notification_bolt)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .setVibrate(if (vibrate) longArrayOf(0, 500, 250, 500) else longArrayOf(0))
            .setAutoCancel(true)
    }

    private fun addSiren(context: Context, builder: NotificationCompat.Builder, vibrate: Boolean) {
        val stop = Intent(context, PacAlarmReceiver::class.java).setAction(PacAlarmReceiver.ACTION_STOP_ALARM)
        builder.setCategory(NotificationCompat.CATEGORY_ALARM).setOngoing(true)
            .addAction(R.drawable.ic_power_off, "🔕 Detener alarma",
                PendingIntent.getBroadcast(context, 101, stop, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        PacAlarmPlayer.startAlarm(context, vibrate)
    }

    private fun post(context: Context, id: Int, builder: NotificationCompat.Builder): Boolean = try {
        NotificationManagerCompat.from(context).notify(id, builder.build())
        true
    } catch (_: SecurityException) {
        PacAlarmPlayer.stopAlarm(context)
        false
    }
}
