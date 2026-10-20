package com.example.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** A return ad covers the existing screen; it never disposes its forms or navigation. */
@Composable
fun AppOpenLaunchHost(
    launchGeneration: Int,
    alarmPlaying: Boolean = false,
    gate: @Composable (() -> Unit) -> Unit = { onReady -> AppOpenLaunchGate(onReady) },
    content: @Composable () -> Unit
) {
    var entered by rememberSaveable { mutableStateOf(false) }
    var completedGeneration by rememberSaveable { mutableIntStateOf(-1) }
    val interactionBlocked by AppOpenInteractionGuard.blocked.collectAsStateWithLifecycle()
    val pending = completedGeneration != launchGeneration
    val blocked = interactionBlocked || alarmPlaying
    val showGate = pending && !blocked

    LaunchedEffect(pending, blocked, launchGeneration) {
        if (pending && blocked) {
            // A protected return is skipped, not deferred until the user closes the form.
            entered = true
            completedGeneration = launchGeneration
        }
    }
    Box(Modifier.fillMaxSize()) {
        if (entered) {
            Box(Modifier.fillMaxSize().then(if (showGate) Modifier.clearAndSetSemantics { } else Modifier)) {
                content()
            }
        }
        if (showGate) {
            key(launchGeneration) {
                Box(Modifier.fillMaxSize().pointerInput(Unit) {
                    // Keep taps on the loading overlay from reaching the preserved content.
                    awaitPointerEventScope {
                        while (true) awaitPointerEvent(PointerEventPass.Final).changes.forEach { it.consume() }
                    }
                }) {
                    gate {
                        entered = true
                        completedGeneration = launchGeneration
                    }
                }
            }
        }
    }
}
