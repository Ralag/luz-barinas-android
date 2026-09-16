package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.OutageRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutageRecordDao {
    @Query("SELECT * FROM outage_records WHERE sectorId = :sectorId ORDER BY startTimeMillis DESC LIMIT :limit")
    suspend fun getRecentOutagesBySector(sectorId: String, limit: Int = 15): List<OutageRecordEntity>

    @Query("SELECT * FROM outage_records ORDER BY startTimeMillis DESC")
    fun getAllOutages(): Flow<List<OutageRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutages(records: List<OutageRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutage(record: OutageRecordEntity)

    @Query("SELECT COUNT(*) FROM outage_records")
    suspend fun getCount(): Int
}
