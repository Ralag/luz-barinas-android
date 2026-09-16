package com.example.engine

import com.example.data.model.OutagePrediction
import com.example.data.model.OutageRecord
import com.example.data.model.PacScheduleData
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object OutagePredictionEngine {

    private const val DEFAULT_OUTAGE_DURATION_HOURS = 4.0f
    private const val MODULO_DAY_HOURS = 24

    /**
     * Calculates the next expected outage window using the official PAC schedule matrix
     * fused with local historical citizen telemetry and modular arithmetic.
     * Works 100% offline using local Room data.
     */
    fun calculateNextOutageWindow(
        sector: Sector,
        history: List<OutageRecord>,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): OutagePrediction {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
            timeInMillis = currentTimeMillis
        }

        // Filter valid scheduled and historical records for this sector
        val sectorRecords = history
            .filter { it.sectorId == sector.id }
            .sortedByDescending { it.startTimeMillis }

        // Determine average duration from history or fallback to 4.0h
        val calculatedDuration = if (sectorRecords.isNotEmpty()) {
            val averageDur = sectorRecords.map { it.durationHours }.average().toFloat()
            if (averageDur in 2.0f..8.0f) averageDur else DEFAULT_OUTAGE_DURATION_HOURS
        } else {
            DEFAULT_OUTAGE_DURATION_HOURS
        }

        val cleanBlock = sector.rotationBlock.uppercase().replace("BLOQUE", "").trim()
        val pacWindow = if (cleanBlock in listOf("A", "B", "C", "D")) {
            PacScheduleData.findNextWindowForBlock(cleanBlock, currentTimeMillis)
        } else null

        if (pacWindow != null) {
            val startMillis = if (pacWindow.isCurrentlyActive) {
                // If currently active, the start was earlier today, but for upcoming or active:
                pacWindow.startMillis
            } else {
                pacWindow.startMillis
            }
            val endMillis = pacWindow.endMillis
            val hoursUntil = max(0f, (startMillis - currentTimeMillis) / 3600000f)
            val confidence = if (sectorRecords.size >= 2) 94 else 88

            val label = if (pacWindow.isCurrentlyActive) {
                "PAC ${sector.rotationBlock}: Corte activo (${pacWindow.timeLabel})"
            } else {
                "PAC ${sector.rotationBlock}: ${pacWindow.dayName} ${pacWindow.timeLabel}"
            }

            return OutagePrediction(
                sectorId = sector.id,
                sectorName = sector.name,
                rotationBlock = sector.rotationBlock,
                nextEstimatedStartMillis = startMillis,
                nextEstimatedEndMillis = endMillis,
                estimatedDurationHours = calculatedDuration,
                confidencePercentage = confidence,
                algorithmDetail = label,
                hoursUntilWindow = hoursUntil
            )
        }

        // Fallback modular calculation if block is custom or unspecified
        val blockOffset = when (cleanBlock) {
            "A" -> 0
            "B" -> 6
            "C" -> 12
            "D" -> 18
            else -> 0
        }

        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val slotHourBase = ((dayOfYear * 4) + blockOffset) % MODULO_DAY_HOURS

        val candidateCal = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
            timeInMillis = currentTimeMillis
            set(Calendar.HOUR_OF_DAY, slotHourBase)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (candidateCal.timeInMillis <= currentTimeMillis) {
            val tomorrowSlot = (((dayOfYear + 1) * 4) + blockOffset) % MODULO_DAY_HOURS
            candidateCal.add(Calendar.DAY_OF_YEAR, 1)
            candidateCal.set(Calendar.HOUR_OF_DAY, tomorrowSlot)
        }

        val startMillis = candidateCal.timeInMillis
        val endMillis = startMillis + (calculatedDuration * 3600 * 1000).toLong()
        val hoursUntil = max(0f, (startMillis - currentTimeMillis) / 3600000f)

        return OutagePrediction(
            sectorId = sector.id,
            sectorName = sector.name,
            rotationBlock = sector.rotationBlock,
            nextEstimatedStartMillis = startMillis,
            nextEstimatedEndMillis = endMillis,
            estimatedDurationHours = calculatedDuration,
            confidencePercentage = 78,
            algorithmDetail = "PAC Corpoelec Rotación Modular (${sector.rotationBlock})",
            hoursUntilWindow = hoursUntil
        )
    }
}

