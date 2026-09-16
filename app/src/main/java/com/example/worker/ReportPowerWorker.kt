package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.remote.ApiClient
import com.example.data.remote.dto.TelemetryReportRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportPowerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getInstance(applicationContext)
        val pendingDao = database.pendingReportDao()
        val sectorDao = database.sectorDao()

        // Check if individual report params were passed
        val sectorId = inputData.getString(KEY_SECTOR_ID)
        val hasPower = inputData.getBoolean(KEY_HAS_POWER, true)
        val reportType = inputData.getString(KEY_REPORT_TYPE) ?: if (hasPower) "NORMAL" else "SIN_LUZ"
        val voltage = inputData.getFloat(KEY_VOLTAGE, if (hasPower) 118f else 0f)

        // If specific params passed, ensure it is in Room or process it
        if (sectorId != null) {
            // Also update local Sector entity to give instant optimistic offline response
            val sector = sectorDao.getSectorById(sectorId)
            if (sector != null) {
                val newStatus = if (hasPower) "NORMAL" else "SCHEDULED_OUTAGE"
                val newVoltage = if (hasPower) 118f else 0f
                val newCount = sector.confirmedReportsCount + 1
                val newNoPowerPct = if (hasPower) {
                    (sector.withoutPowerPercentage - 10).coerceAtLeast(0)
                } else {
                    (sector.withoutPowerPercentage + 15).coerceAtMost(100)
                }
                sectorDao.updateSector(
                    sector.copy(
                        status = newStatus,
                        voltage = newVoltage,
                        confirmedReportsCount = newCount,
                        withoutPowerPercentage = newNoPowerPct,
                        lastUpdatedMillis = System.currentTimeMillis()
                    )
                )
            }
        }

        // Process all unsynced reports in Room
        val unsyncedList = pendingDao.getUnsyncedReports()
        var allSucceeded = true

        for (report in unsyncedList) {
            try {
                val request = TelemetryReportRequest(
                    sectorId = report.sectorId,
                    hasPower = report.hasPower,
                    timestamp = report.reportedAtMillis,
                    reportType = report.reportType,
                    voltageReading = report.voltageObserved
                )

                // Try to send via Retrofit
                val response = ApiClient.api.submitPowerReport(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    pendingDao.markAsSynced(report.id)
                } else {
                    // In Barinas offline circumstances, server might return 404 or connection reset
                    // Keep in Room for next retry
                    pendingDao.updateReport(report.copy(retryCount = report.retryCount + 1))
                    allSucceeded = false
                }
            } catch (e: Exception) {
                // Network unreachable or timeout (common during blackout)
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
