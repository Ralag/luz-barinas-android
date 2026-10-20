package com.example

import com.example.data.model.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class PacScheduleSnapshotTest {
    private fun source(extra: String = "") = Json.parseToJsonElement("""{
        "slots":[{"timeLabel":"Noche","startHour":23,"endHour":3}],
        "matrix":[["A","B","C","D","A","B","C"]]$extra} """).jsonObject

    @Test
    fun isoAndNumericRevisionsRepresentTheSameUpdate() {
        val iso = PacScheduleSnapshot.parse(source(",\"updatedAt\":\"2026-10-16T12:00:00.000Z\""))
        val numeric = PacScheduleSnapshot.parse(source(",\"updatedAt\":1792152000000"))
        assertEquals(numeric.revision, iso.revision)
        assertEquals(numeric.fingerprint(), iso.fingerprint())
    }
    @Test
    fun invalidPayloadCannotPartiallyChangeTheLiveSchedule() {
        val previous = PacScheduleData.getScheduleSnapshot()
        try {
            val bad = source().toMutableMap().apply { put("matrix", JsonArray(listOf(JsonArray(listOf(JsonPrimitive("Z")))))) }
            assertThrows(IllegalArgumentException::class.java) { PacScheduleSnapshot.parse(JsonObject(bad)) }
            assertEquals(previous.fingerprint(), PacScheduleData.getScheduleSnapshot().fingerprint())
        } finally { PacScheduleData.applySnapshot(previous, previous.revision) }
    }
    @Test
    fun customSlotGapHasNoInventedActiveSlot() {
        val previous = PacScheduleData.getScheduleSnapshot()
        try {
            PacScheduleData.applySnapshot(PacScheduleSnapshot.parse(source()), 1)
            assertEquals(-1, PacScheduleData.getCurrentSlotIndex(12))
            assertEquals(0, PacScheduleData.getCurrentSlotIndex(0))
        } finally { PacScheduleData.applySnapshot(previous, previous.revision) }
    }
    @Test
    fun tutorialAllowsOnlyDirectHttpsYoutubeVideoLinks() {
        assertNotNull(TutorialLink.validate("https://youtu.be/dQw4w9WgXcQ"))
        assertNull(TutorialLink.validate("https://youtube.com.evil.test/watch?v=dQw4w9WgXcQ"))
        assertNull(TutorialLink.validate("javascript:alert(1)"))
        assertNull(TutorialLink.validate("https://youtube.com@evil.test/watch?v=dQw4w9WgXcQ"))
    }

    @Test
    fun reportKindsKeepIrregularSupplyEvenWithPower() {
        for (type in listOf("FALLA_IRREGULAR", "IRREGULAR_OUTAGE", "BAJON", "AVERÍA")) {
            assertEquals(ServiceStatus.IRREGULAR_OUTAGE, ReportSemantics.status(type, true))
        }
        assertEquals(ServiceStatus.UNKNOWN, ReportSemantics.remoteStatus("unrecognised"))
        assertEquals("SIN_LUZ", ReportSemantics.canonicalType("unknown", false))
    }
}
