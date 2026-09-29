package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AppUpdater {

    suspend fun checkForUpdates(): com.example.ui.viewmodel.AppUpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
                // Sigue verificando las versiones a través de GitHub si lo deseas, 
                // o puedes usar la API in-app-updates de Play Store más adelante.
                // Por ahora mantenemos la alerta, pero el botón redirigirá a Play Store.
                val apiUrl = URL("https://api.github.com/repos/Ralag/luz-barinas-android/releases/latest")
                val connection = apiUrl.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext null
                }
                
                val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseStr)
                
                val tagName = json.getString("tag_name")
                val body = json.optString("body", "Nueva actualización disponible en Google Play.")
                
                val remoteVersionName = tagName.replace("v", "", ignoreCase = true).trim()
                val remoteParts = remoteVersionName.split(".").map { it.toIntOrNull() ?: 0 }
                
                val currentVersionName = com.example.BuildConfig.VERSION_NAME.replace("v", "", ignoreCase = true).trim()
                val currentParts = currentVersionName.split(".").map { it.toIntOrNull() ?: 0 }
                
                var isNewer = false
                for (i in 0 until maxOf(remoteParts.size, currentParts.size)) {
                    val r = remoteParts.getOrElse(i) { 0 }
                    val c = currentParts.getOrElse(i) { 0 }
                    if (r > c) {
                        isNewer = true
                        break
                    } else if (r < c) {
                        break
                    }
                }
                
                if (isNewer) {
                    return@withContext com.example.ui.viewmodel.AppUpdateInfo(
                        versionCode = com.example.BuildConfig.VERSION_CODE + 1,
                        versionName = remoteVersionName,
                        releaseNotes = body,
                        downloadUrl = "market://details?id=com.ralag.pacbarinas", // Play Store intent scheme
                        isMandatory = body.contains("[MANDATORY]", ignoreCase = true)
                    )
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    suspend fun openPlayStoreForUpdate(context: Context) {
        withContext(Dispatchers.Main) {
            try {
                // Redirigir siempre a la Play Store para cumplir con las políticas de Google Play
                val appPackageName = "com.ralag.pacbarinas" // Tu nuevo ID
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (anfe: android.content.ActivityNotFoundException) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "No se pudo abrir Google Play Store.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
