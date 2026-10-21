package com.example

import com.example.ads.InterstitialPlacementPolicy
import org.junit.Assert.*
import org.junit.Test

class InterstitialPlacementPolicyTest {
    private fun threeTransitions(policy: InterstitialPlacementPolicy) {
        policy.recordNavigation(0, 1)
        policy.recordNavigation(1, 2)
        policy.recordNavigation(2, 0)
    }

    @Test
    fun timeAndThreeDistinctTransitionsAreBothRequired() {
        val policy = InterstitialPlacementPolicy(0)
        policy.recordNavigation(0, 0)
        policy.recordNavigation(0, 9)
        assertFalse(policy.isEligible(180_000))
        threeTransitions(policy)
        assertFalse(policy.isEligible(89_999))
        assertTrue(policy.isEligible(90_000))
    }

    @Test
    fun impressionRestartsBothCounterAndThreeMinuteInterval() {
        val policy = InterstitialPlacementPolicy(0)
        threeTransitions(policy)
        policy.onImpression(90_000)
        assertFalse(policy.isEligible(900_000))
        threeTransitions(policy)
        assertFalse(policy.isEligible(269_999))
        assertTrue(policy.isEligible(270_000))
    }

    @Test
    fun anotherFullscreenAdAlsoPreventsBackToBackAds() {
        val policy = InterstitialPlacementPolicy(0)
        threeTransitions(policy)
        assertFalse(policy.isEligible(180_000, 100_000))
        assertTrue(policy.isEligible(280_000, 100_000))
    }
}
