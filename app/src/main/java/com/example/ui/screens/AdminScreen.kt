package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PacScheduleData
import com.example.data.model.Sector
import com.example.ui.theme.BlockAColor
import com.example.ui.theme.BlockBColor
import com.example.ui.theme.BlockCColor
import com.example.ui.theme.BlockDColor
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.theme.StatusScheduledRed

import com.example.data.model.PacSlot

@Composable
fun AdminScreen(
    sectors: List<Sector>,
    scheduleVersion: Long,
    pacMatrix: List<List<String>> = PacScheduleData.getMatrixSnapshot(),
    pacSlots: List<PacSlot> = PacScheduleData.getSlotsSnapshot(),
    sectorsA: List<String> = emptyList(),
    sectorsB: List<String> = emptyList(),
    sectorsC: List<String> = emptyList(),
    sectorsD: List<String> = emptyList(),
    onUpdateMatrixCell: (slotIdx: Int, dayIdx: Int, block: String) -> Unit,
    onUpdateSlot: (slotIdx: Int, newLabel: String, startHour: Int, endHour: Int) -> Unit = { _, _, _, _ -> },
    onDeleteSlot: (slotIdx: Int) -> Unit = {},
    onAddSlot: (newLabel: String, startHour: Int, endHour: Int) -> Unit = { _, _, _ -> },
    onApplyDoubleTurnPreset: () -> Unit = {},
    onApplySingleTurnPreset: () -> Unit = {},
    onDeleteTurnAndRebalance: (slotIdx: Int, dayIdx: Int) -> Unit = { _, _ -> },
    onAddSectorToBlock: (sectorName: String, blockCode: String) -> Unit = { _, _ -> },
    onRemoveSectorFromBlock: (sectorName: String, blockCode: String) -> Unit = { _, _ -> },
    onReassignSector: (sectorName: String, targetBlock: String) -> Unit,
    onResetToDefault: () -> Unit,
    onPublishNotification: () -> Unit,
    onCloseAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("admin_security_prefs", Context.MODE_PRIVATE) }
    var selectedSectorToMove by remember { mutableStateOf<Sector?>(null) }
    var newPasswordInput by remember { mutableStateOf("") }
    var passwordSavedMessage by remember { mutableStateOf<String?>(null) }

    // Slot Editing Dialog State
    var editingSlotIdx by remember { mutableStateOf<Int?>(null) }
    var editSlotLabel by remember { mutableStateOf("") }
    var editSlotStartHour by remember { mutableIntStateOf(0) }
    var editSlotEndHour by remember { mutableIntStateOf(4) }

    // Add New Slot Dialog State
    var showAddSlotDialog by remember { mutableStateOf(false) }
    var newSlotLabelInput by remember { mutableStateOf("") }
    var newSlotStartHourInput by remember { mutableIntStateOf(12) }
    var newSlotEndHourInput by remember { mutableIntStateOf(16) }

    // Matrix Cell Action Dialog State (Delete & Rebalance, Manual Shift)
    var selectedCellForAction by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Add Sector to Block State
    var newSectorName by remember { mutableStateOf("") }
    var newSectorBlock by remember { mutableStateOf("A") }

    // Active Block Filter for Management Tab
    var activeBlockManagement by remember { mutableStateOf("A") }

    val matrix = pacMatrix

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("admin_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with Close Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Panel de Administrador",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Configuración dinámica de PAC y bloques",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onCloseAdmin,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Schedule Slots Editor (Actualizar Horarios / 2 Cortes al Día)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("schedule_slots_editor_card"),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Actualizar Franjas Horarias",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Personaliza las horas de los turnos o activa el esquema de 2 cortes diarios.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val isSingleCutActive = remember(matrix) {
                        val maxCutsInDay = (0..6).maxOfOrNull { c ->
                            val counts = mutableMapOf<String, Int>()
                            for (r in 0 until minOf(6, matrix.size)) {
                                val b = matrix[r].getOrElse(c) { "" }
                                if (b.isNotEmpty() && b != "-") counts[b] = (counts[b] ?: 0) + 1
                            }
                            counts.values.maxOrNull() ?: 0
                        } ?: 2
                        maxCutsInDay <= 1
                    }

                    // Presets Row: 2 Cortes (8h) vs 1 Corte (4h)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onApplyDoubleTurnPreset,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (!isSingleCutActive) {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            } else {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                            }
                        ) {
                            Icon(Icons.Outlined.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (!isSingleCutActive) "⚡ Esquema: 2 Cortes Diarios (8 hrs) • [ACTIVO]" else "⚡ Esquema: 2 Cortes Diarios (8 hrs por bloque)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onApplySingleTurnPreset,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (isSingleCutActive) {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            } else {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                            }
                        ) {
                            Icon(Icons.Outlined.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSingleCutActive) "⚡ Esquema: 1 Solo Corte Diario (4 hrs) • [ACTIVO]" else "⚡ Esquema: 1 Solo Corte Diario (4 hrs por bloque)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Turnos configurados:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // List of Slots with Edit Button
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        pacSlots.forEachIndexed { idx, slot ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Turno ${idx + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = slot.timeLabel,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${slot.startHour}:00 a ${slot.endHour}:00 hrs",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                editingSlotIdx = idx
                                                editSlotLabel = slot.timeLabel
                                                editSlotStartHour = slot.startHour
                                                editSlotEndHour = slot.endHour
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = { onDeleteSlot(idx) },
                                            enabled = pacSlots.size > 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar Turno", tint = StatusScheduledRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = {
                                newSlotLabelInput = "Turno ${pacSlots.size + 1}"
                                showAddSlotDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agregar Franja Horaria / Turno", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Manual Matrix Editor
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_matrix_editor_card"),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✏️ Editor de Matriz de Turnos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toca cualquier celda para rotar: Bloque A ➔ B ➔ C ➔ D",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = onResetToDefault,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restaurar", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable 6 slots x 7 days table
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                    ) {
                        // Header Row (Days)
                        Row(modifier = Modifier.padding(bottom = 6.dp)) {
                            Box(modifier = Modifier.width(90.dp)) {
                                Text(
                                    "HORARIO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            for (day in PacScheduleData.DAYS) {
                                Box(
                                    modifier = Modifier.width(46.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.shortName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // 6 Slot Rows
                        for (slotIdx in 0 until minOf(6, pacSlots.size)) {
                            val slot = pacSlots[slotIdx]
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.width(90.dp)) {
                                    Text(
                                        text = slot.timeLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                for (dayIdx in 0..6) {
                                    val currentBlock = matrix.getOrNull(slotIdx)?.getOrNull(dayIdx) ?: "A"
                                    val blockColor = when (currentBlock) {
                                        "A" -> BlockAColor
                                        "B" -> BlockBColor
                                        "C" -> BlockCColor
                                        "D" -> BlockDColor
                                        else -> MaterialTheme.colorScheme.primary
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = blockColor.copy(alpha = 0.20f),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, blockColor),
                                        modifier = Modifier
                                            .padding(horizontal = 2.dp)
                                            .size(42.dp)
                                            .clickable {
                                                selectedCellForAction = Pair(slotIdx, dayIdx)
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = currentBlock,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = blockColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Block Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(
                            Triple("A", "Bloque A", BlockAColor),
                            Triple("B", "Bloque B", BlockBColor),
                            Triple("C", "Bloque C", BlockCColor),
                            Triple("D", "Bloque D", BlockDColor)
                        ).forEach { (code, name, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(color, RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // 4. Gestión Completa de Bloques y Sectores (Agregar, Eliminar, Transferir)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manage_blocks_and_sectors_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏢 Gestión de Bloques y Circuitos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Agrega nuevos sectores o transfiérelos entre bloques según el nuevo esquema.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Agregar nuevo sector
                    Text("Agregar Nuevo Sector a Bloque:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSectorName,
                            onValueChange = { newSectorName = it },
                            placeholder = { Text("Nombre del sector o circuito", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        // Block selector
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("A", "B", "C", "D").forEach { b ->
                                val isSelected = newSectorBlock == b
                                val bColor = when (b) {
                                    "A" -> BlockAColor
                                    "B" -> BlockBColor
                                    "C" -> BlockCColor
                                    else -> BlockDColor
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) bColor else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { newSectorBlock = b }
                                ) {
                                    Text(
                                        text = b,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (newSectorName.isNotBlank()) {
                                    onAddSectorToBlock(newSectorName.trim(), newSectorBlock)
                                    newSectorName = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            enabled = newSectorName.isNotBlank()
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "Agregar", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Block Tabs to manage existing sectors
                    Text("Explorar y Reasignar Sectores por Bloque:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("A", "B", "C", "D").forEach { b ->
                            val isSelected = activeBlockManagement == b
                            val bColor = when (b) {
                                "A" -> BlockAColor
                                "B" -> BlockBColor
                                "C" -> BlockCColor
                                else -> BlockDColor
                            }
                            val listA = if (sectorsA.isNotEmpty()) sectorsA else PacScheduleData.SECTORS_BLOQUE_A
                            val listB = if (sectorsB.isNotEmpty()) sectorsB else PacScheduleData.SECTORS_BLOQUE_B
                            val listC = if (sectorsC.isNotEmpty()) sectorsC else PacScheduleData.SECTORS_BLOQUE_C
                            val listD = if (sectorsD.isNotEmpty()) sectorsD else PacScheduleData.SECTORS_BLOQUE_D

                            val count = when (b) {
                                "A" -> listA.size
                                "B" -> listB.size
                                "C" -> listC.size
                                else -> listD.size
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) bColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { activeBlockManagement = b }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Bloque $b",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "($count)",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedContent(
                        targetState = activeBlockManagement,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "block_transition"
                    ) { block ->
                        val listA = if (sectorsA.isNotEmpty()) sectorsA else PacScheduleData.SECTORS_BLOQUE_A
                        val listB = if (sectorsB.isNotEmpty()) sectorsB else PacScheduleData.SECTORS_BLOQUE_B
                        val listC = if (sectorsC.isNotEmpty()) sectorsC else PacScheduleData.SECTORS_BLOQUE_C
                        val listD = if (sectorsD.isNotEmpty()) sectorsD else PacScheduleData.SECTORS_BLOQUE_D

                        val activeSectors = when (block) {
                            "A" -> listA
                            "B" -> listB
                            "C" -> listC
                            else -> listD
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            activeSectors.forEach { secName ->
                                Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = secName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Move to other blocks buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("A", "B", "C", "D").filter { it != activeBlockManagement }.forEach { targetB ->
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                                                modifier = Modifier.clickable {
                                                    onReassignSector(secName, targetB)
                                                }
                                            ) {
                                                Text(
                                                    text = "➔ $targetB",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { onRemoveSectorFromBlock(secName, activeBlockManagement) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.Delete,
                                                contentDescription = "Eliminar",
                                                tint = StatusScheduledRed,
                                                modifier = Modifier.size(14.dp)
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
    }

        // 5. Publish PAC Notification to Community
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Difusión Comunitaria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Envía una alerta a los dispositivos de los ciudadanos notificando la publicación del nuevo cronograma PAC.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onPublishNotification,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Emitir Alerta de Nuevo PAC", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // 6. Change Secret Admin Password
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clave de Acceso Secreto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cambia la clave requerida para ingresar a este panel secreto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it },
                            placeholder = { Text("Nueva clave secreta", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                if (newPasswordInput.isNotBlank()) {
                                    prefs.edit().putString("master_key", newPasswordInput.trim()).apply()
                                    passwordSavedMessage = "¡Clave maestra actualizada exitosamente!"
                                    newPasswordInput = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            enabled = newPasswordInput.isNotBlank()
                        ) {
                            Text("Guardar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(
                        visible = passwordSavedMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = passwordSavedMessage ?: "",
                            fontSize = 12.sp,
                            color = StatusNormalGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialog to Edit a Time Slot
    if (editingSlotIdx != null) {
        val slotIdx = editingSlotIdx!!
        AlertDialog(
            onDismissRequest = { editingSlotIdx = null },
            title = {
                Text(
                    text = "Editar Turno ${slotIdx + 1}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editSlotLabel,
                        onValueChange = { editSlotLabel = it },
                        label = { Text("Etiqueta de Horario") },
                        placeholder = { Text("Ej: 00:00 a 04:00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editSlotStartHour.toString(),
                            onValueChange = {
                                editSlotStartHour = it.toIntOrNull()?.coerceIn(0, 23) ?: 0
                            },
                            label = { Text("Hora Inicio (0-23)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = editSlotEndHour.toString(),
                            onValueChange = {
                                editSlotEndHour = it.toIntOrNull()?.coerceIn(0, 24) ?: 4
                            },
                            label = { Text("Hora Fin (0-24)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSlot(slotIdx, editSlotLabel.trim(), editSlotStartHour, editSlotEndHour)
                        editingSlotIdx = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSlotIdx = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog to Add a New Time Slot
    if (showAddSlotDialog) {
        AlertDialog(
            onDismissRequest = { showAddSlotDialog = false },
            title = {
                Text("➕ Agregar Nueva Franja Horaria", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newSlotLabelInput,
                        onValueChange = { newSlotLabelInput = it },
                        label = { Text("Etiqueta de Horario") },
                        placeholder = { Text("Ej: 03:00 a 07:00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newSlotStartHourInput.toString(),
                            onValueChange = {
                                newSlotStartHourInput = it.toIntOrNull()?.coerceIn(0, 23) ?: 0
                            },
                            label = { Text("Hora Inicio (0-23)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newSlotEndHourInput.toString(),
                            onValueChange = {
                                newSlotEndHourInput = it.toIntOrNull()?.coerceIn(0, 24) ?: 4
                            },
                            label = { Text("Hora Fin (0-24)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSlotLabelInput.isNotBlank()) {
                            onAddSlot(newSlotLabelInput.trim(), newSlotStartHourInput, newSlotEndHourInput)
                            showAddSlotDialog = false
                        }
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSlotDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog to Delete & Rebalance or Edit a Matrix Cell
    if (selectedCellForAction != null) {
        val (slotIdx, dayIdx) = selectedCellForAction!!
        val slot = pacSlots.getOrElse(slotIdx) { PacScheduleData.SLOTS[0] }
        val day = PacScheduleData.DAYS[dayIdx]
        val currentBlock = matrix.getOrNull(slotIdx)?.getOrNull(dayIdx) ?: "A"
        val dayAudit = PacScheduleData.getDayTurnAudit(dayIdx)

        val blockColor = when (currentBlock) {
            "A" -> BlockAColor
            "B" -> BlockBColor
            "C" -> BlockCColor
            "D" -> BlockDColor
            else -> MaterialTheme.colorScheme.primary
        }

        AlertDialog(
            onDismissRequest = { selectedCellForAction = null },
            icon = {
                Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = {
                Text(
                    text = "${day.name} • ${slot.timeLabel}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Current Block status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estado actual:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = blockColor.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, blockColor)
                        ) {
                            Text(
                                text = if (currentBlock == "-") "Sin Corte (Libre)" else "Bloque $currentBlock",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = blockColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Direct Block Assignment
                    Text("Asignar bloque o dejar libre:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("A", "B", "C", "D", "-").forEach { b ->
                            val isSelected = currentBlock == b
                            val bLabel = if (b == "-") "Libre" else b
                            val bColor = when (b) {
                                "A" -> BlockAColor
                                "B" -> BlockBColor
                                "C" -> BlockCColor
                                "D" -> BlockDColor
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) bColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onUpdateMatrixCell(slotIdx, dayIdx, b)
                                        selectedCellForAction = null
                                    }
                            ) {
                                Text(
                                    text = bLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    // Quick Actions (Rotate / Clear)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val next = when (currentBlock) {
                                    "A" -> "B"
                                    "B" -> "C"
                                    "C" -> "D"
                                    "D" -> "-"
                                    else -> "A"
                                }
                                onUpdateMatrixCell(slotIdx, dayIdx, next)
                                selectedCellForAction = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🔄 Rotar Bloque", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onUpdateMatrixCell(slotIdx, dayIdx, "-")
                                selectedCellForAction = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen)
                        ) {
                            Text("🟢 Quitar Corte", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Smart Delete & Rebalance
                    Button(
                        onClick = {
                            onDeleteTurnAndRebalance(slotIdx, dayIdx)
                            selectedCellForAction = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusScheduledRed)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rebalanceo Automático Inteligente",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "Auditoría de hoy: ${dayAudit.detectedScheme} (A:${dayAudit.cutsPerBlock["A"] ?: 0} B:${dayAudit.cutsPerBlock["B"] ?: 0} C:${dayAudit.cutsPerBlock["C"] ?: 0} D:${dayAudit.cutsPerBlock["D"] ?: 0})",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedCellForAction = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

