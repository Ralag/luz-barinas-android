package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BarinasLocation
import com.example.data.model.BarinasLocationsCatalog
import com.example.ui.theme.BlockAColor
import com.example.ui.theme.BlockBColor
import com.example.ui.theme.BlockCColor
import com.example.ui.theme.BlockDColor

@Composable
fun BarinasAddressDialog(
    isOpen: Boolean,
    currentAddress: String?,
    onLocationSelected: (BarinasLocation) -> Unit,
    onDismiss: () -> Unit,
    onRegisterNewCommunity: ((name: String, municipio: String, parroquia: String, block: String, circuit: String) -> Unit)? = null,
    availableSectors: List<com.example.data.model.Sector> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var dialogTab by remember { mutableIntStateOf(0) } // 0 = Buscar, 1 = Denominar / Registrar
    var customName by remember { mutableStateOf("") }
    var customMunicipio by remember { mutableStateOf(BarinasLocationsCatalog.BARINAS_MUNICIPALITIES[0].name) }
    var customParroquia by remember { mutableStateOf(BarinasLocationsCatalog.BARINAS_MUNICIPALITIES[0].parroquias[0]) }
    var customBlock by remember { mutableStateOf("C") }
    var customCircuit by remember { mutableStateOf("") }

    var query by remember { mutableStateOf("") }
    var selectedPreviewLocation by remember { mutableStateOf<BarinasLocation?>(null) }
    val focusManager = LocalFocusManager.current

    val dynamicLocations = remember(availableSectors) {
        availableSectors.map { sector ->
            val cleanBlock = if (sector.rotationBlock.startsWith("Bloque", ignoreCase = true)) {
                sector.rotationBlock
            } else {
                "Bloque ${sector.rotationBlock}"
            }
            BarinasLocation(
                id = sector.id,
                name = sector.name,
                type = if (sector.isCommunity) "Sector Comunitario 🤝" else "Sector Oficial",
                parroquia = sector.parroquia ?: "Barinas",
                block = cleanBlock,
                circuitCode = sector.circuitCode,
                sectorEntityId = sector.id,
                description = if (sector.isCommunity) "Incorporado por la comunidad" else "Sector de Barinas",
                keywords = listOf(sector.name.lowercase(), sector.circuitCode.lowercase(), (sector.parroquia ?: "").lowercase()),
                municipio = "Barinas"
            )
        }
    }

    // Debounced search to avoid blocking main thread on every keystroke
    var results by remember { mutableStateOf(emptyList<BarinasLocation>()) }
    LaunchedEffect(query, dynamicLocations) {
        if (query.length >= 2) {
            delay(200L) // 200ms debounce
            val dynamicMatches = dynamicLocations.filter { loc ->
                loc.name.contains(query, ignoreCase = true) ||
                loc.circuitCode.contains(query, ignoreCase = true) ||
                loc.parroquia.contains(query, ignoreCase = true) ||
                loc.keywords.any { it.contains(query, ignoreCase = true) }
            }
            val catalogMatches = BarinasLocationsCatalog.searchLocations(query)
            // Dynamic matches (especially community sectors) first, distinct by name
            results = (dynamicMatches + catalogMatches).distinctBy { it.name.trim().lowercase() }
        } else {
            // When query is empty, show Community sectors at the top!
            val communitySectors = dynamicLocations.filter { it.type.contains("Comunitario") }
            results = communitySectors
        }
    }

    val popularSuggestions = remember {
        listOf(
            "La Cincuentena",
            "Don Samuel",
            "Alto Barinas Norte",
            "Ciudad Varyna",
            "La Floresta",
            "Centro",
            "Los Pozones",
            "Ciudad Tavacare",
            "Barinitas",
            "Socopó"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 560.dp)
                .padding(vertical = 20.dp)
                .testTag("barinas_address_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ubicación en Barinas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Busca tu sector o regístralo si aún no aparece.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher (Buscar vs Denominar / Registrar)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (dialogTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dialogTab = 0 }
                        ) {
                            Text(
                                text = "🔍 Buscar",
                                fontSize = 12.sp,
                                fontWeight = if (dialogTab == 0) FontWeight.Bold else FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (dialogTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (dialogTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dialogTab = 1 }
                        ) {
                            Text(
                                text = "➕ Denominar Mi Sector",
                                fontSize = 12.sp,
                                fontWeight = if (dialogTab == 1) FontWeight.Bold else FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (dialogTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (dialogTab == 0) {

                // Search Input Field
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        selectedPreviewLocation = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("address_dialog_input"),
                    placeholder = {
                        Text(
                            "Tu barrio, urbanización o sector...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    Icons.Outlined.Clear,
                                    contentDescription = "Limpiar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        val first = results.firstOrNull()
                        if (first != null) {
                            selectedPreviewLocation = first
                        }
                        focusManager.clearFocus()
                    })
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Popular Quick Chips
                Text(
                    text = "Sectores frecuentes:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(popularSuggestions, key = { it }) { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.clickable {
                                query = item
                                val match = BarinasLocationsCatalog.searchLocations(item).firstOrNull()
                                selectedPreviewLocation = match
                            }
                        ) {
                            Text(
                                text = item,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location Confirmation Card if selected
                AnimatedVisibility(
                    visible = selectedPreviewLocation != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    selectedPreviewLocation?.let { loc ->
                        val blockColor = when {
                            loc.block.contains("A") -> BlockAColor
                            loc.block.contains("B") -> BlockBColor
                            loc.block.contains("C") -> BlockCColor
                            loc.block.contains("D") -> BlockDColor
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("location_confirmed_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = blockColor.copy(alpha = 0.12f)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, blockColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = loc.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${loc.type} • Parroquia ${loc.parroquia}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "⚡ ${loc.circuitCode}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = blockColor
                                    ) {
                                        Text(
                                            text = loc.block,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        onLocationSelected(loc)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("confirm_location_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = blockColor)
                                ) {
                                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Confirmar y Fijar esta Ubicación", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Search Results or Not-Found Card
                if (query.length >= 2 && results.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .clickable {
                                customName = query.trim()
                                dialogTab = 1
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¿No encuentras '$query'?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Toca aquí para registrar este sector indicando su Municipio y Parroquia.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    val isShowingCommunityInitial = query.isEmpty() && results.isNotEmpty()
                    Text(
                        text = if (isShowingCommunityInitial) {
                            "Sectores comunitarios activos (${results.size}) 🤝:"
                        } else {
                            "Resultados encontrados (${results.size}):"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isShowingCommunityInitial) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(results, key = { it.id }) { loc ->
                            val locBlockColor = when {
                                loc.block.contains("A") -> BlockAColor
                                loc.block.contains("B") -> BlockBColor
                                loc.block.contains("C") -> BlockCColor
                                loc.block.contains("D") -> BlockDColor
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedPreviewLocation?.id == loc.id) {
                                    locBlockColor.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (selectedPreviewLocation?.id == loc.id) 1.5.dp else 0.5.dp,
                                    color = if (selectedPreviewLocation?.id == loc.id) locBlockColor else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPreviewLocation = loc
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = loc.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (loc.type.contains("Comunitario")) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFFFB300).copy(alpha = 0.2f),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                                                ) {
                                                    Text(
                                                        text = "Comunitario 🤝",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFFFB300),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${loc.parroquia} • ${loc.circuitCode}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = locBlockColor.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, locBlockColor)
                                    ) {
                                        Text(
                                            text = loc.block,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = locBlockColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                } else {
                    // TAB 1: Denominar / Registrar Mi Sector (Crowdsource Barinas geography)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📍 Registrar Sector / Comunidad en la Nube",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ayuda a mapear Barinas. Selecciona tu municipio, parroquia y el bloque asignado.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 1. Nombre de la comunidad
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Nombre de la Comunidad / Sector *") },
                            placeholder = { Text("Ej: Urb. Los Profesionales, Barrio...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 2. Municipio Selector (12 Municipios Oficiales de Barinas)
                        Column {
                            Text("1. Municipio del Estado Barinas: *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(BarinasLocationsCatalog.BARINAS_MUNICIPALITIES) { mun ->
                                    val isSelected = customMunicipio == mun.name
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                        modifier = Modifier.clickable {
                                            customMunicipio = mun.name
                                            customParroquia = mun.parroquias.firstOrNull() ?: ""
                                        }
                                    ) {
                                        Text(
                                            text = mun.name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Parroquia Selector (Filtradas según el Municipio seleccionado)
                        Column {
                            val availableParroquias = remember(customMunicipio) {
                                BarinasLocationsCatalog.getParroquiasForMunicipality(customMunicipio)
                            }
                            Text("2. Parroquia (Municipio $customMunicipio): *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(availableParroquias) { p ->
                                    val isSelected = customParroquia == p
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                        modifier = Modifier.clickable { customParroquia = p }
                                    ) {
                                        Text(
                                            text = p,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Bloque PAC Asignado
                        Column {
                            Text("3. Bloque PAC asignado por Corpoelec: *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("A", "B", "C", "D").forEach { b ->
                                    val isSelected = customBlock == b
                                    val bColor = when (b) {
                                        "A" -> BlockAColor
                                        "B" -> BlockBColor
                                        "C" -> BlockCColor
                                        else -> BlockDColor
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) bColor else bColor.copy(alpha = 0.12f),
                                        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, bColor),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { customBlock = b }
                                    ) {
                                        Text(
                                            text = "Bloque $b",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            color = if (isSelected) Color.White else bColor,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 5. Circuito o Referencia
                        OutlinedTextField(
                            value = customCircuit,
                            onValueChange = { customCircuit = it },
                            label = { Text("Circuito o Punto de Referencia (Opcional)") },
                            placeholder = { Text("Ej: Circuito Don Samuel / Cerca de la plaza...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (customName.isNotBlank()) {
                                    onRegisterNewCommunity?.invoke(
                                        customName.trim(),
                                        customMunicipio.trim(),
                                        customParroquia.trim(),
                                        "Bloque $customBlock",
                                        customCircuit.trim()
                                    )
                                    onDismiss()
                                }
                            },
                            enabled = customName.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar y Registrar en la Nube", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dismiss / Explore button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentAddress != null) {
                        Text(
                            text = "Actual: $currentAddress",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Cerrar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
