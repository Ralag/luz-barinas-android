package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.OutageRecord
import com.example.data.model.ServiceStatus

@Entity(
    tableName = "outage_records",
    indices = [Index(value = ["sectorId", "startTimeMillis"])]
)
data class OutageRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectorId: String,
    val sectorName: String,
    val statusType: String, // SCHEDULED_OUTAGE or IRREGULAR_OUTAGE
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val durationHours: Float,
    val notes: String
) {
    fun toDomain(): OutageRecord {
        val parsedStatus = try {
            ServiceStatus.valueOf(statusType)
        } catch (e: Exception) {
            ServiceStatus.SCHEDULED_OUTAGE
        }
        return OutageRecord(
            id = id,
            sectorId = sectorId,
            sectorName = sectorName,
            statusType = parsedStatus,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            durationHours = durationHours,
            notes = notes
        )
    }
}
