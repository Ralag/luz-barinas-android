package com.example

import com.example.ads.*
import org.junit.Assert.*
import org.junit.Test

class AppOpenLaunchPolicyTest {
    private fun step(policy: AppOpenLaunchPolicy, now: Long, ready: Boolean = false, adFree: Boolean = false,
                     resolved: Boolean = true, allowed: Boolean = true, failed: Boolean = false) =
        policy.evaluate(now, adFree, resolved, allowed, true, ready, failed, true, true)

    @Test
    fun loadCallbackShowsOnlyWithinLoadingDeadline() {
        val policy = AppOpenLaunchPolicy(0)
        assertEquals(AppOpenLaunchAction.LOAD, step(policy, 100))
        assertEquals(AppOpenLaunchAction.SHOW, step(policy, 2000, ready = true))
        assertEquals(AppOpenLaunchAction.NONE, step(policy, 5000, ready = true))
        assertEquals(AppOpenLaunchAction.CONTINUE, policy.onAdFinished())
        assertEquals(AppOpenLaunchAction.NONE, policy.onAdFinished())
    }
    @Test
    fun timeoutAndLateCallbackCannotShowOverContent() {
        val policy = AppOpenLaunchPolicy(0)
        assertEquals(AppOpenLaunchAction.LOAD, step(policy, 100))
        assertEquals(AppOpenLaunchAction.CONTINUE, step(policy, 5100, ready = true))
        assertEquals(AppOpenExitReason.LOAD_TIMEOUT, policy.exitReason)
        assertEquals(AppOpenLaunchAction.NONE, step(policy, 5101, ready = true))
    }
    @Test
    fun skipAndRewardEndLoadingExactlyOnce() {
        val policy = AppOpenLaunchPolicy(0)
        assertEquals(AppOpenLaunchAction.CONTINUE, policy.skip())
        assertEquals(AppOpenLaunchAction.NONE, step(policy, 1, ready = true))
        assertEquals(AppOpenLaunchAction.NONE, policy.skip())
        assertEquals(AppOpenLaunchAction.CONTINUE, step(AppOpenLaunchPolicy(0), 1, adFree = true))
    }
    @Test
    fun consentOrLoadFailureNeverBlocksEntry() {
        assertEquals(AppOpenLaunchAction.NONE, step(AppOpenLaunchPolicy(0), 100, resolved = false))
        assertEquals(AppOpenLaunchAction.CONTINUE, step(AppOpenLaunchPolicy(0), 100, allowed = false))
        assertEquals(AppOpenLaunchAction.CONTINUE, step(AppOpenLaunchPolicy(0), 100, failed = true))
    }

    @Test
    fun slowConsentAndSdkDoNotConsumeTheAdLoadingWindow() {
        val policy = AppOpenLaunchPolicy(0)
        assertEquals(AppOpenLaunchAction.NONE, step(policy, 4500, resolved = false))
        assertEquals(AppOpenLaunchAction.NONE, policy.evaluate(5000, false, true, true, false, false, false, true, true))
        assertEquals(AppOpenLaunchAction.LOAD, step(policy, 6000))
        assertEquals(AppOpenLaunchAction.SHOW, step(policy, 10_000, ready = true))
    }

    @Test
    fun initializationHasAHardLimitAndBackgroundCannotShow() {
        val policy = AppOpenLaunchPolicy(0)
        assertEquals(AppOpenLaunchAction.LOAD, policy.evaluate(1000, false, true, true, true, true, false, true, false))
        assertEquals(AppOpenLaunchAction.CONTINUE, step(policy, 12_000, resolved = false))
        assertEquals(AppOpenExitReason.STARTUP_TIMEOUT, policy.exitReason)
        assertEquals(AppOpenLaunchAction.NONE, step(policy, 12_001, ready = true))
    }

    @Test
    fun cachedConsentCanPreloadButCannotDisplayBeforeRefreshCompletes() {
        val policy = AppOpenLaunchPolicy(0)
        assertEquals(AppOpenLaunchAction.NONE, step(policy, 100, ready = true, resolved = false))
        assertEquals(AppOpenLaunchAction.SHOW, step(policy, 6000, ready = true))
    }
}
