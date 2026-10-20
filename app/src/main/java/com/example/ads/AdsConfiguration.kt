package com.example.ads

import com.example.BuildConfig

object AdsConfiguration {
    val rewardHours: Int
        get() = resolveRewardHours(BuildConfig.ADMOB_REWARD_HOURS)

    fun unitId(format: AdFormat): String? = resolveAdUnitId(
        BuildConfig.DEBUG,
        format,
        when (format) {
            AdFormat.BANNER -> BuildConfig.ADMOB_BANNER_UNIT_ID
            AdFormat.INTERSTITIAL -> BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID
            AdFormat.APP_OPEN -> BuildConfig.ADMOB_APP_OPEN_UNIT_ID
            AdFormat.REWARDED -> BuildConfig.ADMOB_REWARDED_UNIT_ID
        }
    )
}
