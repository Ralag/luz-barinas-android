package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdFormat
import com.example.ads.AdsConfiguration
import com.example.ads.AdsRuntime
import com.example.ads.FullScreenAdCoordinator
import com.example.ads.rememberAdFreeActive
import com.example.ads.resolveAdUnitId
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

fun initializeAdMob(activity: Activity) = AdsRuntime.initialize(activity)

fun getBannerAdUnitId(): String = AdsConfiguration.unitId(AdFormat.BANNER).orEmpty()

/** Width-aware banner, removed immediately for an earned reward or unavailable consent. */
@Composable
fun AdaptiveBannerAd(adUnitId: String = getBannerAdUnitId(), modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val runtime by AdsRuntime.state.collectAsStateWithLifecycle()
    val adFree = rememberAdFreeActive()
    val resolvedId = resolveAdUnitId(BuildConfig.DEBUG, AdFormat.BANNER, adUnitId)
    if (!runtime.initialized || !runtime.canRequestAds || adFree || resolvedId == null) return
    BoxWithConstraints(modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(1)
        key(resolvedId, widthDp) {
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            val adView = remember {
                AdView(context).apply {
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
                    this.adUnitId = resolvedId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() { android.util.Log.i("PacAds", "banner loaded") }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            android.util.Log.i("PacAds", "banner load failed: code=${error.code}, domain=${error.domain}")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
            DisposableEffect(adView, lifecycle) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> adView.resume()
                        Lifecycle.Event.ON_PAUSE -> adView.pause()
                        else -> Unit
                    }
                }
                lifecycle.addObserver(observer)
                if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) adView.pause()
                onDispose {
                    lifecycle.removeObserver(observer)
                    adView.destroy()
                }
            }
            AndroidView(factory = { adView }, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Invoke only at a completed navigation transition, never before a report, alarm or update. */
object InterstitialAdManager {
    private var ad: InterstitialAd? = null
    private var loading = false
    private var generation = 0
    private var loadedAt = 0L
    private var failedAt: Long? = null
    private val placement = com.example.ads.InterstitialPlacementPolicy(SystemClock.elapsedRealtime())
    private const val MAX_AGE_MS = 60 * 60 * 1000L
    private const val RETRY_INTERVAL_MS = 30_000L

    fun clear() { generation++; ad = null; loading = false }

    fun showForNavigation(activity: Activity, fromTab: Int, toTab: Int, onComplete: () -> Unit): Boolean {
        if (fromTab !in 0..2 || toTab !in 0..2 || fromTab == toTab) return false
        placement.recordNavigation(fromTab, toTab)
        if (com.example.ads.AppOpenInteractionGuard.blocked.value) return false
        preload(activity.applicationContext)
        return showAd(activity, onComplete)
    }

    fun preload(context: Context) {
        if (!AdsRuntime.canRequestAds(context) || loading) return
        val now = SystemClock.elapsedRealtime()
        if (ad != null && now - loadedAt < MAX_AGE_MS) return
        if (failedAt?.let { now - it < RETRY_INTERVAL_MS } == true) return
        val unitId = AdsConfiguration.unitId(AdFormat.INTERSTITIAL) ?: return
        ad = null
        loading = true
        val requestGeneration = generation
        InterstitialAd.load(context.applicationContext, unitId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(value: InterstitialAd) {
                    if (generation != requestGeneration) return
                    loading = false
                    if (!AdsRuntime.canRequestAds(context)) return
                    ad = value
                    loadedAt = SystemClock.elapsedRealtime()
                    failedAt = null
                    android.util.Log.i("PacAds", "interstitial loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (generation != requestGeneration) return
                    loading = false
                    ad = null
                    failedAt = SystemClock.elapsedRealtime()
                    android.util.Log.i("PacAds", "interstitial load failed: code=${error.code}, domain=${error.domain}")
                }
            })
    }

    /** True means a show was requested; completion runs once after dismissal or display failure. */
    fun showAd(activity: Activity, onComplete: () -> Unit = {}): Boolean {
        if (!AdsRuntime.canRequestAds(activity)) { clear(); return false }
        if (com.example.notification.PacAlarmPlayer.isPlayingFlow.value) return false
        val now = SystemClock.elapsedRealtime()
        if (!placement.isEligible(now, FullScreenAdCoordinator.lastFinishedAtMillis) ||
            !FullScreenAdCoordinator.canShowAutomaticAd(now)) return false
        val ready = ad
        if (ready == null || now - loadedAt >= MAX_AGE_MS) {
            ad = null
            preload(activity.applicationContext)
            return false
        }
        if (!FullScreenAdCoordinator.acquire(activity)) return false
        ad = null
        var completed = false
        fun finish() {
            if (completed) return
            completed = true
            FullScreenAdCoordinator.release()
            onComplete()
            preload(activity.applicationContext)
        }
        ready.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { placement.onImpression(SystemClock.elapsedRealtime()) }
            override fun onAdDismissedFullScreenContent() = finish()
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                android.util.Log.i("PacAds", "interstitial show failed: code=${error.code}, domain=${error.domain}")
                finish()
            }
        }
        return try { ready.show(activity); true } catch (_: RuntimeException) {
            completed = true
            FullScreenAdCoordinator.release()
            preload(activity.applicationContext)
            false
        }
    }
}

