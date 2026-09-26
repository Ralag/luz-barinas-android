package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.CitizenReport

@Entity(
    tableName = "pending_reports",
    indices = [Index(value = ["isSynced", "reportedAtMillis"])]
)
data class PendingReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectorId: String,
    val sectorName: String,
    val hasPower: Boolean,
    val reportedAtMillis: Long,
    val reportType: String,
    val voltageObserved: Float?,
    val observation: String? = null,
    val isSynced: Boolean = false,
    val retryCount: Int = 0
) {
    fun toDomain(): CitizenReport = CitizenReport(
        id = id,
        sectorId = sectorId,
        sectorName = sectorName,
        hasPower = hasPower,
        reportedAtMillis = reportedAtMillis,
        reportType = reportType,
        voltageObserved = voltageObserved,
        observation = observation,
        isSynced = isSynced
    )
}
