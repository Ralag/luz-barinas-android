package com.example.glance

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PendingReportEntity
import com.example.worker.ReportPowerWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LuzBarinasWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getInstance(context)
        val sectorDao = database.sectorDao()
        val defaultSector = withContext(Dispatchers.IO) {
            sectorDao.getSectorById("sec_a_alto_barinas_1")
        }

        val sectorName = defaultSector?.name ?: "Alto Barinas 1"
        val statusText = when (defaultSector?.status) {
            "SCHEDULED_OUTAGE" -> "🔴 Corte Programado (PAC)"
            "IRREGULAR_OUTAGE" -> "🟣 Corte Irregular (Avería)"
            else -> "🟢 Servicio Activo (Con Luz)"
        }
        val voltageText = if (defaultSector?.status == "NORMAL") "${defaultSector.voltage.toInt()} V" else "0 V"

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color(0xFF0F172A)))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = "⚡ Luz Barinas",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFF59E0B)),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = sectorName,
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Text(
                            text = voltageText,
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF38BDF8)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    Text(
                        text = statusText,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFE2E8F0)),
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(10.dp))

                    // 1-Click interactive reporting buttons
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            text = "🟢 Tengo Luz",
                            onClick = actionRunCallback<ReportPowerActionCallback>(
                                actionParametersOf(
                                    ActionParameters.Key<Boolean>("has_power") to true,
                                    ActionParameters.Key<String>("sector_id") to (defaultSector?.id ?: "sec_a_alto_barinas_1")
                                )
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        Button(
                            text = "🔴 Sin Luz",
                            onClick = actionRunCallback<ReportPowerActionCallback>(
                                actionParametersOf(
                                    ActionParameters.Key<Boolean>("has_power") to false,
                                    ActionParameters.Key<String>("sector_id") to (defaultSector?.id ?: "sec_a_alto_barinas_1")
                                )
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }
                }
            }
        }
    }
}

class ReportPowerActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val hasPower = parameters[ActionParameters.Key<Boolean>("has_power")] ?: true
        val sectorId = parameters[ActionParameters.Key<String>("sector_id")] ?: "sec_a_alto_barinas_1"
        val sectorName = "Alto Barinas 1"
        val reportType = if (hasPower) "NORMAL" else "SIN_LUZ"
        val voltage = if (hasPower) 118.0f else 0.0f

        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            // Insert local report
            val report = PendingReportEntity(
                sectorId = sectorId,
                sectorName = sectorName,
                hasPower = hasPower,
                reportedAtMillis = System.currentTimeMillis(),
                reportType = reportType,
                voltageObserved = voltage,
                isSynced = false
            )
            db.pendingReportDao().insertReport(report)

            // Update sector state in Room
            val sector = db.sectorDao().getSectorById(sectorId)
            if (sector != null) {
                val updatedStatus = if (hasPower) "NORMAL" else "SCHEDULED_OUTAGE"
                val newCount = sector.confirmedReportsCount + 1
                val newPct = if (hasPower) {
                    (sector.withoutPowerPercentage - 10).coerceAtLeast(0)
                } else {
                    (sector.withoutPowerPercentage + 15).coerceAtMost(100)
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

            // Enqueue sync via WorkManager
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
        }

        // Refresh all widgets
        LuzBarinasWidget().updateAll(context)
    }
}

class LuzBarinasWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LuzBarinasWidget()
}