/** Retained for a genuine loading screen. Calling after the main content appeared is rejected. */
enum class AppOpenAdStatus { IDLE, LOADING, READY, FAILED, SHOWING }

object AppOpenAdManager {
    private var ad: AppOpenAd? = null
    private var loading = false
    private var generation = 0
    private var loadedAt = 0L
    private var failedAt: Long? = null
    private var lastShownAt: Long? = null
    private const val MIN_INTERVAL_MS = 10 * 60 * 1000L
    private const val MAX_AGE_MS = 4 * 60 * 60 * 1000L
    private val mutableState = MutableStateFlow(AppOpenAdStatus.IDLE)
    val state = mutableState.asStateFlow()

    fun isLaunchEligible(): Boolean = AdsConfiguration.unitId(AdFormat.APP_OPEN) != null &&
        FullScreenAdCoordinator.canShowAutomaticAd() &&
        lastShownAt?.let { SystemClock.elapsedRealtime() - it >= MIN_INTERVAL_MS } != false

    fun hasAvailableAd(): Boolean = ad != null &&
        SystemClock.elapsedRealtime() - loadedAt < MAX_AGE_MS

    fun clear() {
        generation++
        ad = null
        loading = false
        if (state.value != AppOpenAdStatus.SHOWING) mutableState.value = AppOpenAdStatus.IDLE
    }

    fun loadAd(context: Context) {
        if (!AdsRuntime.canRequestAds(context) || loading) return
        val now = SystemClock.elapsedRealtime()
        if (ad != null && now - loadedAt < MAX_AGE_MS) return
        if (failedAt?.let { now - it < 30_000L } == true) return
        val unitId = AdsConfiguration.unitId(AdFormat.APP_OPEN) ?: return
        ad = null
        loading = true
        mutableState.value = AppOpenAdStatus.LOADING
        val requestGeneration = generation
        AppOpenAd.load(context.applicationContext, unitId, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(value: AppOpenAd) {
                    if (generation != requestGeneration) return
                    loading = false
                    if (!AdsRuntime.canRequestAds(context)) {
                        mutableState.value = AppOpenAdStatus.IDLE
                        return
                    }
                    ad = value
                    loadedAt = SystemClock.elapsedRealtime()
                    failedAt = null
                    mutableState.value = AppOpenAdStatus.READY
                    android.util.Log.i("PacAds", "app_open loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (generation != requestGeneration) return
                    loading = false
                    ad = null
                    failedAt = SystemClock.elapsedRealtime()
                    mutableState.value = AppOpenAdStatus.FAILED
                    android.util.Log.i("PacAds", "app_open load failed: code=${error.code}, domain=${error.domain}")
                }
            })
    }

    fun showAdIfAvailable(activity: Activity, isLoadingScreen: Boolean = false,
                          onComplete: () -> Unit = {}): Boolean {
        if (!isLoadingScreen || !isLaunchEligible() || !AdsRuntime.canRequestAds(activity)) return false
        val ready = ad ?: return false
        if (SystemClock.elapsedRealtime() - loadedAt >= MAX_AGE_MS) { clear(); return false }
        if (!FullScreenAdCoordinator.acquire(activity)) return false
        ad = null
        mutableState.value = AppOpenAdStatus.SHOWING
        var completed = false
        fun finish() {
            if (completed) return
            completed = true
            FullScreenAdCoordinator.release()
            mutableState.value = AppOpenAdStatus.IDLE
            onComplete()
            loadAd(activity.applicationContext)
        }
        ready.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                lastShownAt = SystemClock.elapsedRealtime()
                android.util.Log.i("PacAds", "app_open shown")
            }
            override fun onAdDismissedFullScreenContent() = finish()
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                android.util.Log.i("PacAds", "app_open show failed: code=${error.code}, domain=${error.domain}")
                finish()
            }
        }
        return try { ready.show(activity); true } catch (_: RuntimeException) {
            completed = true
            FullScreenAdCoordinator.release()
            mutableState.value = AppOpenAdStatus.FAILED
            false
        }
    }
}
