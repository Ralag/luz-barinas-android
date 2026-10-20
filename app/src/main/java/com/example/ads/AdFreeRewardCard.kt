package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

internal fun Context.findAdsActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> if (baseContext === this) null else baseContext.findAdsActivity()
    else -> null
}

/** Rechecks at expiry, while visible, and immediately on resume (including clock changes). */
@Composable
fun rememberAdFreeActive(): Boolean {
    val context = LocalContext.current.applicationContext
    AdFreeRewardStore.initialize(context)
    val window by AdFreeRewardStore.window.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) now = System.currentTimeMillis()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(window, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            now = System.currentTimeMillis()
            while (true) {
                val remaining = (window?.expiresAtMillis ?: now) - now
                delay(if (remaining > 0) remaining.coerceIn(1, 60_000) else 60_000)
                now = System.currentTimeMillis()
            }
        }
    }
    return window?.isActive(now) == true
}

@Composable
fun AdFreeRewardCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context.findAdsActivity()
    val runtime by AdsRuntime.state.collectAsStateWithLifecycle()
    val reward by RewardedAdManager.state.collectAsStateWithLifecycle()
    val window by AdFreeRewardStore.window.collectAsStateWithLifecycle()
    val active = rememberAdFreeActive()
    val hours = AdsConfiguration.rewardHours
    val configured = AdsConfiguration.unitId(AdFormat.REWARDED) != null
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (active) "Tu pausa de publicidad está activa" else "$hours horas sin publicidad",
                style = MaterialTheme.typography.titleMedium)
            when {
                active -> Text("Sin banners ni anuncios de pantalla completa hasta " +
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(Date(window?.expiresAtMillis ?: 0)) + ". Válido en este teléfono.")
                !configured -> Text("La recompensa aún no está habilitada en esta versión.")
                else -> {
                    Text("Si quieres, completa un anuncio para pausar toda la publicidad durante $hours horas en este teléfono. Es opcional: horarios, mapa y reportes siguen disponibles.")
                    Button(
                        onClick = {
                            if (reward.status == RewardedAdStatus.READY && activity != null) {
                                RewardedAdManager.show(activity)
                            } else RewardedAdManager.load(context)
                        },
                        enabled = activity != null && runtime.initialized && runtime.canRequestAds &&
                            reward.status != RewardedAdStatus.LOADING && reward.status != RewardedAdStatus.SHOWING,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(when (reward.status) {
                            RewardedAdStatus.LOADING -> "Cargando anuncio…"
                            RewardedAdStatus.READY -> "Ver anuncio: $hours h sin publicidad"
                            RewardedAdStatus.SHOWING -> "Anuncio en curso…"
                            else -> "Preparar anuncio de recompensa"
                        })
                    }
                    if (!runtime.canRequestAds || !runtime.initialized) {
                        Text(runtime.message ?: "La publicidad todavía no está disponible. Revisa tu conexión y las opciones de privacidad.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            reward.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun AdPrivacyOptionsButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context.findAdsActivity()
    val runtime by AdsRuntime.state.collectAsStateWithLifecycle()
    if (activity == null) return
    if (runtime.privacyOptionsRequired) {
        TextButton(onClick = { AdsRuntime.showPrivacyOptions(activity) }, modifier = modifier) {
            Text("Opciones de privacidad de los anuncios")
        }
    } else if (!runtime.canRequestAds && runtime.message != null) {
        TextButton(onClick = { AdsRuntime.initialize(activity) }, modifier = modifier) {
            Text("Reintentar anuncios y privacidad")
        }
    }
}
