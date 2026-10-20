package com.example

import com.example.ads.AdFormat
import com.example.ads.AdFreeRewardWindow
import com.example.ads.resolveAdUnitId
import com.example.ads.resolveRewardHours
import org.junit.Assert.*
import org.junit.Test

class AdFreeRewardWindowTest {
    private val now = 1_800_000_000_000L

    @Test
    fun defaultRewardIs12HoursAndConfigurationCannotExceedOneDay() {
        assertEquals(12, resolveRewardHours(null))
        assertEquals(12, resolveRewardHours("DISABLED"))
        assertEquals(12, resolveRewardHours(""))
        assertEquals(6, resolveRewardHours("6"))
        assertEquals(1, resolveRewardHours("-1"))
        assertEquals(24, resolveRewardHours("48"))
    }

    @Test
    fun oneRewardRemovesAdsForExactly24HoursWithoutStacking() {
        val window = AdFreeRewardWindow.grant(now, 24)
        assertTrue(window.isActive(now))
        assertTrue(window.isActive(now + 24 * 60 * 60 * 1000L - 1))
        assertFalse(window.isActive(now + 24 * 60 * 60 * 1000L))
        assertEquals(window, AdFreeRewardWindow.grant(now + 60_000, 24, window))
    }

    @Test
    fun expiredRewardCanBeEarnedAgainAndConfiguredDurationIsBounded() {
        val old = AdFreeRewardWindow.grant(now, 1)
        val renewed = AdFreeRewardWindow.grant(old.expiresAtMillis, 6, old)
        assertEquals(6 * 60 * 60 * 1000L, renewed.expiresAtMillis - renewed.grantedAtMillis)
        assertEquals(AdFreeRewardWindow.MAX_DURATION_MILLIS,
            AdFreeRewardWindow.grant(now, 100).expiresAtMillis - now)
        assertEquals(60 * 60 * 1000L, AdFreeRewardWindow.grant(now, 0).expiresAtMillis - now)
    }

    @Test
    fun corruptPreferencesOrClockBeforeTheGrantDoNotCreateUnlimitedAdFreeTime() {
        assertFalse(AdFreeRewardWindow(0, Long.MAX_VALUE).isActive(now))
        assertFalse(AdFreeRewardWindow(now, Long.MAX_VALUE).isActive(now + 1))
        assertFalse(AdFreeRewardWindow(now, now).isActive(now))
        assertFalse(AdFreeRewardWindow.grant(now, 24).isActive(now - 1))
    }

    @Test
    fun debugAlwaysUsesTheOfficialTestUnitForEveryFormatEvenWithProductionConfiguration() {
        val production = "ca-app-pub-7639154379634043/2005674160"
        for (format in AdFormat.entries) {
            assertTrue(resolveAdUnitId(true, format, production)!!.startsWith("ca-app-pub-3940256099942544/"))
        }
        assertEquals("ca-app-pub-3940256099942544/5224354917", resolveAdUnitId(true, AdFormat.REWARDED, null))
    }

    @Test
    fun releaseDisablesAFormatUntilAValidProductionUnitIsConfigured() {
        for (value in listOf(null, "", "DISABLED", "ca-app-pub-xxxx/yyyy",
            "ca-app-pub-3940256099942544/5224354917")) {
            assertNull(resolveAdUnitId(false, AdFormat.REWARDED, value))
        }
        val production = "ca-app-pub-7639154379634043/2005674160"
        assertEquals(production, resolveAdUnitId(false, AdFormat.BANNER, production))
    }
}
