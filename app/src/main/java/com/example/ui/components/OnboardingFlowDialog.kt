package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat

/** A short introduction. Permissions are optional and can be configured later. */
@Composable
fun OnboardingFlowDialog(
    isOpen: Boolean,
    onComplete: () -> Unit,
    onSkip: () -> Unit = onComplete,
    onWatchTutorial: (() -> Unit)? = null
) {
    if (!isOpen) return
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var entered by remember { mutableStateOf(false) }
    val entranceAlpha by animateFloatAsState(if (entered) 1f else 0f, tween(220), label = "welcome_entrance")
    LaunchedEffect(Unit) { entered = true }
    AdaptiveActionDialog(
        onDismiss = onSkip,
        modifier = Modifier.testTag("onboarding_dialog"),
        actions = {
            Button(onClick = onComplete, modifier = Modifier.fillMaxWidth().testTag("onboarding_choose_sector")) {
                Text("Elegir mi sector", fontWeight = FontWeight.Bold)
            }
            if (currentStep == 0) {
                TextButton(onClick = { currentStep = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Configurar avisos (opcional)") }
                TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth().testTag("onboarding_skip")) { Text("Explorar primero") }
            } else {
                TextButton(onClick = { currentStep = 0 }, modifier = Modifier.fillMaxWidth()) { Text("Volver a la bienvenida") }
            }
        }
    ) { bodyModifier ->
        // Short transitions inherit Android's animator duration scale, including
        // reduced motion. There is no timer, auto advance or repeating intro.
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) },
            modifier = bodyModifier.graphicsLayer { alpha = entranceAlpha },
            label = "onboarding_steps"
        ) { step ->
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (step == 0) {
                    WelcomeStep(onWatchTutorial)
                } else {
                    PermissionsStep()
                    HorizontalDivider()
                    BatteryStep()
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onWatchTutorial: (() -> Unit)?) {
    Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
    Text("Tu día, con más previsión", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("PAC Barinas reúne tu horario y los avisos de tu comunidad en un solo lugar.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    WelcomeFeature(Icons.Default.Alarm, "Consulta tu próximo turno", "Elige tu sector para ver su programación de hoy.")
    WelcomeFeature(Icons.Default.Map, "Explora el mapa", "Consulta sectores y reportes de la comunidad.")
    WelcomeFeature(Icons.Default.NotificationsActive, "Comparte lo que ocurre", "Reporta cortes, retorno del servicio y fallas irregulares.")
    onWatchTutorial?.let { watch ->
        OutlinedButton(onClick = watch, modifier = Modifier.fillMaxWidth().testTag("onboarding_tutorial")) { Text("Ver tutorial en YouTube") }
    }
    Text(
        "Aplicación comunitaria independiente; no representa a CORPOELEC ni a organismos públicos. Los horarios son referenciales y proceden de comunicaciones públicas del prestador eléctrico (corpoelec.gob.ve).",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun WelcomeFeature(icon: ImageVector, title: String, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PermissionsStep() {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> notificationsEnabled = granted }
    Text("Avisos a tu ritmo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Puedes consultar horarios y usar el mapa sin conceder permisos. Actívalos si deseas recibir avisos locales antes de tu turno.", style = MaterialTheme.typography.bodyMedium)
    OutlinedButton(
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                runCatching { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }
            }
        },
        enabled = !notificationsEnabled,
        modifier = Modifier.fillMaxWidth()
    ) { Text(if (notificationsEnabled) "Notificaciones activadas" else "Permitir notificaciones") }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        OutlinedButton(
            onClick = {
                runCatching { context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Revisar permiso de alarmas") }
    }
}

@Composable
private fun BatteryStep() {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("Si no llegan los avisos", style = MaterialTheme.typography.titleMedium)
    }
    Text("Algunos teléfonos limitan las tareas en segundo plano. Si notas retrasos, revisa los ajustes de batería de PAC Barinas. No necesitas cambiarlos para empezar.", style = MaterialTheme.typography.bodyMedium)
    OutlinedButton(
        onClick = { runCatching { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) } },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Abrir ajustes de la aplicación") }
}
