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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.FilledTonalButton
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    sectors: List<Sector>,
    selectedSector: Sector?,
    userAddress: String? = null,
    prediction: OutagePrediction?,
    unsyncedCount: Int,
    activeNotice: BroadcastNotice? = null,
    activeProfile: String = "home",
    hasWorkProfile: Boolean = false,
    onSwitchProfile: (String) -> Unit = {},
    onDismissNotice: () -> Unit = {},
    onSectorSelected: (Sector) -> Unit,
    onReportStatus: (Boolean, String, Float?, String?) -> Unit,
    onChangeAddressClicked: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onOpenAlarmSettings: () -> Unit = {},
    onWatchTutorial: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    scheduleVersion: Long = PacScheduleData.scheduleVersion
) {
    val configuration = LocalConfiguration.current
    val compactActions = configuration.screenWidthDp < 360 || configuration.fontScale > 1.3f
    var reportFeedback by remember { mutableStateOf<String?>(null) }
    var observationText by remember { mutableStateOf("") }
    var showObservationInput by remember { mutableStateOf(false) }
    com.example.ads.AppOpenInteractionProtection(showObservationInput || observationText.isNotBlank())

    val userBlock = remember(selectedSector) {
        com.example.data.model.PacBlock.normalize(selectedSector?.rotationBlock) ?: ""
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

    var timeKey by remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis() / 60_000L) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10_000L)
            timeKey = System.currentTimeMillis() / 60_000L
        }
    }
    val now = remember(timeKey) { System.currentTimeMillis() }
    val cal = remember(timeKey) { Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply { timeInMillis = now } }
    val currentDayIdx = PacScheduleData.getDayIndex(cal.get(Calendar.DAY_OF_WEEK))
    val currentDayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    val schedule = remember(scheduleVersion) { PacScheduleData.getScheduleSnapshot() }
    val activeWindow = remember(timeKey, scheduleVersion, userBlock) {
        PacScheduleData.findScheduledWindowForBlock(userBlock, now)?.takeIf { it.isCurrentlyActive }
    }
    val nextWindow = remember(timeKey, scheduleVersion, userBlock) {
        PacScheduleData.findScheduledWindowForBlock(userBlock, now, includeActive = false)
    }
    val currentSlotIdx = PacScheduleData.getCurrentSlotIndex(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    val activeSlotStartsToday = currentSlotIdx >= 0 && schedule.slots.getOrNull(currentSlotIdx)?.let { slot ->
        slot.startHour < slot.endHour || cal.get(Calendar.HOUR_OF_DAY) >= slot.startHour
    } == true
    val irregular = selectedSector?.status == ServiceStatus.IRREGULAR_OUTAGE
    val hasPowerNow = selectedSector?.status == ServiceStatus.NORMAL
    val liveStatusColor = when {
        irregular -> Color(0xFF9333EA)
        selectedSector?.status == ServiceStatus.SCHEDULED_OUTAGE -> StatusScheduledRed
        hasPowerNow -> StatusNormalGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusTitle = when {
        irregular -> "Falla irregular reportada"
        selectedSector?.status == ServiceStatus.SCHEDULED_OUTAGE -> "Sin luz reportado por la comunidad"
        hasPowerNow -> "Con luz según el último reporte"
        else -> "Sin reporte actual del servicio"
    }
    val statusDetail = if (activeWindow != null) "PAC en curso: ${activeWindow.timeLabel}" else
        nextWindow?.let { "Próximo PAC: ${it.dayName}, ${it.timeLabel}" } ?: "Sin cortes publicados para este bloque"

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

            // Profile Switcher (Home / Work) if they have a work profile configured, or an "Add Work Profile" button
            item(key = "profile_switcher") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (hasWorkProfile) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                Button(
                                    onClick = { onSwitchProfile("home") },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeProfile == "home") MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (activeProfile == "home") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text("🏠 Casa", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = { onSwitchProfile("work") },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeProfile == "work") MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (activeProfile == "work") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text("💼 Trabajo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { 
                                // To configure work profile, we switch to "work" and open address dialog
                                onSwitchProfile("work")
                                onChangeAddressClicked() 
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.heightIn(min = 48.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("💼 Agregar Ubicación de Trabajo", fontSize = 13.sp)
                        }
                    }
                }
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
                        // Reserve the entire card width for the sector name. The
                        // original nested Row measured it before the block chip.
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = blockColor)
                            Spacer(Modifier.width(8.dp))
                            Text("Tu sector", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            TextButton(onClick = onChangeAddressClicked) { Text("Cambiar") }
                        }
                        Text(
                            text = selectedSector?.name ?: userAddress ?: "Elige tu sector",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().testTag("dashboard_sector_name")
                        )
                        val locationDetail = selectedSector?.circuitCode?.let { "Circuito $it" } ?: "Consulta un sector para personalizar tu horario"
                        Text(locationDetail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = blockColor.copy(alpha = 0.15f)) {
                                Text(if (userBlock.isEmpty()) "Sin bloque asignado" else "Bloque $userBlock", style = MaterialTheme.typography.labelLarge, color = blockColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                            selectedSector?.let {
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(if (it.isCommunity) "Comunitario" else "Sector PAC", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Status Banner with clean high-contrast presentation
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = liveStatusColor.copy(alpha = 0.10f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                liveStatusColor.copy(alpha = 0.45f)
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
                                            liveStatusColor,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (hasPowerNow) Icons.Outlined.Bolt else Icons.Outlined.PowerOff,
                                        contentDescription = statusTitle,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = statusTitle,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = liveStatusColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = statusDetail,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                    // Slots beginning today; an overnight active window is described above.
                    val dayName = PacScheduleData.DAYS[currentDayIdx].name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cronograma de hoy ($dayName)",
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = onNavigateToSchedule,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Ver más", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Ir al horario mensual", modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        schedule.slots.forEachIndexed { slotIdx, slot ->
                            val blockForSlot = schedule.matrix[(slotIdx + PacScheduleData.getWeekOffsetForDayOfMonth(currentDayOfMonth)) % schedule.matrix.size][currentDayIdx]
                            val isUserBlock = blockForSlot.equals(userBlock, ignoreCase = true)
                            val isNow = activeSlotStartsToday && slotIdx == currentSlotIdx

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
                    .clickable(role = androidx.compose.ui.semantics.Role.Button) { onOpenAlarmSettings() },
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
                                contentDescription = if (alertSettings.isAlarmEnabled) "Alarma activada" else "Notificaciones activadas",
                                tint = if (alertSettings.isAlarmEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(Modifier.weight(1f)) {
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

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = if (compactActions) 1 else 3
                    ) {
                        Button(
                            onClick = {
                                val obs = observationText.trim().ifEmpty { null }
                                onReportStatus(true, "NORMAL", null, obs)
                                reportFeedback = "¡Gracias! Reportaste servicio con luz."
                                observationText = ""
                                showObservationInput = false
                            },
                            modifier = Modifier
                                .then(if (compactActions) Modifier.fillMaxWidth() else Modifier.weight(1f))
                                .heightIn(min = 48.dp)
                                .testTag("report_power_on_button")
                                .semantics { contentDescription = "Reportar que tengo servicio eléctrico" },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Outlined.Bolt, contentDescription = "Con luz", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Con Luz", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val obs = observationText.trim().ifEmpty { null }
                                onReportStatus(false, "SIN_LUZ", 0f, obs)
                                reportFeedback = "Reporte guardado: Sin luz (Corte)."
                                observationText = ""
                                showObservationInput = false
                            },
                            modifier = Modifier
                                .then(if (compactActions) Modifier.fillMaxWidth() else Modifier.weight(1f))
                                .heightIn(min = 48.dp)
                                .testTag("report_power_off_button")
                                .semantics { contentDescription = "Reportar que no hay servicio eléctrico" },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusScheduledRed),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Outlined.PowerOff, contentDescription = "Sin luz", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sin Luz", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val obs = observationText.trim().ifEmpty { "Avería o falla fuera de horario" }
                                onReportStatus(false, "FALLA_IRREGULAR", 0f, obs)
                                reportFeedback = "Reporte guardado: Falla Irregular."
                                observationText = ""
                                showObservationInput = false
                            },
                            modifier = Modifier
                                .then(if (compactActions) Modifier.fillMaxWidth() else Modifier.weight(1f))
                                .heightIn(min = 48.dp)
                                .testTag("report_irregular_fault_button")
                                .semantics { contentDescription = "Reportar falla irregular o avería fuera de cronograma" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Falla irregular", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Falla irregular", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Optional Observation Toggle
                    TextButton(
                        onClick = { showObservationInput = !showObservationInput },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (showObservationInput) "▲ Ocultar observación" else "➕ Agregar observación comunitaria (opcional)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(visible = showObservationInput) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickChips = listOf(
                                "Explosión de transformador",
                                "Guaya caída",
                                "Fase caída / 110V",
                                "Fluctuación / Bajón"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickChips.take(2).forEach { chip ->
                                    androidx.compose.material3.FilterChip(
                                        selected = observationText == chip,
                                        onClick = { observationText = if (observationText == chip) "" else chip },
                                        label = { Text(chip, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickChips.drop(2).forEach { chip ->
                                    androidx.compose.material3.FilterChip(
                                        selected = observationText == chip,
                                        onClick = { observationText = if (observationText == chip) "" else chip },
                                        label = { Text(chip, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            androidx.compose.material3.OutlinedTextField(
                                value = observationText,
                                onValueChange = { observationText = it },
                                label = { Text("Detalle u observación de la falla") },
                                placeholder = { Text("Ej: Explotó transformador cerca de la cancha...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
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
                    .heightIn(min = 48.dp)
                    .testTag("view_full_schedule_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "Horarios", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Horarios Semanales y Mensuales", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        onWatchTutorial?.let { watch ->
            item(key = "youtube_tutorial") {
                OutlinedButton(onClick = watch, modifier = Modifier.fillMaxWidth().testTag("dashboard_tutorial")) {
                    Text("Ver tutorial de la aplicación")
                }
            }
        }

        // 5. Disclaimer & Official Sources Footer Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Aviso Legal y Fuentes Oficiales",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "PAC Barinas es una plataforma comunitaria independiente. No representa a CORPOELEC, al MPPEE ni a entes gubernamentales. Cronogramas referenciales obtenidos de fuentes oficiales públicas (corpoelec.gob.ve).",
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
        isWarning -> "⚠️ ALERTA DE CRONOGRAMA PAC"
        else -> "📢 COMUNICADO INFORMATIVO"
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
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Aviso informativo",
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
                    modifier = Modifier.size(48.dp)
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


