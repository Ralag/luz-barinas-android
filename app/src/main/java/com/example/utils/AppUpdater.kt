package com.example.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object AppUpdater {

    suspend fun checkForUpdates(): com.example.ui.viewmodel.AppUpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
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
                val body = json.optString("body", "Nueva actualización disponible.")
                val htmlUrl = json.getString("html_url")
                
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
                    var apkUrl = htmlUrl
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            if (asset.getString("name").endsWith(".apk")) {
                                apkUrl = asset.getString("browser_download_url")
                                break
                            }
                        }
                    }
                    
                    return@withContext com.example.ui.viewmodel.AppUpdateInfo(
                        versionCode = com.example.BuildConfig.VERSION_CODE + 1,
                        versionName = remoteVersionName,
                        releaseNotes = body,
                        downloadUrl = apkUrl,
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
    
    suspend fun downloadAndInstallLatestRelease(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Fetch latest release from GitHub API
                val apiUrl = URL("https://api.github.com/repos/Ralag/luz-barinas-android/releases/latest")
                val connection = apiUrl.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No se pudo obtener la última versión. Intente descargar manualmente.", Toast.LENGTH_LONG).show()
                    }
                    return@withContext
                }
                
                val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseStr)
                val assets = json.getJSONArray("assets")
                
                var apkUrl: String? = null
                var apkName = "PAC-BARINAS-update.apk"
                
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk")) {
                        apkUrl = asset.getString("browser_download_url")
                        apkName = name
                        break
                    }
                }
                
                if (apkUrl == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No se encontró un archivo APK en el último release.", Toast.LENGTH_LONG).show()
                    }
                    return@withContext
                }
                
                // 2. Start Download using DownloadManager
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Descargando actualización en segundo plano...", Toast.LENGTH_LONG).show()
                }
                
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
                    setTitle("Actualizando PAC Barinas")
                    setDescription("Descargando la última versión...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName)
                }
                
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkName)
                if (file.exists() && file.length() > 5000000) { // If it's larger than 5MB, assume it's fully downloaded
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Actualización ya descargada. Iniciando instalación...", Toast.LENGTH_SHORT).show()
                    }
                    installApk(context, file)
                    return@withContext
                }
                
                // If partial or doesn't exist, delete and re-download
                if (file.exists()) {
                    file.delete()
                }
                
                val downloadId = downloadManager.enqueue(request)
                
                // 3. Register Receiver to install APK once downloaded
                val onComplete = object : BroadcastReceiver() {
                    override fun onReceive(ctxt: Context, intent: Intent) {
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (id == downloadId) {
                            installApk(ctxt, file)
                            ctxt.unregisterReceiver(this)
                        }
                    }
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
                } else {
                    context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al descargar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return
        
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
            } else {
                Uri.fromFile(apkFile)
            }
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al instalar. Busca el APK en tu carpeta de Descargas.", Toast.LENGTH_LONG).show()
        }
    }
}
