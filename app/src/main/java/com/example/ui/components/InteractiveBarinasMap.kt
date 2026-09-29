package com.example.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.Sector
import org.json.JSONArray
import org.json.JSONObject

class AndroidMapBridge(
    private val onSectorSelectedCallback: (String) -> Unit,
    private val onMapReadyCallback: () -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onSectorClicked(sectorId: String) {
        mainHandler.post {
            onSectorSelectedCallback(sectorId)
        }
    }

    @JavascriptInterface
    fun onMapReady() {
        mainHandler.post {
            onMapReadyCallback()
        }
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

    fun sendDataToMap(wv: WebView, sectorList: List<Sector>, selected: Sector?) {
        val jsonArray = JSONArray()
        for (s in sectorList) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("circuitCode", s.circuitCode)
                put("rotationBlock", s.rotationBlock)
                put("status", s.status.name)
                put("voltage", s.voltage.toDouble())
                put("isCommunity", s.isCommunity)
                if (s.coordinates.isNotEmpty()) {
                    put("lat", s.coordinates[0].first)
                    put("lon", s.coordinates[0].second)
                }
            }
            jsonArray.put(obj)
        }
        val encodedJson = JSONObject.quote(jsonArray.toString())
        val selectedIdStr = if (selected != null) "'${selected.id}'" else "null"
        val jsCall = "if (window.updateMapData) { window.updateMapData($encodedJson, $selectedIdStr); }"
        wv.evaluateJavascript(jsCall, null)
    }

    LaunchedEffect(sectors, selectedSector, isMapReady) {
        val wv = webViewRef
        if (wv != null && isMapReady) {
            sendDataToMap(wv, sectors, selectedSector)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF09090B), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.parseColor("#09090B"))
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportZoom(true)
                        builtInZoomControls = false
                        displayZoomControls = false
                        allowFileAccess = true
                        allowContentAccess = true
                    }

                    val bridge = AndroidMapBridge(
                        onSectorSelectedCallback = { sectorId ->
                            val found = sectors.find { it.id == sectorId }
                            if (found != null) {
                                onSectorSelected(found)
                            }
                        },
                        onMapReadyCallback = {
                            isMapReady = true
                            sendDataToMap(this, sectors, selectedSector)
                        }
                    )
                    addJavascriptInterface(bridge, "AndroidBridge")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapReady = true
                            view?.let { sendDataToMap(it, sectors, selectedSector) }
                        }
                    }

                    loadUrl("file:///android_asset/map/map.html")
                    webViewRef = this
                }
            },
            update = { wv ->
                if (isMapReady) {
                    sendDataToMap(wv, sectors, selectedSector)
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
            webViewRef = null
        }
    }
}
