package com.example

import com.example.data.model.OutageRecord
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import com.example.engine.OutagePredictionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun `test modular arithmetic outage prediction engine`() {
    val sector = Sector(
      id = "sector_alto_barinas_norte",
      name = "Alto Barinas Norte",
      circuitCode = "Don Samuel",
      status = ServiceStatus.NORMAL,
      voltage = 118f,
      confirmedReportsCount = 20,
      withoutPowerPercentage = 5,
      lastUpdatedMillis = System.currentTimeMillis(),
      rotationBlock = "Bloque A",
      coordinates = emptyList()
    )

    val hourMs = 3600 * 1000L
    val dayMs = 24 * hourMs
    val now = System.currentTimeMillis()

    // 3 historical cycles of 4-hour outages with 4-hour daily modular shift
    val history = listOf(
      OutageRecord(
        id = 1,
        sectorId = sector.id,
        sectorName = sector.name,
        statusType = ServiceStatus.SCHEDULED_OUTAGE,
        startTimeMillis = now - dayMs - (4 * hourMs),
        endTimeMillis = now - dayMs,
        durationHours = 4.0f,
        notes = "PAC A"
      ),
      OutageRecord(
        id = 2,
        sectorId = sector.id,
        sectorName = sector.name,
        statusType = ServiceStatus.SCHEDULED_OUTAGE,
        startTimeMillis = now - (2 * dayMs) - (8 * hourMs),
        endTimeMillis = now - (2 * dayMs) - (4 * hourMs),
        durationHours = 4.0f,
        notes = "PAC A"
      )
    )

    val prediction = OutagePredictionEngine.calculateNextOutageWindow(
      sector = sector,
      history = history,
      currentTimeMillis = now
    )

    assertNotNull(prediction)
    assertEquals(sector.id, prediction.sectorId)
    assertTrue("Estimated duration should be 4.0 hours", prediction.estimatedDurationHours in 3.5f..4.5f)
    assertTrue("Confidence percentage should be at least 70%", prediction.confidencePercentage >= 70)
    assertTrue("Next start should be in future or now", prediction.nextEstimatedStartMillis >= now - 1000)
    assertTrue("Next end must be after start", prediction.nextEstimatedEndMillis > prediction.nextEstimatedStartMillis)
  }
}

