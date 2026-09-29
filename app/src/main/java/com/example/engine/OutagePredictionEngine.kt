package com.example.engine

import com.example.data.model.OutagePrediction
import com.example.data.model.OutageRecord
import com.example.data.model.PacScheduleData
import com.example.data.model.Sector
import com.example.data.repository.SupabaseCitizenReport
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.max

object OutagePredictionEngine {

    private const val DEFAULT_OUTAGE_DURATION_HOURS = 4.0f
    private const val MODULO_DAY_HOURS = 24

    suspend fun calculateNextOutageWindow(
        sector: Sector,
        history: List<OutageRecord>,
        weeklyReports: List<SupabaseCitizenReport> = emptyList(),
        currentTimeMillis: Long = System.currentTimeMillis()
    ): OutagePrediction {
        return calculateLocalOutageWindow(sector, history, weeklyReports, currentTimeMillis)
    }

    fun calculateLocalOutageWindow(
        sector: Sector,
        history: List<OutageRecord>,
        weeklyReports: List<SupabaseCitizenReport> = emptyList(),
        currentTimeMillis: Long = System.currentTimeMillis()
    ): OutagePrediction {
        val tz = TimeZone.getTimeZone("America/Caracas")
        val calendar = Calendar.getInstance(tz).apply {
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
        val theoreticalPacWindow = if (cleanBlock in listOf("A", "B", "C", "D")) {
            PacScheduleData.findNextWindowForBlock(cleanBlock, currentTimeMillis)
        } else null

        // Nuevo Motor Predictivo Ciudadano:
        // Analiza los reportes de la última semana.
        // Si hay una fuerte tendencia de "Sin Luz" (hasPower = false) en un bloque horario distinto al teórico,
        // la predicción se ajustará.
        
        var adjustedStartMillis = theoreticalPacWindow?.startMillis ?: -1L
        var confidence = 75
        var algorithmDetail = "Teórico PAC (" + sector.rotationBlock + ")"

        if (weeklyReports.isNotEmpty()) {
            // Agrupar reportes "Sin Luz" por hora del día.
            val outageReports = weeklyReports.filter { !it.hasPower }
            val hourCounts = mutableMapOf<Int, Int>()
            
            for (report in outageReports) {
                val cal = Calendar.getInstance(tz).apply { timeInMillis = report.timestamp }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                hourCounts[hour] = (hourCounts[hour] ?: 0) + 1
            }
            
            // Encontrar la hora con mayor concentración de cortes que difiera de la teoría
            val peakHourEntry = hourCounts.maxByOrNull { it.value }
            if (peakHourEntry != null && peakHourEntry.value >= 3) {
                // Hay un patrón ciudadano fuerte
                val peakHour = peakHourEntry.key
                
                // Comparar con el inicio del bloque teórico (si existe)
                val theoreticalCal = Calendar.getInstance(tz).apply {
                    if (theoreticalPacWindow != null) timeInMillis = theoreticalPacWindow.startMillis
                }
                val theoreticalHour = theoreticalCal.get(Calendar.HOUR_OF_DAY)
                
                // Si la realidad se desvía por más de 1 hora de la teoría, ajustar.
                if (theoreticalPacWindow == null || Math.abs(theoreticalHour - peakHour) > 1) {
                    val candidateCal = Calendar.getInstance(tz).apply {
                        timeInMillis = currentTimeMillis
                        set(Calendar.HOUR_OF_DAY, peakHour)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (candidateCal.timeInMillis <= currentTimeMillis) {
                        candidateCal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    
                    // Solo ajustar si la nueva estimación está cerca (próximas 24h)
                    if (candidateCal.timeInMillis < currentTimeMillis + 24 * 3600000L) {
                        adjustedStartMillis = candidateCal.timeInMillis
                        algorithmDetail = "Ajuste Ciudadano (Realidad > Teoría)"
                        confidence = minOf(99, 70 + (peakHourEntry.value * 2))
                    }
                }
            }
        }

        // Fallback al cálculo básico si no se determinó ventana
        if (adjustedStartMillis == -1L) {
            val blockOffset = when (cleanBlock) {
                "A" -> 0
                "B" -> 6
                "C" -> 12
                "D" -> 18
                else -> 0
            }

            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val slotHourBase = ((dayOfYear * 4) + blockOffset) % MODULO_DAY_HOURS

            val candidateCal = Calendar.getInstance(tz).apply {
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
            adjustedStartMillis = candidateCal.timeInMillis
            algorithmDetail = "PAC Rotación ($cleanBlock)"
            confidence = 78
        } else if (algorithmDetail.startsWith("Teórico") && theoreticalPacWindow != null) {
            confidence = if (sectorRecords.size >= 2) 94 else 88
            algorithmDetail = if (theoreticalPacWindow.isCurrentlyActive) {
                "PAC ${sector.rotationBlock}: Corte en curso (${theoreticalPacWindow.timeLabel})"
            } else {
                "PAC ${sector.rotationBlock}: ${theoreticalPacWindow.dayName} • ${theoreticalPacWindow.timeLabel}"
            }
        }

        val endMillis = adjustedStartMillis + (calculatedDuration * 3600 * 1000).toLong()
        val hoursUntil = max(0f, (adjustedStartMillis - currentTimeMillis) / 3600000f)

        return OutagePrediction(
            sectorId = sector.id,
            sectorName = sector.name,
            rotationBlock = sector.rotationBlock,
            nextEstimatedStartMillis = adjustedStartMillis,
            nextEstimatedEndMillis = endMillis,
            estimatedDurationHours = calculatedDuration,
            confidencePercentage = confidence,
            algorithmDetail = algorithmDetail,
            hoursUntilWindow = hoursUntil
        )
    }
}
