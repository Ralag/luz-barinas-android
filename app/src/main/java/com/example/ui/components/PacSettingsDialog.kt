package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PacAlertPrefs
import com.example.data.model.PacAlertSettings
import com.example.data.model.PacScheduleData
import com.example.notification.PacAlarmPlayer
import com.example.notification.PacAlarmScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PacSettingsDialog(
    isOpen: Boolean,
    userBlock: String,
    userSectorName: String,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State loaded from prefs
    val initialSettings = remember { PacAlertPrefs.getSettings(context) }
    var advanceMinutes by remember { mutableIntStateOf(initialSettings.advanceMinutes) }
    var isNotificationEnabled by remember { mutableStateOf(initialSettings.isNotificationEnabled) }
    var isAlarmEnabled by remember { mutableStateOf(initialSettings.isAlarmEnabled) }
    var isRestoreAlarmEnabled by remember { mutableStateOf(initialSettings.isRestoreAlarmEnabled) }
    var isVibrationEnabled by remember { mutableStateOf(initialSettings.isVibrationEnabled) }

    var isTestingAlarm by remember { mutableStateOf(false) }
    var customMinutesInput by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(advanceMinutes !in listOf(5, 10, 15, 30, 60)) }

    // Next window preview
    val nextWindow = remember(userBlock) {
        PacScheduleData.findNextWindowForBlock(userBlock)
    }

    // Stop demo alarm if dialog dismissed
    DisposableEffect(Unit) {
        onDispose {
            PacAlarmPlayer.stopAlarm(context)
        }
    }

    Dialog(
        onDismissRequest = {
            PacAlarmPlayer.stopAlarm(context)
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Icono de alarma",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Alertas y Alarma PAC",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "PAC Barinas • $userSectorName ($userBlock)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            PacAlarmPlayer.stopAlarm(context)
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Next PAC Cut Info Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Power,
                                    contentDescription = "Icono de energía",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Próximo Corte en tu Bloque",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${nextWindow.dayName} • ${nextWindow.timeLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val alertPreviewMinutes = if (advanceMinutes > 0) advanceMinutes else 10
                            Text(
                                text = "Te avisará $alertPreviewMinutes minutos antes del inicio.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Section 1: Anticipation Time (5 min, 10 min, etc.)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⏱️ Tiempo de Anticipación",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "¿Con cuántos minutos de anticipación deseas la alerta?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick selection chips
                            val chipOptions = listOf(5, 10, 15, 30, 60)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                chipOptions.forEach { mins ->
                                    val isSelected = advanceMinutes == mins && !showCustomInput
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            advanceMinutes = mins
                                            showCustomInput = false
                                        },
                                        label = {
                                            Text(
                                                text = "${mins}m",
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Outlined.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom time toggle
                            TextButton(
                                onClick = { showCustomInput = !showCustomInput },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (showCustomInput) "⬅ Usar opciones rápidas" else "✏️ O especificar minutos exactos...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            AnimatedVisibility(visible = showCustomInput) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customMinutesInput,
                                        onValueChange = { input ->
                                            if (input.all { it.isDigit() } && input.length <= 3) {
                                                customMinutesInput = input
                                                val parsed = input.toIntOrNull()
                                                if (parsed != null && parsed in 1..180) {
                                                    advanceMinutes = parsed
                                                }
                                            }
                                        },
                                        label = { Text("Minutos") },
                                        placeholder = { Text("Ej: 20") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Text(
                                        text = "minutos antes",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Section 2: Toggles
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Selector de Modo: Notificación Estándar vs Alarma Continua
                            Text(
                                text = "🔔 Modo de Aviso PAC",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = !isAlarmEnabled,
                                    onClick = {
                                        isAlarmEnabled = false
                                        isRestoreAlarmEnabled = false
                                        isNotificationEnabled = true
                                    },
                                    label = {
                                        Text(
                                            text = "📲 Notificaciones (Default)",
                                            fontSize = 11.sp,
                                            fontWeight = if (!isAlarmEnabled) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = isAlarmEnabled,
                                    onClick = {
                                        isAlarmEnabled = true
                                        isRestoreAlarmEnabled = true
                                    },
                                    label = {
                                        Text(
                                            text = "⏰ Alarma Sirena",
                                            fontSize = 11.sp,
                                            fontWeight = if (isAlarmEnabled) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Text(
                                text = if (!isAlarmEnabled)
                                    "✓ Notificación estándar no intrusiva con sonido y vibración diferenciados al irse y volver la luz."
                                else
                                    "⚠️ Método antiguo: sonará como un despertador audible en bucle continuo hasta que presiones Apagar Alarma.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (!isAlarmEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Switch 1: Pre-cut notification
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Notificación Previa",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Aviso en la barra de notificaciones $advanceMinutes min antes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isNotificationEnabled,
                                    onCheckedChange = { isNotificationEnabled = it }
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Switch 2: Audible Alarm for Outage
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "🔔 Alarma Sonora de Corte",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAlarmEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "Suena como despertador audible antes del corte (ideal para la noche o si estás ocupado)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isAlarmEnabled,
                                    onCheckedChange = { isAlarmEnabled = it }
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Switch 3: Restore Alarm (Power back on)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "💡 Alarma de Fin de Turno PAC",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Te avisa con tono cuando culmina el turno PAC de tu bloque",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isRestoreAlarmEnabled,
                                    onCheckedChange = { isRestoreAlarmEnabled = it }
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Switch 4: Vibration
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Vibración Continua",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Vibrar con patrón de alerta",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isVibrationEnabled,
                                    onCheckedChange = { isVibrationEnabled = it }
                                )
                            }
                        }
                    }

                    // Section 3: Alarm Test / Demo Button
                    OutlinedButton(
                        onClick = {
                            if (isTestingAlarm) {
                                PacAlarmPlayer.stopAlarm(context)
                                isTestingAlarm = false
                            } else {
                                isTestingAlarm = true
                                PacAlarmPlayer.startAlarm(context, vibrate = isVibrationEnabled)
                                coroutineScope.launch {
                                    delay(5000)
                                    if (isTestingAlarm) {
                                        PacAlarmPlayer.stopAlarm(context)
                                        isTestingAlarm = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isTestingAlarm) MaterialTheme.colorScheme.errorContainer else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isTestingAlarm) Icons.Default.Close else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isTestingAlarm) "Detener" else "Probar sonido",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTestingAlarm) "Detener Prueba de Alarma" else "🔊 Probar Alarma Sonora (Demo 5s)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save & Apply Button
                Button(
                    onClick = {
                        PacAlarmPlayer.stopAlarm(context)
                        val newSettings = PacAlertSettings(
                            advanceMinutes = advanceMinutes,
                            isNotificationEnabled = isNotificationEnabled,
                            isAlarmEnabled = isAlarmEnabled,
                            isRestoreAlarmEnabled = isRestoreAlarmEnabled,
                            isVibrationEnabled = isVibrationEnabled,
                            useLegacyAlarmSiren = isAlarmEnabled
                        )
                        PacAlertPrefs.saveSettings(context, newSettings)
                        PacAlarmScheduler.scheduleNextAlarm(context)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Guardar y Activar Alerta (${advanceMinutes} min)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
