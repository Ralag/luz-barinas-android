package com.example.ads

/** A local, non-transferable reward. An existing reward cannot be stacked or extended. */
data class AdFreeRewardWindow(val grantedAtMillis: Long, val expiresAtMillis: Long) {
    fun isActive(nowMillis: Long): Boolean =
        grantedAtMillis > 0 && expiresAtMillis > grantedAtMillis &&
            expiresAtMillis - grantedAtMillis <= MAX_DURATION_MILLIS &&
            nowMillis >= grantedAtMillis && nowMillis < expiresAtMillis

    companion object {
        const val MAX_DURATION_MILLIS = 24 * 60 * 60 * 1000L

        fun grant(nowMillis: Long, hours: Int, current: AdFreeRewardWindow? = null): AdFreeRewardWindow {
            if (current?.isActive(nowMillis) == true) return current
            val duration = hours.coerceIn(1, 24) * 60 * 60 * 1000L
            require(nowMillis > 0 && nowMillis <= Long.MAX_VALUE - duration)
            return AdFreeRewardWindow(nowMillis, nowMillis + duration)
        }
    }
}

enum class AdFormat { BANNER, INTERSTITIAL, APP_OPEN, REWARDED }

fun resolveRewardHours(configured: String?): Int = configured?.toIntOrNull()?.coerceIn(1, 24) ?: 12

/** Debug never requests production inventory; missing or test IDs disable a release format. */
fun resolveAdUnitId(debug: Boolean, format: AdFormat, configured: String?): String? {
    val testId = when (format) {
        AdFormat.BANNER -> "ca-app-pub-3940256099942544/6300978111"
        AdFormat.INTERSTITIAL -> "ca-app-pub-3940256099942544/1033173712"
        AdFormat.APP_OPEN -> "ca-app-pub-3940256099942544/9257395921"
        AdFormat.REWARDED -> "ca-app-pub-3940256099942544/5224354917"
    }
    if (debug) return testId
    return configured?.trim()?.takeIf {
        Regex("ca-app-pub-[0-9]{16}/[0-9]{10}").matches(it) &&
            !it.startsWith("ca-app-pub-3940256099942544/")
    }
}
