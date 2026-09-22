package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

/**
 * Initialize AdMob SDK. Call once in MainActivity.onCreate().
 */
fun initializeAdMob(context: Context) {
    MobileAds.initialize(context) { }
}

/**
 * Gets the Ad Unit ID from BuildConfig / .env, falling back to Google's official Test ID.
 */
fun getBannerAdUnitId(): String {
    return try {
        val field = com.example.BuildConfig::class.java.getField("ADMOB_BANNER_UNIT_ID")
        val id = field.get(null) as? String
        if (!id.isNullOrBlank() && !id.contains("xxxx")) id else "ca-app-pub-7639154379634043/8172800723"
    } catch (e: Exception) {
        "ca-app-pub-7639154379634043/8172800723"
    }
}

/**
 * Adaptive Banner Ad composable.
 * Renders a banner ad that adapts to the screen width.
 * Uses test ad unit ID by default for development.
 */
@Composable
fun AdaptiveBannerAd(
    adUnitId: String = getBannerAdUnitId(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
