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
    const val CHANNEL_NAME = "Alertas PAC Barinas"

    const val CHANNEL_ID_OUTAGE = "pac_corte_luz"
    const val CHANNEL_NAME_OUTAGE = "Aviso de Corte de Luz (PAC)"

    const val CHANNEL_ID_RESTORE = "pac_restablecimiento_luz"
    const val CHANNEL_NAME_RESTORE = "Restablecimiento del Servicio Eléctrico"

    const val NOTIFICATION_ID_ALERT = 1001
    const val NOTIFICATION_ID_CONFIRMATION = 1002
    const val NOTIFICATION_ID_PREDICTIVE = 1003
    const val NOTIFICATION_ID_BROADCAST = 1004
    const val NOTIFICATION_ID_RESTORE = 1005

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            // 1. Canal General PAC
            val generalChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones comunitarias y avisos de PAC Barinas"
                enableVibration(true)
                setShowBadge(true)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(generalChannel)

            // 2. Canal de Corte de Luz (Vibración enfática y redundante)
            val outageChannel = NotificationChannel(
                CHANNEL_ID_OUTAGE,
                CHANNEL_NAME_OUTAGE,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos previos e inicio de corte de energía eléctrica (PAC)"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                setShowBadge(true)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(outageChannel)

            // 3. Canal de Restablecimiento de Luz (Patrón suave de retorno)
            val restoreChannel = NotificationChannel(
                CHANNEL_ID_RESTORE,
                CHANNEL_NAME_RESTORE,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones al finalizar el turno de racionamiento cuando retorna la luz"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200, 100, 400)
                setShowBadge(true)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(restoreChannel)
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
            .setContentText("Consulta comunitaria del circuito $circuitCode. Confirma tu estado con un toque:")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Consulta ciudadana sobre el circuito $circuitCode ($sectorName). Tu reporte actualiza el mapa comunitario:")
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
        val statusText = if (hasPower) "Con Luz" else "Sin Luz"
        val icon = if (hasPower) R.drawable.ic_power_on else R.drawable.ic_power_off

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle("✅ Reporte ciudadano registrado")
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

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_OUTAGE)
            .setSmallIcon(R.drawable.ic_notification_bolt)
            .setContentTitle("⚡ Corte programado en ~$minutesUntil min")
            .setContentText("$sectorName: corte PAC de $slotLabel. ¡Carga tus dispositivos!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Tu sector $sectorName tiene un corte PAC programado de $slotLabel en aproximadamente $minutesUntil minutos.\n\n🔋 Carga tus dispositivos\n💡 Prepara linternas\n🔌 Desconecta equipos sensibles"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PREDICTIVE, builder.build())
        } catch (e: SecurityException) {
            // Ignore if permissions missing
        }
    }

    /**
     * Broadcast official notice notification: alerts citizens of emergency notices, PAC updates, or announcements.
     */
    fun showBroadcastNoticeNotification(
        context: Context,
        title: String,
        message: String,
        level: String = "INFO"
    ) {
        createNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prefix = when (level.uppercase()) {
            "EMERGENCY" -> "🚨 AVISO URGENTE"
            "WARNING" -> "⚠️ ALERTA PAC"
            else -> "📢 COMUNICADO INFORMATIVO"
        }

        val fullTitle = "$prefix: $title"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bolt)
            .setContentTitle(fullTitle)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(fullTitle)
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 350, 200, 350))

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BROADCAST, builder.build())
        } catch (e: SecurityException) {
            // Ignore if permissions missing
        }
    }
}
