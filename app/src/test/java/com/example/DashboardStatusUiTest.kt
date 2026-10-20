package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.model.PacScheduleData
import com.example.data.model.PacScheduleSnapshot
import com.example.data.model.PacSlot
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-port-mdpi", sdk = [36])
class DashboardStatusUiTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var previous: PacScheduleSnapshot

    @Before
    fun publishActivePacForTheWholeDay() {
        previous = PacScheduleData.getScheduleSnapshot()
        PacScheduleData.applySnapshot(PacScheduleSnapshot(
            slots = listOf(PacSlot(0, "00:00 a 12:00", 0, 12), PacSlot(1, "12:00 a 24:00", 12, 24)),
            matrix = List(2) { List(7) { "A" } }
        ), 123L)
    }

    @After
    fun restoreSchedule() { PacScheduleData.applySnapshot(previous, previous.revision) }

    private fun showStatus(status: ServiceStatus) {
        val sector = Sector("status", "Sector de prueba", "Circuito", status, 0f, 0, 0, 0,
            "Bloque A", emptyList())
        compose.setContent {
            MyApplicationTheme(darkTheme = true) {
                DashboardScreen(listOf(sector), sector, prediction = null, unsyncedCount = 0,
                    onSectorSelected = {}, onReportStatus = { _, _, _, _ -> })
            }
        }
        compose.onNodeWithTag("today_live_status_banner").performScrollTo()
        compose.onNodeWithText("PAC en curso:", substring = true).assertIsDisplayed()
        compose.onNodeWithText("🔴 Turno de Corte Programado").assertDoesNotExist()
    }

    @Test
    fun unknownServiceIsNotReportedAsAnObservedOutageDuringPac() {
        showStatus(ServiceStatus.UNKNOWN)
        compose.onNodeWithText("Sin reporte actual del servicio").assertIsDisplayed()
        compose.onNodeWithText("Con luz según el último reporte").assertDoesNotExist()
    }

    @Test
    fun normalCommunityReportRemainsDistinctFromTheActivePac() {
        showStatus(ServiceStatus.NORMAL)
        compose.onNodeWithText("Con luz según el último reporte").assertIsDisplayed()
        compose.onNodeWithText("Sin reporte actual del servicio").assertDoesNotExist()
    }

    @Test
    fun irregularFaultKeepsItsLabelDuringAnActiveScheduledCut() {
        showStatus(ServiceStatus.IRREGULAR_OUTAGE)
        compose.onNodeWithText("Falla irregular reportada").assertIsDisplayed()
        compose.onNodeWithText("Con luz según el último reporte").assertDoesNotExist()
    }
}
