package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsDialog(
    isOpen: Boolean,
    currentThemeMode: String,
    currentColorScheme: String,
    currentVisualScale: Float,
    onDismiss: () -> Unit,
    onUpdateTheme: (themeMode: String?, colorScheme: String?, scale: Float?) -> Unit
) {
    if (!isOpen) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎨 Personalizar Interfaz",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Cerrar")
                }
            }

            // Fondo / Modo
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Modo de Fondo",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        text = "Claro",
                        isSelected = currentThemeMode == "light",
                        onClick = { onUpdateTheme("light", null, null) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        text = "Oscuro",
                        isSelected = currentThemeMode == "dark",
                        onClick = { onUpdateTheme("dark", null, null) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        text = "Sistema",
                        isSelected = currentThemeMode == "system",
                        onClick = { onUpdateTheme("system", null, null) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Colores
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Estilo de Colores",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        text = "Predeterminado",
                        isSelected = currentColorScheme == "default",
                        onClick = { onUpdateTheme(null, "default", null) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        text = "Dinámico",
                        isSelected = currentColorScheme == "dynamic",
                        onClick = { onUpdateTheme(null, "dynamic", null) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Escala visual
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Escala Visual",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        text = "Compacto",
                        isSelected = currentVisualScale == 0.85f,
                        onClick = { onUpdateTheme(null, null, 0.85f) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        text = "Normal",
                        isSelected = currentVisualScale == 1.0f,
                        onClick = { onUpdateTheme(null, null, 1.0f) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        text = "Grande",
                        isSelected = currentVisualScale == 1.15f,
                        onClick = { onUpdateTheme(null, null, 1.15f) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThemeOptionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
