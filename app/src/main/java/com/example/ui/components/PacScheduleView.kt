package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PacScheduleData
import com.example.data.model.PacSlot
import com.example.data.model.PacWeekPlan
import com.example.data.model.Sector
import com.example.ui.theme.BlockAColor
import com.example.ui.theme.BlockBColor
import com.example.ui.theme.BlockCColor
import com.example.ui.theme.BlockDColor
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.theme.StatusScheduledRed
import java.util.Calendar
import java.util.TimeZone

/**
 * Authentic, clean PAC Schedule screen following the #SOYBARINAS Corpoelec graphic layout.
 * Displays Semanal, Mensual (4 weeks matrix), and Bloques/Circuitos tables.
 */
@Composable
fun PacScheduleView(
    selectedSector: Sector?,
    modifier: Modifier = Modifier,
    onSectorClicked: (String) -> Unit = {},
    pacMatrix: List<List<String>> = PacScheduleData.getMatrixSnapshot(),
    pacSlots: List<PacSlot> = PacScheduleData.getSlotsSnapshot()
) {
    val cal = remember { Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")) }
    val todayIdx = remember { PacScheduleData.getDayIndex(cal.get(Calendar.DAY_OF_WEEK)) }
    val currentSlotIdx = remember { PacScheduleData.getCurrentSlotIndex(cal.get(Calendar.HOUR_OF_DAY)) }

    var selectedDayIdx by remember { mutableIntStateOf(todayIdx) }
    var mainTab by remember { mutableIntStateOf(0) } // 0: Semanal, 1: Mensual, 2: Bloques
    val currentMonthIdx = remember { cal.get(Calendar.MONTH) }
    var selectedMonthIdx by remember { mutableIntStateOf(currentMonthIdx) }

    val userBlock = remember(selectedSector) {
        selectedSector?.rotationBlock?.uppercase()?.replace("BLOQUE", "")?.trim() ?: "A"
    }

    val currentYear = remember { cal.get(Calendar.YEAR) }
    val todayDayOfMonth = remember { cal.get(Calendar.DAY_OF_MONTH) }

    val currentWeekNumber = remember(todayDayOfMonth) {
        when (todayDayOfMonth) {
            in 1..7 -> 1
            in 8..14 -> 2
            in 15..21 -> 3
            else -> 4
        }
    }

    val selectedMonthName = remember(selectedMonthIdx) {
        PacScheduleData.MONTH_NAMES.getOrElse(selectedMonthIdx) { "Mes" }
    }

    val selectedMonthDays = remember(selectedMonthIdx, currentYear) {
        val tempCal = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas")).apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, selectedMonthIdx)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val monthlyWeeks: List<PacWeekPlan> by produceState(initialValue = emptyList(), key1 = selectedMonthName, key2 = selectedMonthDays, key3 = PacScheduleData.scheduleVersion) {
        value = try {
            val response = com.example.data.remote.ApiClient.api.getMonthlyWeeks(selectedMonthName, selectedMonthDays)
            if (response.isSuccessful) {
                response.body()?.weeks ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("pac_schedule_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Horarios PAC Barinas",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Plan de Administración de Carga • $selectedMonthName $currentYear",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
            }
        }

        // Main Tab Switcher (Semanal, Mensual, Bloques)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    // Tab 0: Semanal
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (mainTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (mainTab == 0) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { mainTab = 0 }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.ViewWeek,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (mainTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Semanal",
                                fontWeight = if (mainTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (mainTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Tab 1: Mensual
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (mainTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (mainTab == 1) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { mainTab = 1 }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (mainTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mensual",
                                fontWeight = if (mainTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (mainTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Tab 2: Bloques
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (mainTab == 2) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (mainTab == 2) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { mainTab = 2 }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.ViewAgenda,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (mainTab == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bloques",
                                fontWeight = if (mainTab == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (mainTab == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB CONTENT
        // =========================================================================
        item {
            AnimatedContent(
                targetState = mainTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "schedule_tab_transition"
            ) { tab ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (tab) {
                        0 -> {
                            PacOfficialMatrixCard(
                                title = "SEMANA EN CURSO • ROTACIÓN PAC",
                                matrix = pacMatrix,
                                slots = pacSlots,
                                highlightToday = true,
                                todayIdx = todayIdx,
                                currentSlotIdx = currentSlotIdx,
                                userBlock = userBlock,
                                onDayHeaderClicked = { selectedDayIdx = it }
                            )

                            Column {
                                Text(
                                    text = "Detalle diario para ${selectedSector?.name ?: "Barinas"}:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    itemsIndexed(PacScheduleData.DAYS, key = { _, day -> day.name }) { index, day ->
                                        val isSelected = selectedDayIdx == index
                                        val isToday = index == todayIdx

                                        Surface(
                                            onClick = { selectedDayIdx = index },
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = day.shortName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isToday) {
                                                    Text(
                                                        text = "Hoy",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            val selectedDay = PacScheduleData.DAYS[selectedDayIdx]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Turnos para el ${selectedDay.name}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = getBlockColor(userBlock).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Tu Bloque: $userBlock",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = getBlockColor(userBlock),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        pacSlots.forEachIndexed { slotIdx, slot ->
                                            val block = pacMatrix.getOrNull(slotIdx)?.getOrNull(selectedDayIdx) ?: "A"
                                            val isCutForUser = block.equals(userBlock, ignoreCase = true)
                                            val isNow = selectedDayIdx == todayIdx && slotIdx == currentSlotIdx
                                            val blockColor = getBlockColor(block)

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = when {
                                                    isNow -> blockColor.copy(alpha = 0.12f)
                                                    isCutForUser -> MaterialTheme.colorScheme.surfaceVariant
                                                    else -> MaterialTheme.colorScheme.surface
                                                },
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    when {
                                                        isNow -> blockColor
                                                        isCutForUser -> blockColor.copy(alpha = 0.5f)
                                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                                    }
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .background(blockColor, RoundedCornerShape(8.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = block,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp,
                                                                color = Color.White
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text(
                                                                text = slot.timeLabel,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 13.sp,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = if (isCutForUser) "🔴 Corte PAC asignado a tu bloque" else "🟢 Servicio eléctrico con luz garantizada",
                                                                fontSize = 11.sp,
                                                                color = if (isCutForUser) StatusScheduledRed else StatusNormalGreen
                                                            )
                                                        }
                                                    }

                                                    if (isNow) {
                                                        Surface(
                                                            shape = RoundedCornerShape(100.dp),
                                                            color = StatusScheduledRed.copy(alpha = 0.15f)
                                                        ) {
                                                            Text(
                                                                text = "EN CURSO",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = StatusScheduledRed,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                        1 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Cronograma mensual oficial organizado por semanas y bloques para $selectedMonthName $currentYear. Las rotaciones avanzan de forma cíclica durante el mes.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Selecciona el Mes:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    itemsIndexed(PacScheduleData.MONTH_NAMES, key = { _, monthName -> monthName }) { index, monthName ->
                                        val isSelected = selectedMonthIdx == index
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedMonthIdx = index },
                                            label = {
                                                Text(
                                                    text = monthName,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            monthlyWeeks.forEach { week ->
                                val isCurrentWeek = (selectedMonthIdx == currentMonthIdx && week.weekNumber == currentWeekNumber)
                                PacOfficialMatrixCard(
                                    title = week.dateRangeLabel,
                                    matrix = week.matrix,
                                    slots = pacSlots,
                                    highlightToday = isCurrentWeek,
                                    todayIdx = if (isCurrentWeek) todayIdx else -1,
                                    currentSlotIdx = if (isCurrentWeek) currentSlotIdx else -1,
                                    userBlock = userBlock
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "— ASIGNACIÓN DE CIRCUITOS POR BLOQUE —",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            PacBlockSectorsGrid(onSectorClicked = onSectorClicked)
                        }
                        2 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Circuitos y Sectores del Estado Barinas",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Consulta a qué bloque de corte pertenece tu urbanización, barrio o circuito eléctrico. Toca cualquier sector para seleccionarlo.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            PacBlockSectorsGrid(onSectorClicked = onSectorClicked)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders the clean bordered matrix table corresponding to each week in image.png.
 */
@Composable
fun PacOfficialMatrixCard(
    title: String,
    modifier: Modifier = Modifier,
    matrix: List<List<String>> = PacScheduleData.getMatrixSnapshot(),
    slots: List<PacSlot> = PacScheduleData.getSlotsSnapshot(),
    highlightToday: Boolean = false,
    todayIdx: Int = -1,
    currentSlotIdx: Int = -1,
    userBlock: String? = null,
    onDayHeaderClicked: ((Int) -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Date range banner (matching image.png e.g. "DEL 03 AL 09 DE AGOSTO")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Scrollable Matrix Table
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                // Header Row: HORARIO | LUNES | MARTES | MIERCOLES | JUEVES | VIERNES | SABADO | DOMINGO
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.width(88.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "HORARIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    PacScheduleData.DAYS.forEachIndexed { dayIdx, day ->
                        val isToday = highlightToday && dayIdx == todayIdx
                        val isSelected = highlightToday && dayIdx == currentSlotIdx // Just a hack, wait.
                        Box(
                            modifier = Modifier
                                .width(46.dp)
                                .clickable { onDayHeaderClicked?.invoke(dayIdx) }
                                .then(
                                    if (isToday) Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        RoundedCornerShape(4.dp)
                                    ) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.shortName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isToday) {
                                    Text(
                                        text = "HOY",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Slot Rows
                for (slotIdx in 0 until minOf(6, slots.size)) {
                    val slot = slots[slotIdx]
                    val isCurrentSlot = highlightToday && slotIdx == currentSlotIdx

                    Row(
                        modifier = Modifier
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                            .then(
                                if (isCurrentSlot) Modifier.background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(6.dp)
                                ) else Modifier
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Slot Label (e.g. 03:00 a 07:00 or short 03-07)
                        Box(
                            modifier = Modifier.width(88.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = slot.timeLabel,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrentSlot) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrentSlot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Day Cells (A, B, C, D)
                        for (dayIdx in 0..6) {
                            val block = matrix.getOrNull(slotIdx)?.getOrNull(dayIdx) ?: "A"
                            val blockColor = getBlockColor(block)
                            val isCellNow = highlightToday && dayIdx == todayIdx && slotIdx == currentSlotIdx
                            val isUserTarget = userBlock != null && block.equals(userBlock, ignoreCase = true)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = blockColor,
                                border = if (isCellNow) {
                                    androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                                } else if (isUserTarget && highlightToday && dayIdx == todayIdx) {
                                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface)
                                } else null,
                                shadowElevation = if (isCellNow) 3.dp else 0.dp,
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(width = 42.dp, height = 36.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = block,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend below matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Pair("A", BlockAColor),
                    Pair("B", BlockBColor),
                    Pair("C", BlockCColor),
                    Pair("D", BlockDColor)
                ).forEach { (code, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bloque $code",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4 Blocks Sector Card Grid matching the bottom half of image.png.
 */
@Composable
fun PacBlockSectorsGrid(
    modifier: Modifier = Modifier,
    onSectorClicked: (String) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Bloque A & Bloque B
        BlockSectorsCard(
            blockName = "BLOQUE A",
            headerColor = BlockAColor,
            sectors = PacScheduleData.SECTORS_BLOQUE_A,
            onSectorClicked = onSectorClicked
        )

        BlockSectorsCard(
            blockName = "BLOQUE B",
            headerColor = BlockBColor,
            sectors = PacScheduleData.SECTORS_BLOQUE_B,
            onSectorClicked = onSectorClicked
        )

        // Bloque C & Bloque D
        BlockSectorsCard(
            blockName = "BLOQUE C",
            headerColor = BlockCColor,
            sectors = PacScheduleData.SECTORS_BLOQUE_C,
            onSectorClicked = onSectorClicked
        )

        BlockSectorsCard(
            blockName = "BLOQUE D",
            headerColor = BlockDColor,
            sectors = PacScheduleData.SECTORS_BLOQUE_D,
            onSectorClicked = onSectorClicked,
            note = "Sectores temporalmente consolidados en rotación técnica."
        )
    }
}

/**
 * Single Block Card styled identically to the bordered boxes in image.png.
 */
@Composable
fun BlockSectorsCard(
    blockName: String,
    headerColor: Color,
    sectors: List<String>,
    modifier: Modifier = Modifier,
    onSectorClicked: (String) -> Unit = {},
    note: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, headerColor.copy(alpha = 0.8f))
    ) {
        Column {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = blockName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            if (note != null) {
                Text(
                    text = "ℹ️ $note",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // 2-Column table list of sectors with borders (memoized to prevent jank)
            val leftCol = remember(sectors) { sectors.filterIndexed { index, _ -> index % 2 == 0 } }
            val rightCol = remember(sectors) { sectors.filterIndexed { index, _ -> index % 2 == 1 } }
            val totalRowCount = remember(leftCol, rightCol) { maxOf(leftCol.size, rightCol.size) }

            var isExpanded by remember { mutableStateOf(false) }
            val displayRowCount = if (isExpanded || totalRowCount <= 8) totalRowCount else 8

            Column(modifier = Modifier.padding(6.dp)) {
                for (i in 0 until displayRowCount) {
                    val leftSector = leftCol.getOrNull(i)
                    val rightSector = rightCol.getOrNull(i)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        // Left Column Cell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = leftSector != null) {
                                    leftSector?.let { onSectorClicked(it) }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = leftSector?.uppercase() ?: "",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(0.5.dp)
                                .height(26.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        )

                        // Right Column Cell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = rightSector != null) {
                                    rightSector?.let { onSectorClicked(it) }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = rightSector?.uppercase() ?: "",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (totalRowCount > 8) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isExpanded) "▲ Mostrar menos" else "▼ Ver todos los sectores (${sectors.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Returns color corresponding to each block (A, B, C, D).
 */
fun getBlockColor(block: String): Color {
    return when (block.uppercase().trim()) {
        "A" -> BlockAColor
        "B" -> BlockBColor
        "C" -> BlockCColor
        "D" -> BlockDColor
        else -> Color(0xFF6366F1)
    }
}
