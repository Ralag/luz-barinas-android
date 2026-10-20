package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportPowerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getInstance(applicationContext)
        val pendingDao = database.pendingReportDao()
        // Transport the saved event only. Reapplying its optimistic state here
        // used to erase irregular faults and increment counters a second time.

        // Process all unsynced reports in Room
        val unsyncedList = pendingDao.getUnsyncedReports()
        val cloudSync = com.example.data.repository.CloudSyncRepository(applicationContext, database)
        var allSucceeded = true

        for (report in unsyncedList) {
            try {
                val success = cloudSync.uploadCitizenReport(
                    sectorId = report.sectorId,
                    sectorName = report.sectorName,
                    hasPower = report.hasPower,
                    reportType = report.reportType,
                    voltage = report.voltageObserved,
                    observation = report.observation,
                    reportedAtMillis = report.reportedAtMillis,
                    localReportId = report.id
                )

                if (success) {
                    pendingDao.markAsSynced(report.id)
                } else {
                    pendingDao.updateReport(report.copy(retryCount = report.retryCount + 1))
                    allSucceeded = false
                }
            } catch (e: Exception) {
                pendingDao.updateReport(report.copy(retryCount = report.retryCount + 1))
                allSucceeded = false
            }
        }

        if (allSucceeded) {
            Result.success()
        } else {
            // WorkManager will automatically retry with exponential backoff when connectivity returns
            Result.retry()
        }
    }

    companion object {
        const val KEY_SECTOR_ID = "key_sector_id"
        const val KEY_HAS_POWER = "key_has_power"
        const val KEY_REPORT_TYPE = "key_report_type"
        const val KEY_VOLTAGE = "key_voltage"
        const val WORK_NAME = "sync_power_telemetry_work"
    }
}
