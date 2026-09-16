package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SectorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectorDao {
    @Query("SELECT * FROM sectors ORDER BY name ASC")
    fun getAllSectors(): Flow<List<SectorEntity>>

    @Query("SELECT * FROM sectors ORDER BY name ASC")
    suspend fun getAllSectorsList(): List<SectorEntity>

    @Query("SELECT * FROM sectors WHERE id = :id LIMIT 1")
    suspend fun getSectorById(id: String): SectorEntity?

    @Query("SELECT * FROM sectors WHERE id = :id LIMIT 1")
    fun observeSectorById(id: String): Flow<SectorEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSectors(sectors: List<SectorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSector(sector: SectorEntity)

    @Update
    suspend fun updateSector(sector: SectorEntity)

    @Query("SELECT COUNT(*) FROM sectors")
    suspend fun getSectorsCount(): Int
}
