package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import com.example.ui.components.InteractiveBarinasMap
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
    val filteredSectors = remember(sectors, activeFilter) { sectors.filter { activeFilter == null || it.status == activeFilter } }
    Column(modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp).testTag("map_screen")) {
        Text("Mapa comunitario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Bloque en el pin · estado en la insignia · ubicación aproximada con borde punteado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            item { FilterChipItem("Todos (${sectors.size})", MaterialTheme.colorScheme.primary, activeFilter == null) { onFilterChanged(null) } }
            item { FilterChipItem("Con luz", StatusNormalGreen, activeFilter == ServiceStatus.NORMAL) { onFilterChanged(ServiceStatus.NORMAL) } }
            item { FilterChipItem("Sin luz", StatusScheduledRed, activeFilter == ServiceStatus.SCHEDULED_OUTAGE) { onFilterChanged(ServiceStatus.SCHEDULED_OUTAGE) } }
            item { FilterChipItem("Falla irregular", StatusIrregularPurple, activeFilter == ServiceStatus.IRREGULAR_OUTAGE) { onFilterChanged(ServiceStatus.IRREGULAR_OUTAGE) } }
        }
        Spacer(Modifier.height(8.dp))
        InteractiveBarinasMap(filteredSectors, selectedSector, onSectorSelected, Modifier.weight(1f).fillMaxWidth())
        if (selectedSector != null) {
            Spacer(Modifier.height(8.dp))
            val statusColor = when (selectedSector.status) {
                ServiceStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                ServiceStatus.NORMAL -> StatusNormalGreen
                ServiceStatus.SCHEDULED_OUTAGE -> StatusScheduledRed
                ServiceStatus.IRREGULAR_OUTAGE -> StatusIrregularPurple
            }
            val statusText = when (selectedSector.status) {
                ServiceStatus.UNKNOWN -> "Sin reporte actual"
                ServiceStatus.NORMAL -> "Con luz · estado registrado"
                ServiceStatus.SCHEDULED_OUTAGE -> "Sin luz / corte registrado"
                ServiceStatus.IRREGULAR_OUTAGE -> "Falla irregular reportada"
            }
            Card(
                modifier = Modifier.fillMaxWidth().heightIn(max = 205.dp).testTag("map_sector_detail_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
                    Text(selectedSector.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("Circuito ${selectedSector.circuitCode} · ${selectedSector.rotationBlock}", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                    Text("Los reportes ciudadanos no confirman una medición en tiempo real.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (activeFilter != null && selectedSector.status != activeFilter) {
                        Text("Este sector está fuera del filtro actual.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { onQuickReport(true, "NORMAL", null) }, modifier = Modifier.weight(1f).heightIn(min = 44.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = StatusNormalGreen), shape = RoundedCornerShape(10.dp)) {
                            Text("Con luz", fontSize = 12.sp, maxLines = 1)
                        }
                        Button(onClick = { onQuickReport(false, "SIN_LUZ", null) }, modifier = Modifier.weight(1f).heightIn(min = 44.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = StatusScheduledRed), shape = RoundedCornerShape(10.dp)) {
                            Text("Sin luz", fontSize = 12.sp, maxLines = 1)
                        }
                        Button(onClick = { onQuickReport(false, "FALLA_IRREGULAR", null) }, modifier = Modifier.weight(1f).heightIn(min = 44.dp).testTag("map_report_irregular"), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = StatusIrregularPurple), shape = RoundedCornerShape(10.dp)) {
                            Text("Falla", fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(label: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(100.dp), color = if (isSelected) color.copy(alpha = .15f) else MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, if (isSelected) color else MaterialTheme.colorScheme.outline), modifier = Modifier.clickable(onClick = onClick)) {
        Row(Modifier.heightIn(min = 40.dp).padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) color else MaterialTheme.colorScheme.onSurface)
        }
    }
}
