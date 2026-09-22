package com.example.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM token: \$token")
        // No need to save token since we will use Topic messaging
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("barinas_global")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to barinas_global topic")
                }
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "Alerta PAC"
        val body = data["message"] ?: remoteMessage.notification?.body ?: ""
        val isAlarm = data["is_alarm"]?.toBoolean() ?: false
        
        Log.d("FCM", "Message received: \$title, isAlarm: \$isAlarm")

        if (isAlarm) {
            // Wake up and play siren
            CoroutineScope(Dispatchers.Main).launch {
                PacAlarmPlayer.startAlarm(applicationContext, true)
            }
        }
        
        // Show notification
        NotificationHelper.showBroadcastNoticeNotification(
            context = applicationContext,
            title = title,
            message = body,
            level = if (isAlarm) "EMERGENCY" else "INFO"
        )
    }
}
