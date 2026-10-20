package com.example

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import com.example.data.model.PacSlot
import com.example.ui.components.PacOfficialMatrixCard
import com.example.ui.components.BarinasAddressDialog
import com.example.ui.components.OnboardingFlowDialog
import com.example.ui.components.UpdatePromptContent
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppUpdateInfo
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w320dp-h480dp-port-mdpi", sdk = [36])
class CompactDialogUiTest {
    @get:Rule val compose = createComposeRule()

    @Composable
    private fun LargeText(content: @Composable () -> Unit) {
        val density = LocalDensity.current
        val configuration = Configuration(LocalConfiguration.current).apply { fontScale = 1.6f }
        CompositionLocalProvider(
            LocalConfiguration provides configuration,
            LocalDensity provides Density(density.density, fontScale = 1.6f)
        ) { MyApplicationTheme(darkTheme = true, content = content) }
    }

    @Test
    fun weeklyMatrixShowsSeventhSlotAndMissingCellsAsUnassigned() {
        val slots = List(7) { index -> PacSlot(index, "Franja ${index + 1}", index * 3, index * 3 + 2) }
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                PacOfficialMatrixCard("Semana", slots = slots, matrix = List(6) { List(7) { "B" } })
            }
        }
        compose.onNodeWithText("Franja 7").assertExists()
        compose.onAllNodesWithText("-", useUnmergedTree = true).assertCountEquals(7)
    }

    @Test
    fun registerSectorSaveRemainsReachableWithLargeText() {
        var saved: List<String>? = null
        var dismissCount = 0
        compose.setContent {
            LargeText {
                BarinasAddressDialog(
                    isOpen = true,
                    currentAddress = "Urbanización con un nombre que ocupa varias líneas en una pantalla pequeña",
                    onLocationSelected = {},
                    onDismiss = { dismissCount++ },
                    onRegisterNewCommunity = { name, municipality, parish, block, circuit -> saved = listOf(name, municipality, parish, block, circuit) }
                )
            }
        }
        compose.onNodeWithTag("address_register_tab").performScrollTo().performClick()
        compose.onNodeWithTag("register_sector_name").performScrollTo().performTextInput("Comunidad La Esperanza del Municipio Barinas")
        compose.onNodeWithTag("register_sector_button").assertIsDisplayed().assertIsEnabled().performClick()
        assertEquals("Comunidad La Esperanza del Municipio Barinas", saved?.first())
        assertEquals("Bloque C", saved?.get(3))
        assertEquals(1, dismissCount)
        compose.onNodeWithTag("barinas_address_dialog").captureRoboImage(filePath = "build/reports/compact-ui/register-320-large-text.png")
    }

    @Test
    @Config(qualifiers = "w480dp-h320dp-land-mdpi", sdk = [36])
    fun updateActionsRemainReachableWithLongReleaseNotesInLandscape() {
        var updates = 0
        var dismissals = 0
        compose.setContent {
            LargeText {
                UpdatePromptContent(
                    AppUpdateInfo(8, "1.3.2", "Mejoras de mapas, avisos y horarios. ".repeat(50), isMandatory = false),
                    onDismiss = { dismissals++ },
                    onUpdate = { updates++ }
                )
            }
        }
        compose.onNodeWithTag("update_download_button").assertIsDisplayed().performClick()
        compose.onNodeWithTag("update_later_button").assertIsDisplayed().performClick()
        assertEquals(1, updates)
        assertEquals(1, dismissals)
        compose.onNodeWithTag("update_prompt_dialog").captureRoboImage(filePath = "build/reports/compact-ui/update-landscape-large-text.png")
    }

    @Test
    fun welcomeCanBeSkippedWithoutOpeningPermissionSetup() {
        var skipped = 0
        var selected = 0
        compose.setContent {
            LargeText {
                OnboardingFlowDialog(true, onComplete = { selected++ }, onSkip = { skipped++ })
            }
        }
        compose.onNodeWithTag("onboarding_skip").assertIsDisplayed().performClick()
        compose.onNodeWithTag("onboarding_tutorial").assertDoesNotExist()
        assertEquals(1, skipped)
        assertEquals(0, selected)
    }

    @Test
    fun longSectorNameKeepsChangeActionAndIrregularReportUsable() {
        val sectorName = "Urbanización Comunidad Socialista La Gran Esperanza de Barinas, segunda etapa y sectores vecinos"
        val sector = Sector("compact", sectorName, "Circuito largo de prueba", ServiceStatus.IRREGULAR_OUTAGE, 0f, 1, 100, 0, "Bloque C", emptyList(), isCommunity = true)
        var changes = 0
        var reportType: String? = null
        compose.setContent {
            LargeText {
                DashboardScreen(
                    sectors = listOf(sector), selectedSector = sector,
                    prediction = null, unsyncedCount = 0,
                    onSectorSelected = {},
                    onChangeAddressClicked = { changes++ },
                    onReportStatus = { _, type, _, _ -> reportType = type }
                )
            }
        }
        compose.onNodeWithTag("dashboard_sector_name").performScrollTo().assertTextEquals(sectorName).assertIsDisplayed()
        compose.onNodeWithText("Cambiar").performScrollTo().performClick()
        assertEquals(1, changes)
        compose.onNodeWithTag("dashboard_screen").performScrollToNode(hasTestTag("report_irregular_fault_button"))
        compose.onRoot().captureRoboImage(filePath = "build/reports/compact-ui/report-320-large-text.png")
        compose.onNodeWithTag("report_irregular_fault_button").assertIsDisplayed().performClick()
        assertEquals("FALLA_IRREGULAR", reportType)
        assertTrue(compose.onNodeWithTag("report_irregular_fault_button").fetchSemanticsNode().boundsInRoot.width > 200f)
    }
}
