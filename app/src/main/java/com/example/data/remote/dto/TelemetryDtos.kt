package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SectorDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "circuit_code") val circuitCode: String,
    @Json(name = "status") val status: String,
    @Json(name = "voltage") val voltage: Float,
    @Json(name = "confirmed_reports") val confirmedReports: Int,
    @Json(name = "without_power_percentage") val withoutPowerPercentage: Int,
    @Json(name = "last_updated") val lastUpdated: Long,
    @Json(name = "rotation_block") val rotationBlock: String,
    @Json(name = "polygon_points") val polygonPoints: String
)

@JsonClass(generateAdapter = true)
data class TelemetryReportRequest(
    @Json(name = "sector_id") val sectorId: String,
    @Json(name = "has_power") val hasPower: Boolean,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "report_type") val reportType: String,
    @Json(name = "voltage_reading") val voltageReading: Float? = null,
    @Json(name = "observation") val observation: String? = null,
    @Json(name = "client_device_id") val clientDeviceId: String = "barinas_citizen_app"
)

@JsonClass(generateAdapter = true)
data class TelemetryReportResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String,
    @Json(name = "updated_sector_status") val updatedSectorStatus: String?,
    @Json(name = "total_sector_reports") val totalSectorReports: Int?
)
