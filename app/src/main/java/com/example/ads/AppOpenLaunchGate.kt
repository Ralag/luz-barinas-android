package com.example.ads

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppOpenAdManager
import com.example.ui.components.AppOpenAdStatus
import kotlinx.coroutines.delay

/** Only this loading surface may show an opening ad. Late loads are cached, never shown. */
@Composable
fun AppOpenLaunchGate(onReady: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context.findAdsActivity()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val runtime by AdsRuntime.state.collectAsStateWithLifecycle()
    val status by AppOpenAdManager.state.collectAsStateWithLifecycle()
    val adFree = rememberAdFreeActive()
    val policy = remember { AppOpenLaunchPolicy(SystemClock.elapsedRealtime()) }
    val readyCallback by rememberUpdatedState(onReady)
    var tick by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var resumed by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ -> resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        while (policy.phase == AppOpenLaunchPhase.WAITING) {
            delay(50)
            tick = SystemClock.elapsedRealtime()
        }
    }
    LaunchedEffect(tick, runtime, status, adFree, resumed) {
        when (policy.evaluate(SystemClock.elapsedRealtime(), adFree, runtime.consentResolved,
            runtime.canRequestAds, runtime.initialized, AppOpenAdManager.hasAvailableAd(),
            status == AppOpenAdStatus.FAILED, activity != null && AppOpenAdManager.isLaunchEligible(), resumed)) {
            AppOpenLaunchAction.LOAD -> AppOpenAdManager.loadAd(context.applicationContext)
            AppOpenLaunchAction.SHOW -> {
                val shown = activity != null && AppOpenAdManager.showAdIfAvailable(activity, isLoadingScreen = true) {
                    if (policy.onAdFinished() == AppOpenLaunchAction.CONTINUE) readyCallback()
                }
                if (!shown && policy.onAdFinished() == AppOpenLaunchAction.CONTINUE) readyCallback()
            }
            AppOpenLaunchAction.CONTINUE -> readyCallback()
            AppOpenLaunchAction.NONE -> Unit
        }
    }
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Outlined.Bolt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("PAC Barinas", style = MaterialTheme.typography.headlineMedium)
            Text("Tu comunidad, conectada", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(Modifier.widthIn(max = 240.dp).fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { if (policy.skip() == AppOpenLaunchAction.CONTINUE) readyCallback() }) {
                Text("Entrar ahora")
            }
        }
    }
}
