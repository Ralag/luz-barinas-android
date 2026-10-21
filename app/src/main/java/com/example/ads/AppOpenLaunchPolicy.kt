package com.example.ads

enum class AppOpenLaunchPhase { WAITING, SHOWING, FINISHED }
enum class AppOpenLaunchAction { NONE, LOAD, SHOW, CONTINUE }
enum class AppOpenExitReason { REWARD_ACTIVE, NOT_ELIGIBLE, STARTUP_TIMEOUT, LOAD_TIMEOUT, LOAD_FAILED, CONSENT_UNAVAILABLE, USER_SKIPPED, AD_FINISHED }

/** Main-thread state machine: once the loading screen ends, no event may reopen an ad. */
class AppOpenLaunchPolicy(startedAtMillis: Long, timeoutMillis: Long = 5_000L,
                          maxStartupMillis: Long = 12_000L) {
    val deadlineMillis = startedAtMillis + maxStartupMillis.coerceIn(1L, 15_000L)
    private val loadTimeoutMillis = timeoutMillis.coerceIn(1L, 5_000L)
    private var loadDeadlineMillis: Long? = null
    var phase = AppOpenLaunchPhase.WAITING
        private set
    var exitReason: AppOpenExitReason? = null
        private set
    private var requestedLoad = false

    fun evaluate(
        nowMillis: Long,
        adFree: Boolean,
        consentResolved: Boolean,
        consentAllowsAds: Boolean,
        sdkInitialized: Boolean,
        adReady: Boolean,
        loadFailed: Boolean,
        launchEligible: Boolean,
        activityResumed: Boolean
    ): AppOpenLaunchAction {
        if (phase != AppOpenLaunchPhase.WAITING) return AppOpenLaunchAction.NONE
        val reason = when {
            adFree -> AppOpenExitReason.REWARD_ACTIVE
            !launchEligible -> AppOpenExitReason.NOT_ELIGIBLE
            nowMillis >= deadlineMillis -> AppOpenExitReason.STARTUP_TIMEOUT
            loadDeadlineMillis?.let { nowMillis >= it } == true -> AppOpenExitReason.LOAD_TIMEOUT
            loadFailed -> AppOpenExitReason.LOAD_FAILED
            consentResolved && !consentAllowsAds -> AppOpenExitReason.CONSENT_UNAVAILABLE
            else -> null
        }
        if (reason != null) return continueOnce(reason)
        if (!consentResolved || !consentAllowsAds || !sdkInitialized) return AppOpenLaunchAction.NONE
        if (loadDeadlineMillis == null) loadDeadlineMillis = minOf(deadlineMillis, nowMillis + loadTimeoutMillis)
        if (adReady && activityResumed) {
            phase = AppOpenLaunchPhase.SHOWING
            return AppOpenLaunchAction.SHOW
        }
        if (!requestedLoad) {
            requestedLoad = true
            return AppOpenLaunchAction.LOAD
        }
        return AppOpenLaunchAction.NONE
    }

    fun skip(): AppOpenLaunchAction =
        if (phase == AppOpenLaunchPhase.WAITING) continueOnce(AppOpenExitReason.USER_SKIPPED) else AppOpenLaunchAction.NONE

    /** Only dismissal/failure may finish while the fullscreen ad owns the screen. */
    fun onAdFinished(): AppOpenLaunchAction =
        if (phase == AppOpenLaunchPhase.SHOWING) continueOnce(AppOpenExitReason.AD_FINISHED) else AppOpenLaunchAction.NONE

    private fun continueOnce(reason: AppOpenExitReason): AppOpenLaunchAction {
        exitReason = reason
        phase = AppOpenLaunchPhase.FINISHED
        return AppOpenLaunchAction.CONTINUE
    }
}
