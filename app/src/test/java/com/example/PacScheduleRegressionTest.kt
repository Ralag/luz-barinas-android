package com.example

import com.example.data.model.PacBlock
import com.example.data.model.PacScheduleData
import com.example.data.model.PacSlot
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class PacScheduleRegressionTest {
    @Before
    fun setUp() { PacScheduleData.applyDoubleTurnPreset() }
    @After
    fun tearDown() { PacScheduleData.applyDoubleTurnPreset() }

    private fun caracas(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Long =
        GregorianCalendar(TimeZone.getTimeZone("America/Caracas")).apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }.timeInMillis

    @Test

    fun normalizesEveryBlockWithoutTakingTheBOfBloque() {
        for (block in listOf("A", "B", "C", "D")) {
            assertEquals(block, PacBlock.normalize(block))
            assertEquals(block, PacBlock.normalize(" Bloque ${block.lowercase()} "))
            assertEquals(block, PacBlock.normalize("BLOQUE   $block"))
        }
        for (invalid in listOf(null, "", " ", "E", "Bloque", "Bloque AB", "Barrio C", "C/D", "Bloque A extra")) {
            assertNull("Invalid block must not acquire a default: $invalid", PacBlock.normalize(invalid))
        }
    }

    @Test

    fun findsAnActualWindowForEachOfTheFourBlocks() {
        PacScheduleData.activeSlots.clear()
        PacScheduleData.activeSlots.addAll((0..3).map { PacSlot(it, "${8 + it}–${9 + it}", 8 + it, 9 + it) })
        PacScheduleData.activeMatrix = arrayOf("A", "B", "C", "D").map { Array(7) { _ -> it } }.toTypedArray()
        val now = caracas(2026, 10, 5, 0)
        for ((index, block) in listOf("A", "B", "C", "D").withIndex()) {
            val result = PacScheduleData.findScheduledWindowForBlock("Bloque $block", now)!!
            assertEquals(block, result.block)
            assertEquals(caracas(2026, 10, 5, 8 + index), result.startMillis)
            assertEquals(caracas(2026, 10, 5, 9 + index), result.endMillis)
        }
    }

    @Test

    fun missingAndInvalidBlocksNeverInventAnAlarmWindow() {
        val now = caracas(2026, 10, 5, 0)
        assertNull(PacScheduleData.findScheduledWindowForBlock("D", now))
        assertNull(PacScheduleData.findScheduledWindowForBlock("unknown", now))
        PacScheduleData.activeMatrix = Array(6) { Array(7) { "-" } }
        assertNull(PacScheduleData.findScheduledWindowForBlock("A", now))
    }

    @Test

    fun overnightUsesThePreviousStartDateAcrossAMonthBoundaryAndHasExclusiveEnd() {
        PacScheduleData.activeSlots.clear()
        PacScheduleData.activeSlots.add(PacSlot(0, "23–03", 23, 3))
        // Wednesday's A starts in September; Thursday's B starts in October.
        PacScheduleData.activeMatrix = arrayOf(arrayOf("-", "-", "A", "B", "-", "-", "-"))
        val now = caracas(2026, 10, 1, 1)
        val window = PacScheduleData.findScheduledWindowForBlock("A", now)!!
        assertTrue(window.isCurrentlyActive)
        assertEquals(caracas(2026, 9, 30, 23), window.startMillis)
        assertEquals(caracas(2026, 10, 1, 3), window.endMillis)
        assertEquals("A", PacScheduleData.getActiveBlockNow(now))
        val atEnd = PacScheduleData.findScheduledWindowForBlock("A", window.endMillis)!!
        assertFalse(atEnd.isCurrentlyActive)
        assertTrue(atEnd.startMillis > window.endMillis)
        assertEquals("-", PacScheduleData.getActiveBlockNow(window.endMillis))
    }

    @Test

    fun choosesChronologicallyEvenWhenSlotsAreOutOfOrderAndIgnoresDeviceTimezone() {
        val oldTimezone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            PacScheduleData.activeSlots.clear()
            PacScheduleData.activeSlots.addAll(listOf(PacSlot(0, "23–03", 23, 3), PacSlot(1, "07–11", 7, 11)))
            PacScheduleData.activeMatrix = Array(2) { Array(7) { "A" } }
            val now = caracas(2026, 10, 5, 4)
            assertEquals(caracas(2026, 10, 5, 7),
                PacScheduleData.findScheduledWindowForBlock("A", now, includeActive = false)!!.startMillis)
        } finally {
            TimeZone.setDefault(oldTimezone)
        }
    }

    @Test

    fun malformedScheduleCannotCreateAWindow() {
        val now = caracas(2026, 10, 5, 0)
        PacScheduleData.activeMatrix = arrayOf(arrayOf("A"))
        assertNull(PacScheduleData.findScheduledWindowForBlock("A", now))
        PacScheduleData.activeSlots.clear()
        PacScheduleData.activeSlots.add(PacSlot(0, "invalid", 7, 7))
        PacScheduleData.activeMatrix = arrayOf(Array(7) { "A" })
        assertNull(PacScheduleData.findScheduledWindowForBlock("A", now))
    }
}
