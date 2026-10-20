package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BarinasLocation
import com.example.data.model.BarinasLocationsCatalog
import com.example.data.model.PacBlock
import com.example.data.model.Sector
import kotlinx.coroutines.delay

@Composable
fun BarinasAddressDialog(
    isOpen: Boolean,
    currentAddress: String?,
    onLocationSelected: (BarinasLocation) -> Unit,
    onDismiss: () -> Unit,
    onRegisterNewCommunity: ((name: String, municipio: String, parroquia: String, block: String, circuit: String) -> Unit)? = null,
    availableSectors: List<Sector> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (!isOpen) return
    var dialogTab by rememberSaveable { mutableIntStateOf(0) }
    var customName by rememberSaveable { mutableStateOf("") }
    var customMunicipio by rememberSaveable { mutableStateOf(BarinasLocationsCatalog.BARINAS_MUNICIPALITIES[0].name) }
    var customParroquia by rememberSaveable { mutableStateOf(BarinasLocationsCatalog.BARINAS_MUNICIPALITIES[0].parroquias[0]) }
    var customBlock by rememberSaveable { mutableStateOf("C") }
    var customCircuit by rememberSaveable { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedPreviewLocation by remember { mutableStateOf<BarinasLocation?>(null) }
    val focusManager = LocalFocusManager.current
    val dynamicLocations = remember(availableSectors) {
        availableSectors.map { sector ->
            BarinasLocation(
                id = sector.id, name = sector.name,
                type = if (sector.isCommunity) "Sector comunitario" else "Sector PAC",
                parroquia = "Barinas",
                block = PacBlock.normalize(sector.rotationBlock)?.let { "Bloque $it" } ?: sector.rotationBlock,
                circuitCode = sector.circuitCode, sectorEntityId = sector.id,
                description = if (sector.isCommunity) "Incorporado por la comunidad" else "Sector del catálogo PAC Barinas",
                keywords = listOf(sector.name.lowercase(), sector.circuitCode.lowercase(), "barinas"),
                municipio = "Barinas"
            )
        }
    }
    var results by remember { mutableStateOf(emptyList<BarinasLocation>()) }
    LaunchedEffect(query, dynamicLocations) {
        if (query.length >= 2) {
            delay(200)
            val matches = dynamicLocations.filter { location ->
                location.name.contains(query, ignoreCase = true) ||
                    location.circuitCode.contains(query, ignoreCase = true) ||
                    location.parroquia.contains(query, ignoreCase = true) ||
                    location.keywords.any { it.contains(query, ignoreCase = true) }
            }
            results = (matches + BarinasLocationsCatalog.searchLocations(query)).distinctBy { it.name.trim().lowercase() }
        } else {
            results = dynamicLocations.filter { it.type == "Sector comunitario" }
        }
    }
    AdaptiveActionDialog(
        onDismiss = onDismiss,
        modifier = modifier.testTag("barinas_address_dialog"),
        actions = {
            if (dialogTab == 1) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onRegisterNewCommunity?.invoke(customName.trim(), customMunicipio.trim(), customParroquia.trim(), "Bloque $customBlock", customCircuit.trim())
                        onDismiss()
                    },
                    enabled = customName.isNotBlank() && onRegisterNewCommunity != null,
                    modifier = Modifier.fillMaxWidth().testTag("register_sector_button")
                ) { Text("Guardar mi sector", fontWeight = FontWeight.Bold) }
            } else {
                selectedPreviewLocation?.let { location ->
                    Button(
                        onClick = { focusManager.clearFocus(); onLocationSelected(location) },
                        modifier = Modifier.fillMaxWidth().testTag("confirm_location_button")
                    ) { Text("Elegir este sector", fontWeight = FontWeight.Bold) }
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") }
        }
    ) { bodyModifier ->
        LazyColumn(
            modifier = bodyModifier.testTag("address_dialog_scroll"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Tu sector en Barinas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Busca una ubicación o denomina tu sector.", style = MaterialTheme.typography.bodyMedium)
                currentAddress?.let {
                    Text("Actual: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = dialogTab == 0, onClick = { focusManager.clearFocus(); dialogTab = 0 }, label = { Text("Buscar") }, modifier = Modifier.weight(1f).testTag("address_search_tab"))
                    FilterChip(selected = dialogTab == 1, onClick = { focusManager.clearFocus(); dialogTab = 1 }, label = { Text("Mi sector") }, modifier = Modifier.weight(1f).testTag("address_register_tab"))
                }
            }
            if (dialogTab == 0) {
                item {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it; selectedPreviewLocation = null },
                        modifier = Modifier.fillMaxWidth().testTag("address_dialog_input"),
                        label = { Text("Barrio, urbanización o circuito") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) IconButton(onClick = { query = ""; selectedPreviewLocation = null }) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { selectedPreviewLocation = results.firstOrNull(); focusManager.clearFocus() })
                    )
                }
                selectedPreviewLocation?.let { location ->
                    item(key = "selected_location") {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().testTag("location_confirmed_card")) {
                            Column(Modifier.padding(12.dp)) {
                                Text(location.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${location.block} · ${location.circuitCode}", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("Pulsa «Elegir este sector» para guardar.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item {
                    Text(
                        if (query.length < 2 && results.isEmpty()) "Escribe al menos dos letras para buscar en el catálogo." else "${results.size} sectores encontrados",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                items(results, key = { it.id }) { location ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedPreviewLocation?.id == location.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { selectedPreviewLocation = location; focusManager.clearFocus() }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(location.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("${location.type} · ${location.block}", style = MaterialTheme.typography.labelMedium)
                            Text("${location.parroquia} · ${location.circuitCode}", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (query.length >= 2 && results.isEmpty() && onRegisterNewCommunity != null) {
                    item {
                        OutlinedButton(onClick = { customName = query.trim(); dialogTab = 1; focusManager.clearFocus() }, modifier = Modifier.fillMaxWidth()) {
                            Text("No aparece: denominar mi sector")
                        }
                    }
                }
            } else {
                item {
                    Text("Denominar mi sector", style = MaterialTheme.typography.titleMedium)
                    Text("Indica su ubicación y bloque PAC. La propuesta se enviará para revisión cuando tengas conexión.", style = MaterialTheme.typography.bodyMedium)
                }
                item {
                    OutlinedTextField(value = customName, onValueChange = { customName = it }, label = { Text("Nombre del sector") }, singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), modifier = Modifier.fillMaxWidth().testTag("register_sector_name"))
                }
                item {
                    Text("Municipio", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BarinasLocationsCatalog.BARINAS_MUNICIPALITIES) { municipality ->
                            FilterChip(selected = customMunicipio == municipality.name, onClick = { customMunicipio = municipality.name; customParroquia = municipality.parroquias.firstOrNull().orEmpty() }, label = { Text(municipality.name) })
                        }
                    }
                }
                item {
                    Text("Parroquia", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BarinasLocationsCatalog.getParroquiasForMunicipality(customMunicipio)) { parish ->
                            FilterChip(selected = customParroquia == parish, onClick = { customParroquia = parish }, label = { Text(parish) })
                        }
                    }
                }
                item {
                    Text("Bloque PAC asignado", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("A", "B", "C", "D").forEach { block ->
                            FilterChip(selected = customBlock == block, onClick = { customBlock = block }, label = { Text(block) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = customCircuit, onValueChange = { customCircuit = it },
                        label = { Text("Circuito o referencia (opcional)") }, minLines = 1, maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth().testTag("register_sector_circuit")
                    )
                }
            }
        }
    }
}
