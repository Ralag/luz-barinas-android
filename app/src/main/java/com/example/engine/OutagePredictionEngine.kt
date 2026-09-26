package com.example.engine

import com.example.data.model.OutagePrediction
import com.example.data.model.OutageRecord
import com.example.data.model.PacScheduleData
import com.example.data.model.Sector
import com.example.data.remote.ApiClient
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.max

object OutagePredictionEngine {

    private const val DEFAULT_OUTAGE_DURATION_HOURS = 4.0f
    private const val MODULO_DAY_HOURS = 24

    /**
     * Calls the 'getOutagePrediction' Serverless Function to get the estimated next outage
     * from the backend, falling back to local PAC schedule calculation if the backend is unreachable or returns an error.
     */
    suspend fun calculateNextOutageWindow(
        sector: Sector,
        history: List<OutageRecord>,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): OutagePrediction? {
        try {
            val response = ApiClient.api.getOutagePrediction(sector.id)
            if (response.isSuccessful && response.body() != null) {
                return response.body()
            }
        } catch (_: Exception) {
            // Backend offline or unreachable; fall back to local schedule estimation
        }
        return calculateLocalOutageWindow(sector, history, currentTimeMillis)
    }

    /**
     * Calculates the estimated next outage window locally using PAC rotation matrix and historical records.
     */
    fun calculateLocalOutageWindow(
        sector: Sector,
        history: List<OutageRecord>,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): OutagePrediction {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
            timeInMillis = currentTimeMillis
        }

        val sectorRecords = history
            .filter { it.sectorId == sector.id }
            .sortedByDescending { it.startTimeMillis }

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
            val startMillis = pacWindow.startMillis
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
            algorithmDetail = "PAC Rotación (${sector.rotationBlock})",
            hoursUntilWindow = hoursUntil
        )
    }
}
