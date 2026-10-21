package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.example.ui.components.AppOpenAdManager
import com.example.ui.components.InterstitialAdManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RewardedAdStatus { IDLE, LOADING, READY, SHOWING, ERROR }
data class RewardedAdState(val status: RewardedAdStatus = RewardedAdStatus.IDLE, val message: String? = null)

/** No automatic display: a load action and a separate, explicit show action are required. */
object RewardedAdManager {
    private var ad: RewardedAd? = null
    private var loadedAt = 0L
    private var failedAt: Long? = null
    private var generation = 0
    private val mutableState = MutableStateFlow(RewardedAdState())
    val state = mutableState.asStateFlow()

    fun clear() {
        generation++
        ad = null
        if (state.value.status != RewardedAdStatus.SHOWING) mutableState.value = RewardedAdState()
    }

    fun load(context: Context) {
        if (state.value.status == RewardedAdStatus.LOADING || state.value.status == RewardedAdStatus.SHOWING) return
        if (!AdsRuntime.canRequestAds(context)) {
            mutableState.value = RewardedAdState(RewardedAdStatus.ERROR,
                "El anuncio no está disponible. Puedes seguir usando todas las funciones.")
            return
        }
        val unitId = AdsConfiguration.unitId(AdFormat.REWARDED)
        if (unitId == null) {
            mutableState.value = RewardedAdState(RewardedAdStatus.ERROR, "La recompensa aún no está habilitada.")
            return
        }
        val now = SystemClock.elapsedRealtime()
        if (ad != null && now - loadedAt < 60 * 60 * 1000L) return
        if (failedAt?.let { now - it < 30_000L } == true) {
            mutableState.value = RewardedAdState(RewardedAdStatus.ERROR,
                "Espera 30 segundos antes de intentar cargar otro anuncio.")
            return
        }
        ad = null
        mutableState.value = RewardedAdState(RewardedAdStatus.LOADING)
        val requestGeneration = generation
        RewardedAd.load(context.applicationContext, unitId, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(value: RewardedAd) {
                    if (generation != requestGeneration) return
                    if (!AdsRuntime.canRequestAds(context)) { clear(); return }
                    ad = value
                    loadedAt = SystemClock.elapsedRealtime()
                    failedAt = null
                    mutableState.value = RewardedAdState(RewardedAdStatus.READY)
                    android.util.Log.i("PacAds", "rewarded loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (generation != requestGeneration) return
                    ad = null
                    failedAt = SystemClock.elapsedRealtime()
                    android.util.Log.i("PacAds", "rewarded load failed: code=${error.code}, domain=${error.domain}")
                    mutableState.value = RewardedAdState(RewardedAdStatus.ERROR,
                        "No hay un anuncio disponible ahora. Puedes intentarlo más tarde.")
                }
            })
    }

    fun show(activity: Activity): Boolean {
        if (!AdsRuntime.canRequestAds(activity) || state.value.status != RewardedAdStatus.READY) return false
        val ready = ad ?: return false
        if (SystemClock.elapsedRealtime() - loadedAt >= 60 * 60 * 1000L) {
            ad = null
            mutableState.value = RewardedAdState(RewardedAdStatus.ERROR, "El anuncio caducó. Vuelve a cargarlo.")
            return false
        }
        if (!FullScreenAdCoordinator.acquire(activity)) return false
        ad = null
        mutableState.value = RewardedAdState(RewardedAdStatus.SHOWING)
        var earned = false
        var persisted = true
        var completed = false
        fun finish(failed: Boolean = false) {
            if (completed) return
            completed = true
            FullScreenAdCoordinator.release()
            mutableState.value = RewardedAdState(
                if (failed) RewardedAdStatus.ERROR else RewardedAdStatus.IDLE,
                when {
                    earned && !persisted -> "Recompensa activa en esta sesión. El teléfono no pudo guardarla para el próximo inicio."
                    earned -> "Recompensa activada."
                    failed -> "El anuncio no pudo mostrarse. Puedes seguir usando la app."
                    else -> "El anuncio se cerró sin confirmar la recompensa. Puedes seguir usando la app."
                }
            )
        }
        ready.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = finish()
            override fun onAdFailedToShowFullScreenContent(error: AdError) = finish(failed = true)
        }
        return try {
            ready.show(activity) {
                // The SDK reward callback is the only place that grants the benefit.
                if (!earned) {
                    earned = true
                    persisted = AdFreeRewardStore.grantEarnedReward(activity.applicationContext)
                    InterstitialAdManager.clear()
                    AppOpenAdManager.clear()
                }
            }
            true
        } catch (_: RuntimeException) {
            finish(failed = true)
            false
        }
    }
}
