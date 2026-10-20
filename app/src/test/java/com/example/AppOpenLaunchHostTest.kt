package com.example

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ads.AppOpenInteractionGuard
import com.example.ads.AppOpenInteractionProtection
import com.example.ads.AppOpenLaunchHost
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-port-mdpi", sdk = [36])
class AppOpenLaunchHostTest {
    @get:Rule val compose = createComposeRule()

    @Composable
    private fun FakeGate(onReady: () -> Unit) {
        Button(onClick = onReady, modifier = Modifier.testTag("fake_open_gate")) { Text("Entrar") }
    }

    @Test
    fun returnOverlayPreservesMountedFormAndDraft() {
        var generation by mutableIntStateOf(0)
        var mounts = 0
        var disposals = 0
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                AppOpenLaunchHost(generation, gate = { FakeGate(it) }) {
                    DisposableEffect(Unit) { mounts++; onDispose { disposals++ } }
                    var draft by remember { mutableStateOf("") }
                    TextField(draft, { draft = it }, Modifier.testTag("draft"))
                }
            }
        }
        compose.onNodeWithTag("draft").assertDoesNotExist()
        compose.onNodeWithTag("fake_open_gate").performClick()
        compose.onNodeWithTag("draft").performTextInput("Mi sector y observación pendientes")
        compose.runOnIdle { generation++ }
        compose.onNodeWithTag("fake_open_gate").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, mounts); assertEquals(0, disposals) }
        compose.onNodeWithTag("fake_open_gate").performClick()
        compose.onNodeWithTag("draft").assertTextContains("Mi sector y observación pendientes")
        compose.runOnIdle { assertEquals(1, mounts); assertEquals(0, disposals) }
    }

    @Test
    fun protectedReturnIsSkippedAndNotDeferredUntilFormCloses() {
        var generation by mutableIntStateOf(0)
        var formBusy by mutableStateOf(true)
        var gateMounts = 0
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                AppOpenLaunchHost(generation, gate = {
                    DisposableEffect(Unit) { gateMounts++; onDispose { } }
                    FakeGate(it)
                }) {
                    AppOpenInteractionProtection(formBusy)
                    Text("Formulario en curso", Modifier.testTag("protected_form"))
                }
            }
        }
        compose.onNodeWithTag("fake_open_gate").performClick()
        compose.runOnIdle {
            assertFalse(AppOpenInteractionGuard.canOpenAfterReturn(launchEligible = true, alarmPlaying = false))
            generation++ // Defensive host check, even if an external return request races the guard.
        }
        compose.onNodeWithTag("fake_open_gate").assertDoesNotExist()
        compose.onNodeWithTag("protected_form").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, gateMounts); formBusy = false }
        compose.onNodeWithTag("fake_open_gate").assertDoesNotExist()
        compose.runOnIdle {
            assertEquals(1, gateMounts)
            assertTrue(AppOpenInteractionGuard.canOpenAfterReturn(launchEligible = true, alarmPlaying = false))
        }
    }

    @Test
    fun activeAlarmBypassesInitialGateAndAlwaysBlocksReturn() {
        var gateMounts = 0
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                AppOpenLaunchHost(0, alarmPlaying = true, gate = {
                    DisposableEffect(Unit) { gateMounts++; onDispose { } }
                    FakeGate(it)
                }) { Text("Apagar alarma", Modifier.testTag("alarm_content")) }
            }
        }
        compose.onNodeWithTag("fake_open_gate").assertDoesNotExist()
        compose.onNodeWithTag("alarm_content").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(0, gateMounts)
            assertFalse(AppOpenInteractionGuard.canOpenAfterReturn(launchEligible = true, alarmPlaying = true))
        }
    }

    @Test
    fun closingOneProtectedSurfaceCannotReleaseAnother() {
        val dialog = AppOpenInteractionGuard.acquire()
        val draft = AppOpenInteractionGuard.acquire()
        try {
            AppOpenInteractionGuard.release(dialog)
            assertFalse(AppOpenInteractionGuard.canOpenAfterReturn(launchEligible = true, alarmPlaying = false))
            AppOpenInteractionGuard.release(draft)
            assertTrue(AppOpenInteractionGuard.canOpenAfterReturn(launchEligible = true, alarmPlaying = false))
            assertFalse(AppOpenInteractionGuard.canOpenAfterReturn(launchEligible = false, alarmPlaying = false))
        } finally {
            AppOpenInteractionGuard.release(dialog)
            AppOpenInteractionGuard.release(draft)
        }
    }
}
