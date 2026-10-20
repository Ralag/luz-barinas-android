package com.example.ads

enum class AppOpenLaunchPhase { WAITING, SHOWING, FINISHED }
enum class AppOpenLaunchAction { NONE, LOAD, SHOW, CONTINUE }

/** Main-thread state machine: once the loading screen ends, no event may reopen an ad. */
class AppOpenLaunchPolicy(startedAtMillis: Long, timeoutMillis: Long = 3_500L) {
    val deadlineMillis = startedAtMillis + timeoutMillis.coerceIn(1L, 4_000L)
    var phase = AppOpenLaunchPhase.WAITING
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
        if (adFree || !launchEligible || nowMillis >= deadlineMillis || loadFailed ||
            (consentResolved && !consentAllowsAds)) return continueOnce()
        if (!consentResolved || !consentAllowsAds || !sdkInitialized) return AppOpenLaunchAction.NONE
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
        if (phase == AppOpenLaunchPhase.WAITING) continueOnce() else AppOpenLaunchAction.NONE

    /** Only dismissal/failure may finish while the fullscreen ad owns the screen. */
    fun onAdFinished(): AppOpenLaunchAction =
        if (phase == AppOpenLaunchPhase.SHOWING) continueOnce() else AppOpenLaunchAction.NONE

    private fun continueOnce(): AppOpenLaunchAction {
        phase = AppOpenLaunchPhase.FINISHED
        return AppOpenLaunchAction.CONTINUE
    }
}
