package com.example.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Each visible editor/dialog owns a token; closing one cannot unblock another. */
internal object AppOpenInteractionGuard {
    private val owners = mutableSetOf<Any>()
    private val mutableBlocked = MutableStateFlow(false)
    val blocked = mutableBlocked.asStateFlow()

    fun canOpenAfterReturn(launchEligible: Boolean, alarmPlaying: Boolean): Boolean =
        launchEligible && !alarmPlaying && !blocked.value

    @Synchronized
    fun acquire(): Any = Any().also {
        owners.add(it)
        mutableBlocked.value = true
    }

    @Synchronized
    fun release(owner: Any) {
        owners.remove(owner)
        mutableBlocked.value = owners.isNotEmpty()
    }
}

/** Protect unfinished work for as long as this surface remains in composition. */
@Composable
fun AppOpenInteractionProtection(busy: Boolean) {
    DisposableEffect(busy) {
        val owner = if (busy) AppOpenInteractionGuard.acquire() else null
        onDispose { owner?.let(AppOpenInteractionGuard::release) }
    }
}
