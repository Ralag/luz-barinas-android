package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.PowerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import com.example.ui.components.InteractiveBarinasMap
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusIrregularPurple
import com.example.ui.theme.StatusNormalGreen
import com.example.ui.theme.StatusScheduledRed

@Composable
fun MapScreen(
    sectors: List<Sector>,
    selectedSector: Sector?,
    activeFilter: ServiceStatus?,
    onSectorSelected: (Sector) -> Unit,
    onFilterChanged: (ServiceStatus?) -> Unit,
    onQuickReport: (Boolean, String, Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredSectors = remember(sectors, activeFilter) {
        if (activeFilter == null) {
            sectors
        } else {
            sectors.filter { it.status == activeFilter }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("map_screen")
    ) {
        // Map Title & Description
        Column {
            Text(
                text = "Mapa de Red Eléctrica",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Topología de circuitos y subestaciones de Barinas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips (Todos, Normal, Programado, Avería)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChipItem(
                    label = "Todos (${sectors.size})",
                    color = MaterialTheme.colorScheme.primary,
                    isSelected = activeFilter == null,
                    onClick = { onFilterChanged(null) }
                )
            }
            item {
                FilterChipItem(
                    label = "Normal",
                    color = StatusNormalGreen,
                    isSelected = activeFilter == ServiceStatus.NORMAL,
                    onClick = { onFilterChanged(ServiceStatus.NORMAL) }
                )
            }
            item {
                FilterChipItem(
                    label = "Corte PAC",
                    color = StatusScheduledRed,
                    isSelected = activeFilter == ServiceStatus.SCHEDULED_OUTAGE,
                    onClick = { onFilterChanged(ServiceStatus.SCHEDULED_OUTAGE) }
                )
            }
            item {
                FilterChipItem(
                    label = "Avería",
                    color = StatusIrregularPurple,
                    isSelected = activeFilter == ServiceStatus.IRREGULAR_OUTAGE,
                    onClick = { onFilterChanged(ServiceStatus.IRREGULAR_OUTAGE) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Barinas Map Canvas
        InteractiveBarinasMap(
            sectors = filteredSectors,
            selectedSector = selectedSector,
            onSectorSelected = onSectorSelected,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Sector Hero Details Card (Migrated from Dashboard)
        if (selectedSector != null) {
            val hasPower = selectedSector.status == ServiceStatus.NORMAL

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("map_sector_detail_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top row: Sector name and Circuit details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedSector.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Circuito ${selectedSector.circuitCode} • ${selectedSector.rotationBlock} • Subestación Barinas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = selectedSector.status)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hero Live Status Row
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (hasPower) StatusNormalGreen.copy(alpha = 0.10f) else StatusScheduledRed.copy(alpha = 0.10f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (hasPower) StatusNormalGreen.copy(alpha = 0.35f) else StatusScheduledRed.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (hasPower) StatusNormalGreen else StatusScheduledRed,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasPower) Icons.Outlined.Bolt else Icons.Outlined.PowerOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (hasPower) "Hay luz ahora" else "Corte PAC en progreso",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (hasPower) StatusNormalGreen else StatusScheduledRed
                                )
                                Text(
                                    text = if (hasPower) "Suministro estable en este sector" else "Interrupción programada Corpoelec",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Telemetry Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Tensión", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (hasPower) "${selectedSector.voltage.toInt()} V" else "0 V",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasPower) StatusNormalGreen else StatusScheduledRed
                            )
                        }

                        Column {
                            Text("Consenso", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${100 - selectedSector.withoutPowerPercentage}% con luz",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSector.withoutPowerPercentage > 50) StatusScheduledRed else StatusNormalGreen
                            )
                        }

                        Column {
                            Text("Reportes hoy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${selectedSector.confirmedReportsCount}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1-Tap Report Buttons on Map
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onQuickReport(true, "NORMAL", 118f) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen)
                        ) {
                            Icon(Icons.Outlined.Bolt, contentDescription = "Con Luz", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Con Luz", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onQuickReport(false, "SIN_LUZ", 0f) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusScheduledRed)
                        ) {
                            Icon(Icons.Outlined.PowerOff, contentDescription = "Sin Luz", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sin Luz", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun FilterChipItem(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
