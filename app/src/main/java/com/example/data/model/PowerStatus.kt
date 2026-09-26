package com.example.data.model

import androidx.compose.runtime.Immutable

enum class ServiceStatus(val label: String, val hexColor: Long) {
    NORMAL("Servicio Normal", 0xFF10B981),
    SCHEDULED_OUTAGE("Corte Programado (PAC)", 0xFFEF4444),
    IRREGULAR_OUTAGE("Corte Irregular (Avería)", 0xFF9333EA)
}

@Immutable
data class Sector(
    val id: String,
    val name: String,
    val circuitCode: String,
    val status: ServiceStatus,
    val voltage: Float,
    val confirmedReportsCount: Int,
    val withoutPowerPercentage: Int,
    val lastUpdatedMillis: Long,
    val rotationBlock: String, // Bloque A, B, C, D
    val coordinates: List<Pair<Double, Double>>, // Polygons for the circuit in Barinas
    val isCommunity: Boolean = false
)

data class OutageRecord(
    val id: Long = 0,
    val sectorId: String,
    val sectorName: String,
    val statusType: ServiceStatus,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val durationHours: Float,
    val notes: String
)

@Immutable
data class CitizenReport(
    val id: Long = 0,
    val sectorId: String,
    val sectorName: String,
    val hasPower: Boolean,
    val reportedAtMillis: Long,
    val reportType: String, // "NORMAL", "SIN_LUZ", "BAJON", "FALLA_IRREGULAR"
    val voltageObserved: Float?,
    val observation: String? = null,
    val isSynced: Boolean = false
)

@Immutable
data class OutagePrediction(
    val sectorId: String,
    val sectorName: String,
    val rotationBlock: String,
    val nextEstimatedStartMillis: Long,
    val nextEstimatedEndMillis: Long,
    val estimatedDurationHours: Float,
    val confidencePercentage: Int,
    val algorithmDetail: String,
    val hoursUntilWindow: Float
)
