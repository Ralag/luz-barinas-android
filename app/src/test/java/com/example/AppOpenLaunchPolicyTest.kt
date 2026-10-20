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
        assertEquals(AppOpenLaunchAction.CONTINUE, step(policy, 3500, ready = true))
        assertEquals(AppOpenLaunchAction.NONE, step(policy, 3501, ready = true))
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
}
