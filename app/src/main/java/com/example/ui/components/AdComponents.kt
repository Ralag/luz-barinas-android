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

/**
 * Interstitial Ad Manager - singleton to preload and show interstitial ads.
 * Call preload() in onCreate, then showAd() when needed (e.g., opening Settings).
 */
object InterstitialAdManager {
    private var interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null
    private var lastShownTime = 0L
    private const val MIN_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes between interstitials

    // Use test ID for development; replace with production ID later
    private const val AD_UNIT_ID = "ca-app-pub-7639154379634043/1963218233"

    fun preload(context: Context) {
        val adRequest = AdRequest.Builder().build()
        com.google.android.gms.ads.interstitial.InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: com.google.android.gms.ads.interstitial.InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showAd(activity: android.app.Activity): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastShownTime < MIN_INTERVAL_MS) return false

        val ad = interstitialAd ?: return false
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preload(activity) // Preload next one
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                interstitialAd = null
                preload(activity)
            }
        }
        ad.show(activity)
        lastShownTime = now
        return true
    }
}

/**
 * App Open Ad Manager - shows a fullscreen ad when the app is opened.
 * The user can close it. Shown only once per cold start.
 */
object AppOpenAdManager {
    private var appOpenAd: com.google.android.gms.ads.appopen.AppOpenAd? = null
    private var isShowingAd = false
    private var hasShownOnce = false

    // Use test ID for development; replace with production ID later
    private const val AD_UNIT_ID = "ca-app-pub-7639154379634043/8337054893"

    fun loadAd(context: Context) {
        if (appOpenAd != null || hasShownOnce) return

        val adRequest = AdRequest.Builder().build()
        com.google.android.gms.ads.appopen.AppOpenAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: com.google.android.gms.ads.appopen.AppOpenAd) {
                    appOpenAd = ad
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    appOpenAd = null
                }
            }
        )
    }

    fun showAdIfAvailable(activity: android.app.Activity) {
        if (isShowingAd || hasShownOnce) return

        val ad = appOpenAd ?: return
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowingAd = false
                hasShownOnce = true
                appOpenAd = null
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                isShowingAd = false
                hasShownOnce = true
                appOpenAd = null
            }
            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }
        ad.show(activity)
    }
}
