package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig

enum class LegalPage {
    PRIVACY, TERMS, DEVELOPER, REFUND, SYSTEM_DOC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onToggleDarkMode: () -> Unit,
    isDarkMode: Boolean,
    onChangeLocation: () -> Unit,
    onRestartOnboarding: () -> Unit,
    onOpenAlarmSettings: () -> Unit,
    onCheckUpdate: () -> Unit,
    onOpenDonations: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentLegalPage by remember { mutableStateOf<LegalPage?>(null) }

    BackHandler {
        if (currentLegalPage != null) {
            currentLegalPage = null
        } else {
            onBack()
        }
    }

    Crossfade(targetState = currentLegalPage, label = "Settings_Legal_Transition") { legalPage ->
        if (legalPage != null) {
            val title = when (legalPage) {
                LegalPage.PRIVACY -> "Política de Privacidad"
                LegalPage.TERMS -> "Términos y Condiciones"
                LegalPage.DEVELOPER -> "Datos del Desarrollador"
                LegalPage.REFUND -> "Política de Reembolsos"
                LegalPage.SYSTEM_DOC -> "Documentación del Sistema"
            }
            LegalScreen(title = title, onBack = { currentLegalPage = null }) {
                when (legalPage) {
                    LegalPage.PRIVACY -> PrivacyPolicyContent()
                    LegalPage.TERMS -> TermsAndConditionsContent()
                    LegalPage.DEVELOPER -> DeveloperInfoContent()
                    LegalPage.REFUND -> RefundPolicyContent()
                    LegalPage.SYSTEM_DOC -> SystemDocumentationContent()
                }
            }
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Categoría: General
                    item {
                        SettingsCategory(title = "General") {
                            SettingsItem(
                                icon = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                title = "Modo Oscuro",
                                subtitle = "Cambiar entre tema claro y oscuro",
                                onClick = onToggleDarkMode,
                                trailingContent = {
                                    Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() })
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.Outlined.LocationOn,
                                title = "Cambiar Ubicación/Sector",
                                subtitle = "Modificar el sector para ver el cronograma",
                                onClick = onChangeLocation
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.Outlined.RestartAlt,
                                title = "Reiniciar Configuración Inicial",
                                subtitle = "Volver a mostrar la pantalla de bienvenida",
                                onClick = onRestartOnboarding
                            )
                        }
                    }

                    // Categoría: Notificaciones y Alarmas
                    item {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        var pacSettings by remember { mutableStateOf(com.example.data.model.PacAlertPrefs.getSettings(context)) }

                        SettingsCategory(title = "Notificaciones y Alarmas") {
                            SettingsItem(
                                icon = if (pacSettings.isAlarmEnabled) Icons.Filled.Alarm else Icons.Outlined.NotificationsActive,
                                title = if (pacSettings.isAlarmEnabled) "Alarma Sonora Continua (Antiguo)" else "Notificaciones Push Estándar (Default)",
                                subtitle = if (pacSettings.isAlarmEnabled)
                                    "Activado: Sonará sirena en bucle continuo audible hasta que se apague manualmente."
                                else
                                    "Recomendado: Notificaciones push estándar con tonos únicos para corte y retorno de luz.",
                                onClick = {
                                    val updated = pacSettings.copy(
                                        isAlarmEnabled = !pacSettings.isAlarmEnabled,
                                        useLegacyAlarmSiren = !pacSettings.isAlarmEnabled
                                    )
                                    pacSettings = updated
                                    com.example.data.model.PacAlertPrefs.saveSettings(context, updated)
                                    com.example.notification.PacAlarmScheduler.scheduleNextAlarm(context)
                                },
                                trailingContent = {
                                    Switch(
                                        checked = pacSettings.isAlarmEnabled,
                                        onCheckedChange = { checked ->
                                            val updated = pacSettings.copy(
                                                isAlarmEnabled = checked,
                                                useLegacyAlarmSiren = checked
                                            )
                                            pacSettings = updated
                                            com.example.data.model.PacAlertPrefs.saveSettings(context, updated)
                                            com.example.notification.PacAlarmScheduler.scheduleNextAlarm(context)
                                        }
                                    )
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.Outlined.LightMode,
                                title = "Aviso de Retorno de la Luz",
                                subtitle = "Notificar cuando finaliza el turno PAC y regresa el servicio eléctrico",
                                onClick = {
                                    val updated = pacSettings.copy(isRestoreAlarmEnabled = !pacSettings.isRestoreAlarmEnabled)
                                    pacSettings = updated
                                    com.example.data.model.PacAlertPrefs.saveSettings(context, updated)
                                    com.example.notification.PacAlarmScheduler.scheduleNextAlarm(context)
                                },
                                trailingContent = {
                                    Switch(
                                        checked = pacSettings.isRestoreAlarmEnabled,
                                        onCheckedChange = { checked ->
                                            val updated = pacSettings.copy(isRestoreAlarmEnabled = checked)
                                            pacSettings = updated
                                            com.example.data.model.PacAlertPrefs.saveSettings(context, updated)
                                            com.example.notification.PacAlarmScheduler.scheduleNextAlarm(context)
                                        }
                                    )
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.Outlined.Tune,
                                title = "Ajustes Avanzados de Anticipación y Sonido",
                                subtitle = "Anticipación (${pacSettings.advanceMinutes} min antes), pruebas acústicas y vibración",
                                onClick = onOpenAlarmSettings
                            )
                        }
                    }

                    // Categoría: Actualizaciones
                    item {
                        SettingsCategory(title = "Actualizaciones") {
                            SettingsItem(
                                icon = Icons.Outlined.SystemUpdate,
                                title = "Buscar Actualizaciones OTA",
                                subtitle = "Verificar si hay una nueva versión disponible",
                                onClick = onCheckUpdate
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.Outlined.Info,
                                title = "Versión de la aplicación",
                                subtitle = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                onClick = {}
                            )
                        }
                    }

                    // Categoría: Apoyar el Proyecto
                    item {
                        SettingsCategory(title = "Apoyar el Proyecto") {
                            SettingsItem(
                                icon = Icons.Outlined.FavoriteBorder,
                                title = "Donar al Proyecto",
                                subtitle = "Apoya el mantenimiento de los servidores",
                                onClick = onOpenDonations,
                                iconTint = androidx.compose.ui.graphics.Color(0xFFE91E63)
                            )
                        }
                    }

                    // Categoría: Legal
                    item {
                        SettingsCategory(title = "Legal") {
                            SettingsItem(
                                icon = Icons.Outlined.Policy,
                                title = "Política de Privacidad",
                                subtitle = "Cómo manejamos tus datos",
                                onClick = { currentLegalPage = LegalPage.PRIVACY }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.Outlined.Gavel,
                                title = "Términos y Condiciones",
                                subtitle = "Reglas de uso del servicio",
                                onClick = { currentLegalPage = LegalPage.TERMS }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.Outlined.Business,
                                title = "Datos del Negocio/Desarrollador",
                                subtitle = "Información sobre el proyecto",
                                onClick = { currentLegalPage = LegalPage.DEVELOPER }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                                title = "Política de Reembolsos",
                                subtitle = "Información sobre donaciones",
                                onClick = { currentLegalPage = LegalPage.REFUND }
                            )
                        }
                    }

                    // Categoría: Información del Sistema
                    item {
                        SettingsCategory(title = "Información del Sistema") {
                            SettingsItem(
                                icon = Icons.Outlined.MenuBook,
                                title = "Documentación Técnica del Sistema",
                                subtitle = "Arquitectura, módulos, bases de datos y flujos",
                                onClick = { currentLegalPage = LegalPage.SYSTEM_DOC }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.Outlined.Build,
                                title = "Detalles Técnicos",
                                subtitle = "Versión: ${BuildConfig.VERSION_NAME}\nBuild ID: ${BuildConfig.VERSION_CODE}",
                                onClick = { currentLegalPage = LegalPage.SYSTEM_DOC }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.Outlined.IntegrationInstructions,
                                title = "Integraciones Activas",
                                subtitle = "AdMob (Anuncios), Supabase (Base de Datos y Tiempo Real)",
                                onClick = { currentLegalPage = LegalPage.SYSTEM_DOC }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                icon = Icons.Outlined.DataUsage,
                                title = "Datos Recopilados",
                                subtitle = "Sector seleccionado, reportes anónimos, preferencias",
                                onClick = { currentLegalPage = LegalPage.PRIVACY }
                            )
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategory(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}
