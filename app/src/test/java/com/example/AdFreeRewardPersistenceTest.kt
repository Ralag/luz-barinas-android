package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ads.AdFreeRewardStore
import com.example.ads.AdFreeRewardWindow
import com.example.ads.AdsConfiguration
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AdFreeRewardPersistenceTest {
    @Test
    fun earnedRewardPersistsTheSameWindowUsedByAllAdGatesAndDoesNotExtendOnDuplicateCallback() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = context.getSharedPreferences("pac_ad_free_reward", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        assertFalse(AdFreeRewardStore.isActive(context))

        val before = System.currentTimeMillis()
        assertTrue(AdFreeRewardStore.grantEarnedReward(context))
        val stored = AdFreeRewardWindow(
            preferences.getLong("granted_at", 0), preferences.getLong("expires_at", 0)
        )
        assertTrue(stored.grantedAtMillis >= before)
        assertEquals(AdsConfiguration.rewardHours * 60 * 60 * 1000L,
            stored.expiresAtMillis - stored.grantedAtMillis)
        assertEquals(stored, AdFreeRewardStore.window.value)
        assertTrue(AdFreeRewardStore.isActive(context, stored.grantedAtMillis))
        assertFalse(AdFreeRewardStore.isActive(context, stored.expiresAtMillis))

        assertTrue(AdFreeRewardStore.grantEarnedReward(context))
        assertEquals(stored, AdFreeRewardStore.window.value)
        assertEquals(stored.expiresAtMillis, preferences.getLong("expires_at", 0))
    }
}
