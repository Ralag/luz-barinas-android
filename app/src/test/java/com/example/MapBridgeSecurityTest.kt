package com.example

import android.net.Uri
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import com.example.ui.components.mapAssetName
import com.example.ui.components.mapDataScript
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MapBridgeSecurityTest {
    private fun sector(id: String) = Sector(
        id = id, name = "<img src=x onerror=alert(1)>", circuitCode = "test", status = ServiceStatus.IRREGULAR_OUTAGE,
        voltage = Float.NaN, confirmedReportsCount = 0, withoutPowerPercentage = 0, lastUpdatedMillis = 1L,
        rotationBlock = "Bloque D", coordinates = listOf(Double.NaN to -70.0)
    )

    @Test
    fun selectedIdAndPayloadAreEncodedAsJsonInsteadOfExecutableJavascript() {
        val selected = sector("quote'\"\\\n);alert(1);//")
        val script = mapDataScript(listOf(selected), selected)
        val argumentStart = script.indexOf("window.updateMapData(") + "window.updateMapData(".length
        val arguments = JSONArray("[" + script.substring(argumentStart, script.lastIndexOf(");")) + "]")
        assertEquals(selected.id, arguments.getString(1))
        val payload = JSONArray(arguments.getString(0)).getJSONObject(0)
        assertEquals(selected.id, payload.getString("id"))
        assertEquals("IRREGULAR_OUTAGE", payload.getString("status"))
        assertEquals("<img src=x onerror=alert(1)>", payload.getString("name"))
        assertFalse(payload.has("lat"))
        assertFalse(payload.has("lon"))
        assertTrue(payload.isNull("voltage"))
    }

    @Test
    fun localAssetOriginOnlyServesAnExplicitAllowlist() {
        assertEquals("map.html", mapAssetName(Uri.parse("https://appassets.androidplatform.net/assets/map/map.html")))
        assertEquals("pac-map-core.js", mapAssetName(Uri.parse("https://appassets.androidplatform.net/assets/map/pac-map-core.js")))
        for (url in listOf(
            "file:///android_asset/map/map.html", "https://evil.example/assets/map/map.html",
            "http://appassets.androidplatform.net/assets/map/map.html", "https://appassets.androidplatform.net:443/assets/map/map.html",
            "https://appassets.androidplatform.net/assets/map/../secret", "https://appassets.androidplatform.net/assets/map/%2e%2e/secret",
            "https://appassets.androidplatform.net/assets/map/other.js"
        )) assertNull(url, mapAssetName(Uri.parse(url)))
    }

    @Test
    fun coordinatesAndZeroVoltageArePreservedWhenValid() {
        val selected = sector("valid").copy(voltage = 0f, coordinates = listOf(8.62 to -70.2))
        val script = mapDataScript(listOf(selected), null)
        val argumentStart = script.indexOf("window.updateMapData(") + "window.updateMapData(".length
        val arguments = JSONArray("[" + script.substring(argumentStart, script.lastIndexOf(");")) + "]")
        val payload = JSONArray(arguments.getString(0)).getJSONObject(0)
        assertEquals(8.62, payload.getDouble("lat"), .000001)
        assertEquals(-70.2, payload.getDouble("lon"), .000001)
        assertEquals(0.0, payload.getDouble("voltage"), .000001)
        assertTrue(arguments.isNull(1))
    }
}
