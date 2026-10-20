package com.example.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.Sector
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream

internal const val PAC_MAP_URL = "https://appassets.androidplatform.net/assets/map/map.html"

internal fun mapAssetName(uri: Uri): String? {
    if (uri.scheme != "https" || uri.host != "appassets.androidplatform.net" || uri.port != -1) return null
    val path = uri.path ?: return null
    val allowed = setOf("map.html", "leaflet.js", "leaflet-heat.js", "pac-map-core.js", "pac-map-app.js", "leaflet.css", "pac-map.css", "images/layers.png", "images/layers-2x.png", "images/marker-icon.png", "images/marker-icon-2x.png", "images/marker-shadow.png")
    return path.removePrefix("/assets/map/").takeIf { path.startsWith("/assets/map/") && it in allowed }
}

internal fun mapDataScript(sectors: List<Sector>, selected: Sector?): String {
    val payload = JSONArray()
    sectors.forEach { sector ->
        payload.put(JSONObject().apply {
            put("id", sector.id)
            put("name", sector.name)
            put("circuitCode", sector.circuitCode)
            put("rotationBlock", sector.rotationBlock)
            put("status", sector.status.name)
            put("voltage", sector.voltage.takeIf { it.isFinite() && sector.status != com.example.data.model.ServiceStatus.UNKNOWN })
            put("isCommunity", sector.isCommunity)
            put("lastUpdatedMillis", sector.lastUpdatedMillis)
            sector.coordinates.firstOrNull()?.let { (lat, lon) ->
                if (lat.isFinite() && lon.isFinite() && lat in -90.0..90.0 && lon in -180.0..180.0 && !(lat == 0.0 && lon == 0.0)) {
                    put("lat", lat)
                    put("lon", lon)
                }
            }
        })
    }
    val selectedJson = selected?.id?.let(JSONObject::quote) ?: "null"
    return "if(window.updateMapData){window.updateMapData(${JSONObject.quote(payload.toString())},$selectedJson); }"
}

class AndroidMapBridge(
    private val onSectorSelectedCallback: (String) -> Unit,
    private val onMapReadyCallback: () -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var active = true

    @JavascriptInterface
    fun onSectorClicked(sectorId: String) {
        if (sectorId.length > 512) return
        mainHandler.post { if (active) onSectorSelectedCallback(sectorId) }
    }

    @JavascriptInterface
    fun onMapReady() {
        mainHandler.post { if (active) onMapReadyCallback() }
    }

    fun dispose() {
        active = false
        mainHandler.removeCallbacksAndMessages(null)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveBarinasMap(
    sectors: List<Sector>,
    selectedSector: Sector?,
    onSectorSelected: (Sector) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMapReady by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var bridgeRef by remember { mutableStateOf<AndroidMapBridge?>(null) }
    val currentSectors = rememberUpdatedState(sectors)
    val currentSelected = rememberUpdatedState(selectedSector)
    val currentOnSectorSelected = rememberUpdatedState(onSectorSelected)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(sectors, selectedSector, isMapReady) {
        if (isMapReady) webViewRef?.evaluateJavascript(mapDataScript(sectors, selectedSector), null)
    }

    Box(modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFF09090B)).border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp))) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    setBackgroundColor(android.graphics.Color.parseColor("#09090B"))
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportMultipleWindows(false)
                        javaScriptCanOpenWindowsAutomatically = false
                        allowFileAccess = false
                        allowContentAccess = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        @Suppress("DEPRECATION")
                        allowFileAccessFromFileURLs = false
                        @Suppress("DEPRECATION")
                        allowUniversalAccessFromFileURLs = false
                    }
                    val bridge = AndroidMapBridge(
                        onSectorSelectedCallback = { id -> currentSectors.value.find { it.id == id }?.let { currentOnSectorSelected.value(it) } },
                        onMapReadyCallback = {
                            isMapReady = true
                            evaluateJavascript(mapDataScript(currentSectors.value, currentSelected.value), null)
                        }
                    )
                    bridgeRef = bridge
                    addJavascriptInterface(bridge, "AndroidBridge")
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = true

                        @Deprecated("Legacy callback for older WebView implementations")
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean = true

                        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                            val uri = request?.url ?: return blockedResponse()
                            val asset = mapAssetName(uri)
                            if (asset != null) {
                                val mime = when (asset.substringAfterLast('.')) {
                                    "html" -> "text/html"
                                    "js" -> "application/javascript"
                                    "css" -> "text/css"
                                    else -> "image/png"
                                }
                                return try { WebResourceResponse(mime, "UTF-8", context.assets.open("map/$asset")) } catch (_: Exception) { blockedResponse() }
                            }
                            // Only remote map imagery is allowed. No Vercel page, script, iframe or arbitrary origin can inherit the native bridge.
                            val tileHost = uri.host == "server.arcgisonline.com" || uri.host == "tile.openstreetmap.org" || uri.host?.endsWith(".tile.openstreetmap.org") == true
                            return if (!request.isForMainFrame && uri.scheme == "https" && tileHost) null else blockedResponse()
                        }

                        private fun blockedResponse() = WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", emptyMap(), ByteArrayInputStream(ByteArray(0)))
                    }
                    webViewRef = this
                    loadUrl(PAC_MAP_URL)
                }
            }
        )
    }

    DisposableEffect(lifecycleOwner, webViewRef) {
        val webView = webViewRef
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> { webView?.onResume(); webView?.evaluateJavascript("window.resumePacMap&&window.resumePacMap();", null) }
                Lifecycle.Event.ON_PAUSE -> webView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        onDispose {
            bridgeRef?.dispose()
            webViewRef?.apply {
                evaluateJavascript("window.disposePacMap&&window.disposePacMap();", null)
                removeJavascriptInterface("AndroidBridge")
                stopLoading()
                (parent as? ViewGroup)?.removeView(this)
                destroy()
            }
            webViewRef = null
            bridgeRef = null
        }
    }
}
