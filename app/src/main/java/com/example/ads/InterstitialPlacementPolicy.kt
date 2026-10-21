package com.example.ads

/** Elapsed time only permits an ad at an explicit, completed navigation opportunity. */
class InterstitialPlacementPolicy(private val sessionStartedAtMillis: Long) {
    private var transitions = 0
    private var lastImpressionAtMillis: Long? = null

    fun recordNavigation(fromTab: Int, toTab: Int) {
        if (fromTab in 0..2 && toTab in 0..2 && fromTab != toTab) transitions++
    }

    fun isEligible(nowMillis: Long, lastFullscreenFinishedAtMillis: Long? = null): Boolean {
        if (nowMillis - sessionStartedAtMillis < MIN_SESSION_MILLIS || transitions < MIN_TRANSITIONS) return false
        if (lastImpressionAtMillis?.let { nowMillis - it < MIN_INTERVAL_MILLIS } == true) return false
        return lastFullscreenFinishedAtMillis?.let { nowMillis - it >= MIN_INTERVAL_MILLIS } != false
    }

    fun onImpression(nowMillis: Long) {
        lastImpressionAtMillis = nowMillis
        transitions = 0
    }

    companion object {
        const val MIN_SESSION_MILLIS = 90_000L
        const val MIN_INTERVAL_MILLIS = 3 * 60_000L
        const val MIN_TRANSITIONS = 3
    }
}
