package com.example.ads

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdFreeRewardStore {
    private val mutableWindow = MutableStateFlow<AdFreeRewardWindow?>(null)
    val window = mutableWindow.asStateFlow()
    private var initialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        val prefs = context.applicationContext.getSharedPreferences("pac_ad_free_reward", Context.MODE_PRIVATE)
        mutableWindow.value = AdFreeRewardWindow(
            prefs.getLong("granted_at", 0), prefs.getLong("expires_at", 0)
        )
        initialized = true
    }

    fun isActive(context: Context, nowMillis: Long = System.currentTimeMillis()): Boolean {
        initialize(context)
        return mutableWindow.value?.isActive(nowMillis) == true
    }

    /** Called only by RewardedAd's onUserEarnedReward callback, never by dismissal or ad load. */
    @Synchronized
    internal fun grantEarnedReward(context: Context): Boolean {
        initialize(context)
        val reward = AdFreeRewardWindow.grant(
            System.currentTimeMillis(), AdsConfiguration.rewardHours, mutableWindow.value
        )
        // Honor the earned reward in memory even if the device cannot persist preferences.
        mutableWindow.value = reward
        return context.applicationContext.getSharedPreferences("pac_ad_free_reward", Context.MODE_PRIVATE)
            .edit().putLong("granted_at", reward.grantedAtMillis)
            .putLong("expires_at", reward.expiresAtMillis).commit()
    }
}
