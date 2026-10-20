package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.ui.components.AppOpenAdManager
import com.example.ui.components.InterstitialAdManager
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdsRuntimeState(
    val initialized: Boolean = false,
    val canRequestAds: Boolean = false,
    val consentResolved: Boolean = false,
    val privacyOptionsRequired: Boolean = false,
    val message: String? = null
)

object AdsRuntime {
    private val mutableState = MutableStateFlow(AdsRuntimeState())
    val state = mutableState.asStateFlow()
    private var initializationStarted = false
    private var consentRequestRunning = false
    private val mainHandler = Handler(Looper.getMainLooper())

    /** Refresh UMP on each activity launch before allowing any ad request. */
    fun initialize(activity: Activity) {
        AdFreeRewardStore.initialize(activity)
        if (consentRequestRunning) return
        consentRequestRunning = true
        mutableState.value = mutableState.value.copy(consentResolved = false, canRequestAds = false)
        val consent = UserMessagingPlatform.getConsentInformation(activity)
        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                if (activity.isFinishing || activity.isDestroyed) {
                    consentRequestRunning = false
                    reconcile(activity.applicationContext, consent)
                } else {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                        consentRequestRunning = false
                        reconcile(activity.applicationContext, consent,
                            if (error != null) "No se pudo actualizar la privacidad publicitaria." else null)
                    }
                }
            },
            {
                consentRequestRunning = false
                // UMP may still permit requests using an existing, valid choice.
                reconcile(activity.applicationContext, consent,
                    "No se pudo actualizar la privacidad publicitaria. Intenta de nuevo con conexión.")
            }
        )
    }

    private fun reconcile(context: Context, consent: ConsentInformation, message: String? = null) {
        val allowed = consent.canRequestAds()
        mutableState.value = mutableState.value.copy(
            canRequestAds = allowed,
            consentResolved = true,
            privacyOptionsRequired = consent.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED,
            message = message
        )
        if (!allowed) {
            InterstitialAdManager.clear()
            AppOpenAdManager.clear()
            RewardedAdManager.clear()
            return
        }
        if (mutableState.value.initialized) {
            AppOpenAdManager.loadAd(context.applicationContext)
            return
        }
        if (initializationStarted) return
        initializationStarted = true
        MobileAds.initialize(context.applicationContext) {
            mainHandler.post {
                mutableState.value = mutableState.value.copy(initialized = true)
                // Cache only: the loading gate is the sole caller allowed to show app-open ads.
                AppOpenAdManager.loadAd(context.applicationContext)
                InterstitialAdManager.preload(context.applicationContext)
            }
        }
    }

    fun canRequestAds(context: Context): Boolean = state.value.initialized &&
        state.value.canRequestAds && !AdFreeRewardStore.isActive(context)

    fun showPrivacyOptions(activity: Activity) {
        if (!canShowOn(activity)) return
        mutableState.value = mutableState.value.copy(canRequestAds = false)
        InterstitialAdManager.clear()
        AppOpenAdManager.clear()
        RewardedAdManager.clear()
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            reconcile(activity.applicationContext, UserMessagingPlatform.getConsentInformation(activity),
                if (error != null) "No se pudieron abrir las opciones de privacidad." else null)
        }
    }

    fun canShowOn(activity: Activity): Boolean = !activity.isFinishing && !activity.isDestroyed &&
        (activity as? LifecycleOwner)?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != false
}

/** One fullscreen ad at a time across rewarded, interstitial and app-open formats. */
internal object FullScreenAdCoordinator {
    private var showing = false
    val isShowing: Boolean get() = showing

    fun acquire(activity: Activity): Boolean {
        if (showing || !AdsRuntime.canShowOn(activity)) return false
        showing = true
        return true
    }

    fun release() { showing = false }
}
