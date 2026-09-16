package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PendingReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingReportDao {
    @Query("SELECT * FROM pending_reports WHERE isSynced = 0 ORDER BY reportedAtMillis ASC")
    suspend fun getUnsyncedReports(): List<PendingReportEntity>

    @Query("SELECT * FROM pending_reports ORDER BY reportedAtMillis DESC")
    fun getAllReportsFlow(): Flow<List<PendingReportEntity>>

    @Query("SELECT COUNT(*) FROM pending_reports WHERE isSynced = 0")
    fun getUnsyncedReportsCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: PendingReportEntity): Long

    @Update
    suspend fun updateReport(report: PendingReportEntity)

    @Query("UPDATE pending_reports SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("DELETE FROM pending_reports WHERE isSynced = 1 AND reportedAtMillis < :olderThanMillis")
    suspend fun deleteOldSyncedReports(olderThanMillis: Long)
}
