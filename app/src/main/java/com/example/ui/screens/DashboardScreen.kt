package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PowerOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.FilledTonalButton
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.PacAlertPrefs
import com.example.data.model.BroadcastNotice
import com.example.data.model.OutagePrediction
import com.example.data.model.PacScheduleData
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import com.example.ui.components.GoogleSearchBar
import com.example.ui.theme.BlockAColor
import com.example.ui.theme.BlockBColor
import com.example.ui.theme.BlockCColor
import com.example.ui.theme.BlockDColor
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.theme.StatusScheduledRed
import java.util.Calendar
import java.util.TimeZone

/**
 * Clean, Fast, Minimalist Dashboard for PAC Barinas.
 * Philosophy: Menos es mejor. All vital schedule and status info at a single glance.
 */
@Composable
fun DashboardScreen(
    sectors: List<Sector>,
    selectedSector: Sector?,
    userAddress: String? = null,
    prediction: OutagePrediction?,
    unsyncedCount: Int,
    activeNotice: BroadcastNotice? = null,
    onDismissNotice: () -> Unit = {},
    onSectorSelected: (Sector) -> Unit,
    onReportStatus: (Boolean, String, Float?) -> Unit,
    onChangeAddressClicked: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onOpenAlarmSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var reportFeedback by remember { mutableStateOf<String?>(null) }

    val userBlock = remember(selectedSector) {
        selectedSector?.rotationBlock?.uppercase()?.replace("BLOQUE", "")?.trim() ?: "A"
    }

    val blockColor = remember(userBlock) {
        when {
            userBlock.contains("A") -> BlockAColor
            userBlock.contains("B") -> BlockBColor
            userBlock.contains("C") -> BlockCColor
            userBlock.contains("D") -> BlockDColor
            else -> BlockAColor
        }
    }

    // Calendar & slot calculation for Barinas timezone (recalculates every minute)
    var timeKey by remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis() / 60_000L) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000L) // updates more frequently than 60s to be safe
            timeKey = System.currentTimeMillis() / 60_000L
        }
    }
    val cal = remember(timeKey) { Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")) }
    val currentDayIdx = remember(timeKey) { PacScheduleData.getDayIndex(cal.get(Calendar.DAY_OF_WEEK)) }
    val currentSlotIdx = remember(timeKey) { PacScheduleData.getCurrentSlotIndex(cal.get(Calendar.HOUR_OF_DAY)) }
    val currentDayOfMonth = remember(timeKey) { cal.get(Calendar.DAY_OF_MONTH) }

    // Check if the current slot has PAC cut for the user's block considering the exact week rotation
    val currentSlotBlock = remember(currentSlotIdx, currentDayIdx, currentDayOfMonth, PacScheduleData.scheduleVersion) {
        PacScheduleData.getBlockForDate(currentSlotIdx, currentDayIdx, currentDayOfMonth)
    }
    val isCurrentSlotOutage = remember(currentSlotBlock, userBlock) {
        currentSlotBlock.equals(userBlock, ignoreCase = true)
    }
    val hasPowerNow = (selectedSector?.status == ServiceStatus.NORMAL) && !isCurrentSlotOutage

    // Next scheduled cut calculation based on monthly rotation
    val nextCutSlotIdx = remember(currentSlotIdx, currentDayIdx, currentDayOfMonth, userBlock, PacScheduleData.scheduleVersion) {
        var found = -1
        for (offset in 1..6) {
            val sIdx = (currentSlotIdx + offset) % 6
            val dIdx = if (sIdx < currentSlotIdx) (currentDayIdx + 1) % 7 else currentDayIdx
            val dayMonth = if (sIdx < currentSlotIdx) currentDayOfMonth + 1 else currentDayOfMonth
            if (PacScheduleData.getBlockForDate(sIdx, dIdx, dayMonth).equals(userBlock, ignoreCase = true)) {
                found = sIdx
                break
            }
        }
        found
    }

    val dayAudit = remember(currentDayIdx, PacScheduleData.scheduleVersion) {
        PacScheduleData.getDayTurnAudit(currentDayIdx)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. Official Broadcast Notice Banner (if active)
            if (activeNotice != null) {
                item(key = "broadcast_notice_banner") {
                    BroadcastNoticeBanner(
                        notice = activeNotice,
                        onDismiss = onDismissNotice,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 1. Search Bar inside LazyColumn (scrolls away naturally when scrolling down)
            item(key = "google_search_bar") {
                GoogleSearchBar(
                    sectors = sectors,
                    onSectorSelected = onSectorSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2. Horario de Hoy (Includes Sector Info and Live Status)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_schedule_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Top Header: Primary Location Title & Block Chip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(blockColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = blockColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = selectedSector?.name ?: userAddress ?: "Barinas",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        if (selectedSector?.isCommunity == true) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF9C27B0).copy(alpha = 0.15f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.3f))
                                            ) {
                                                Text(
                                                    text = "Comunitario \uD83E\uDD1D", // Handshake emoji
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF9C27B0),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    val circuitText = "Circuito ${selectedSector?.circuitCode ?: "Don Samuel"}"
                                    val locationDetail = if (userAddress != null && userAddress != selectedSector?.name) {
                                        "$userAddress • $circuitText"
                                    } else {
                                        "$circuitText • Barinas"
                                    }
                                    Text(
                                        text = locationDetail,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Block & Change button column
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = blockColor.copy(alpha = 0.18f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, blockColor)
                                ) {
                                    Text(
                                        text = "Bloque $userBlock",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = blockColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { onChangeAddressClicked() }
                                ) {
                                    Text(
                                        text = "Cambiar",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Status Banner with clean high-contrast presentation
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (hasPowerNow) StatusNormalGreen.copy(alpha = 0.10f) else StatusScheduledRed.copy(alpha = 0.10f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (hasPowerNow) StatusNormalGreen.copy(alpha = 0.45f) else StatusScheduledRed.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("today_live_status_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            if (hasPowerNow) StatusNormalGreen else StatusScheduledRed,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (hasPowerNow) Icons.Outlined.Bolt else Icons.Outlined.PowerOff,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (hasPowerNow) "🟢 Servicio Eléctrico Activo (Con Luz)" else "🔴 Corte de PAC en progreso",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (hasPowerNow) StatusNormalGreen else StatusScheduledRed
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (hasPowerNow) {
                                            if (prediction != null && prediction.hoursUntilWindow > 0f) {
                                                "Próximo corte: ${prediction.algorithmDetail.substringAfter(": ")}"
                                            } else if (nextCutSlotIdx != -1) {
                                                "Próximo corte estimado: ${PacScheduleData.SLOTS[nextCutSlotIdx].timeLabel}"
                                            } else {
                                                "Sin cortes programados en este turno"
                                            }
                                        } else {
                                            "Turno actual: ${PacScheduleData.SLOTS[currentSlotIdx].timeLabel} (4 hrs)"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                    // Today's 6 PAC Time Slots
                    val dayName = PacScheduleData.DAYS[currentDayIdx].name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cronograma de hoy ($dayName)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = onNavigateToSchedule,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Ver mensual", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PacScheduleData.SLOTS.forEachIndexed { slotIdx, slot ->
                            val blockForSlot = PacScheduleData.getBlockForDate(slotIdx, currentDayIdx, currentDayOfMonth)
                            val isUserBlock = blockForSlot.equals(userBlock, ignoreCase = true)
                            val isNow = slotIdx == currentSlotIdx

                            val slotBlockColor = when (blockForSlot) {
                                "A" -> BlockAColor
                                "B" -> BlockBColor
                                "C" -> BlockCColor
                                "D" -> BlockDColor
                                else -> BlockAColor
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isNow -> (if (isUserBlock) StatusScheduledRed else StatusNormalGreen).copy(alpha = 0.12f)
                                    isUserBlock -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when {
                                        isNow -> if (isUserBlock) StatusScheduledRed else StatusNormalGreen
                                        isUserBlock -> StatusScheduledRed.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = slot.timeLabel,
                                            fontSize = 13.sp,
                                            fontWeight = if (isNow || isUserBlock) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isNow) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isUserBlock) StatusScheduledRed else StatusNormalGreen
                                            ) {
                                                Text(
                                                    text = "AHORA",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Bloque $blockForSlot",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = slotBlockColor
                                        )

                                        if (isUserBlock) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(Corte)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StatusScheduledRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2.5 Quick Alarms & Notification Settings Card
        item(key = "quick_alarm_card") {
            val context = LocalContext.current
            val alertSettings = remember { PacAlertPrefs.getSettings(context) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAlarmSettings() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (alertSettings.isAlarmEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (alertSettings.isAlarmEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (alertSettings.isAlarmEnabled) Icons.Default.Alarm else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (alertSettings.isAlarmEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (alertSettings.isAlarmEnabled) "🔔 Alarma PAC Activada" else "⏰ Alerta de Corte Programada",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Aviso ${alertSettings.advanceMinutes} min antes del corte • Toca para ajustar",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onOpenAlarmSettings,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Ajustar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Quick Citizen Report Buttons (Minimalist 1-tap)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "¿Tienes luz en casa ahora?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Reporte rápido para confirmar el estado en tu comunidad.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onReportStatus(true, "NORMAL", 118f)
                                reportFeedback = "¡Gracias! Reportaste servicio con luz."
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("report_power_on_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Bolt, contentDescription = "Tengo luz", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tengo luz", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                onReportStatus(false, "SIN_LUZ", 0f)
                                reportFeedback = "Reporte guardado: Sin luz."
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("report_power_off_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusScheduledRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.PowerOff, contentDescription = "Se fue la luz", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Se fue la luz", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    AnimatedVisibility(
                        visible = reportFeedback != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = "Confirmado",
                                tint = StatusNormalGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reportFeedback ?: "",
                                fontSize = 12.sp,
                                color = StatusNormalGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick Shortcut to Monthly PAC Calendar
        item {
            OutlinedButton(
                onClick = onNavigateToSchedule,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("view_full_schedule_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "Horarios", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Horarios Semanales y Mensuales", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
}

@Composable
fun BroadcastNoticeBanner(
    notice: BroadcastNotice,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmergency = notice.level.equals("EMERGENCY", ignoreCase = true)
    val isWarning = notice.level.equals("WARNING", ignoreCase = true)

    val containerColor = when {
        isEmergency -> Color(0xFF381212)
        isWarning -> Color(0xFF332008)
        else -> Color(0xFF0C2436)
    }

    val borderColor = when {
        isEmergency -> Color(0xFFFF5252).copy(alpha = 0.5f)
        isWarning -> Color(0xFFFFB300).copy(alpha = 0.5f)
        else -> Color(0xFF40C4FF).copy(alpha = 0.5f)
    }

    val iconTint = when {
        isEmergency -> Color(0xFFFF5252)
        isWarning -> Color(0xFFFFB300)
        else -> Color(0xFF40C4FF)
    }

    val tagText = when {
        isEmergency -> "🚨 AVISO DE EMERGENCIA"
        isWarning -> "⚠️ ALERTA PAC OFICIAL"
        else -> "📢 COMUNICADO OFICIAL"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tagText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconTint
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar aviso",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (notice.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notice.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

