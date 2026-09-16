package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.OutageRecordDao
import com.example.data.local.dao.PendingReportDao
import com.example.data.local.dao.SectorDao
import com.example.data.local.entity.OutageRecordEntity
import com.example.data.local.entity.PendingReportEntity
import com.example.data.local.entity.SectorEntity

@Database(
    entities = [
        SectorEntity::class,
        OutageRecordEntity::class,
        PendingReportEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sectorDao(): SectorDao
    abstract fun outageRecordDao(): OutageRecordDao
    abstract fun pendingReportDao(): PendingReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "luz_barinas.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
