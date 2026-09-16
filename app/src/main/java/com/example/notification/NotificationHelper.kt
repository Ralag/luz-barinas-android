package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "alertas_cortes_barinas"
    const val CHANNEL_NAME = "Alertas de Cortes Eléctricos"
    const val NOTIFICATION_ID_ALERT = 1001
    const val NOTIFICATION_ID_CONFIRMATION = 1002
    const val NOTIFICATION_ID_PREDICTIVE = 1003

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = "Notificaciones interactivas para telemetría ciudadana de cortes en Barinas"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showInteractiveOutageAlert(
        context: Context,
        sectorId: String = "sec_a_alto_barinas_1",
        sectorName: String = "Alto Barinas 1",
        circuitCode: String = "Don Samuel"
    ) {
        createNotificationChannel(context)

        // PendingIntent for main app launch if user taps content
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: "Sí, hay luz" (No friction)
        val powerOnIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_POWER_ON
            putExtra(NotificationActionReceiver.EXTRA_SECTOR_ID, sectorId)
            putExtra(NotificationActionReceiver.EXTRA_SECTOR_NAME, sectorName)
            putExtra(NotificationActionReceiver.EXTRA_HAS_POWER, true)
        }
        val powerOnPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            powerOnIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: "Se fue la luz" (No friction)
        val powerOffIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_POWER_OFF
            putExtra(NotificationActionReceiver.EXTRA_SECTOR_ID, sectorId)
            putExtra(NotificationActionReceiver.EXTRA_SECTOR_NAME, sectorName)
            putExtra(NotificationActionReceiver.EXTRA_HAS_POWER, false)
        }
        val powerOffPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            powerOffIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bolt)
            .setContentTitle("⚡ ¿Tienes servicio en $sectorName?")
            .setContentText("Circuito $circuitCode reporta variación. Confirma tu estado con un toque:")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Se detectó un cambio en el circuito $circuitCode ($sectorName). Tu telemetría actualiza el mapa ciudadano en tiempo real:")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_power_on,
                "🟢 Sí, hay luz",
                powerOnPendingIntent
            )
            .addAction(
                R.drawable.ic_power_off,
                "🔴 Se fue la luz",
                powerOffPendingIntent
            )

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ALERT, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted yet on Android 13+
        }
    }

    fun showConfirmationNotification(
        context: Context,
        sectorName: String,
        hasPower: Boolean
    ) {
        val statusText = if (hasPower) "Servicio Activo (Con Luz)" else "Corte Eléctrico Reportado"
        val icon = if (hasPower) R.drawable.ic_power_on else R.drawable.ic_power_off

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle("✅ Telemetría registrada")
            .setContentText("Reporte para $sectorName: $statusText. Sincronizando en segundo plano.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CONFIRMATION, builder.build())
        } catch (e: SecurityException) {
            // Ignore if permissions missing
        }
    }

    /**
     * Predictive notification: warns the user about an upcoming outage in their sector.
     */
    fun showPredictiveAlert(
        context: Context,
        sectorName: String,
        sectorId: String = "sec_a_alto_barinas_1",
        minutesUntil: Int,
        slotLabel: String
    ) {
        createNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bolt)
            .setContentTitle("⚡ Corte programado en ~$minutesUntil min")
            .setContentText("$sectorName: corte PAC de $slotLabel. ¡Carga tus dispositivos!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Tu sector $sectorName tiene un corte PAC programado de $slotLabel en aproximadamente $minutesUntil minutos.\n\n🔋 Carga tus dispositivos\n💡 Prepara linternas\n🔌 Desconecta equipos sensibles"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 150, 300))

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PREDICTIVE, builder.build())
        } catch (e: SecurityException) {
            // Ignore if permissions missing
        }
    }
}
