package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus

@Entity(tableName = "sectors")
data class SectorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val circuitCode: String,
    val status: String, // NORMAL, SCHEDULED_OUTAGE, IRREGULAR_OUTAGE
    val voltage: Float,
    val confirmedReportsCount: Int,
    val withoutPowerPercentage: Int,
    val lastUpdatedMillis: Long,
    val rotationBlock: String,
    val polygonPointsRaw: String, // "lat,lng;lat,lng;..."
    val isCommunity: Boolean = false
) {
    fun toDomain(): Sector {
        val parsedCoords = polygonPointsRaw.split(";").mapNotNull { pair ->
            val parts = pair.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) Pair(lat, lng) else null
            } else null
        }

        val parsedStatus = try {
            ServiceStatus.valueOf(status)
        } catch (e: Exception) {
            ServiceStatus.NORMAL
        }

        return Sector(
            id = id,
            name = name,
            circuitCode = circuitCode,
            status = parsedStatus,
            voltage = voltage,
            confirmedReportsCount = confirmedReportsCount,
            withoutPowerPercentage = withoutPowerPercentage,
            lastUpdatedMillis = lastUpdatedMillis,
            rotationBlock = rotationBlock,
            coordinates = parsedCoords,
            isCommunity = isCommunity
        )
    }
}
