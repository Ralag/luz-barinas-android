package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PendingReportEntity
import com.example.worker.ReportPowerWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sectorId = intent.getStringExtra(EXTRA_SECTOR_ID) ?: "sec_a_alto_barinas_1"
        val sectorName = intent.getStringExtra(EXTRA_SECTOR_NAME) ?: "Alto Barinas 1"
        val hasPower = intent.getBooleanExtra(EXTRA_HAS_POWER, true)
        val reportType = if (hasPower) "NORMAL" else "SIN_LUZ"
        val voltage = if (hasPower) 118.0f else 0.0f

        // Dismiss the alert notification immediately
        NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIFICATION_ID_ALERT)

        // Show brief confirmation
        NotificationHelper.showConfirmationNotification(context, sectorName, hasPower)

        // Save report into local Room DB immediately (Offline-First)
        // Coroutine on Dispatchers.IO
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val newEntity = PendingReportEntity(
                    sectorId = sectorId,
                    sectorName = sectorName,
                    hasPower = hasPower,
                    reportedAtMillis = System.currentTimeMillis(),
                    reportType = reportType,
                    voltageObserved = voltage,
                    isSynced = false
                )
                db.pendingReportDao().insertReport(newEntity)

                // Optimistically update sector in Room
                val sector = db.sectorDao().getSectorById(sectorId)
                if (sector != null) {
                    val updatedStatus = if (hasPower) "NORMAL" else "SCHEDULED_OUTAGE"
                    val newCount = sector.confirmedReportsCount + 1
                    val newPct = if (hasPower) {
                        (sector.withoutPowerPercentage - 12).coerceAtLeast(0)
                    } else {
                        (sector.withoutPowerPercentage + 18).coerceAtMost(100)
                    }
                    db.sectorDao().updateSector(
                        sector.copy(
                            status = updatedStatus,
                            voltage = voltage,
                            confirmedReportsCount = newCount,
                            withoutPowerPercentage = newPct,
                            lastUpdatedMillis = System.currentTimeMillis()
                        )
                    )
                }

                // Enqueue background sync with WorkManager
                val inputData = Data.Builder()
                    .putString(ReportPowerWorker.KEY_SECTOR_ID, sectorId)
                    .putBoolean(ReportPowerWorker.KEY_HAS_POWER, hasPower)
                    .putString(ReportPowerWorker.KEY_REPORT_TYPE, reportType)
                    .putFloat(ReportPowerWorker.KEY_VOLTAGE, voltage)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<ReportPowerWorker>()
                    .setInputData(inputData)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    ReportPowerWorker.WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    workRequest
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_POWER_ON = "com.example.luzbarinas.ACTION_POWER_ON"
        const val ACTION_POWER_OFF = "com.example.luzbarinas.ACTION_POWER_OFF"
        const val EXTRA_SECTOR_ID = "extra_sector_id"
        const val EXTRA_SECTOR_NAME = "extra_sector_name"
        const val EXTRA_HAS_POWER = "extra_has_power"
    }
}
