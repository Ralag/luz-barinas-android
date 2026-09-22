package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.data.model.BarinasLocation
import com.example.data.model.BarinasLocationsCatalog
import com.example.data.model.BroadcastNotice
import com.example.data.model.CitizenReport
import com.example.data.model.DayTurnAudit
import com.example.data.model.OutagePrediction
import com.example.data.model.PacScheduleData
import com.example.data.model.PacSlot
import com.example.data.model.RebalanceResult
import com.example.data.model.Sector
import com.example.data.model.ServiceStatus
import com.example.data.PacSchedulePrefs
import com.example.data.repository.EnergyRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LuzBarinasUiState(
    val sectors: List<Sector> = emptyList(),
    val selectedSector: Sector? = null,
    val userAddress: String? = null,
    val isOnboardingOpen: Boolean = false,
    val prediction: OutagePrediction? = null,
    val unsyncedReportsCount: Int = 0,
    val recentReports: List<CitizenReport> = emptyList(),
    val activeFilter: ServiceStatus? = null,
    val currentTab: Int = 0,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val isBlackoutSimulationMode: Boolean = false,
    val isDarkMode: Boolean = false,
    val scheduleVersion: Long = 0L,
    val isAdminOpen: Boolean = false,
    val lastRebalanceResult: RebalanceResult? = null,
    val pacMatrix: List<List<String>> = PacScheduleData.getMatrixSnapshot(),
    val pacSlots: List<PacSlot> = PacScheduleData.getSlotsSnapshot(),
    val sectorsA: List<String> = PacScheduleData.SECTORS_BLOQUE_A.toList(),
    val sectorsB: List<String> = PacScheduleData.SECTORS_BLOQUE_B.toList(),
    val sectorsC: List<String> = PacScheduleData.SECTORS_BLOQUE_C.toList(),
    val sectorsD: List<String> = PacScheduleData.SECTORS_BLOQUE_D.toList(),
    val activeBroadcastNotice: BroadcastNotice? = null,
    val updateAvailable: AppUpdateInfo? = null,
    val donationUrl: String? = null
)

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val isMandatory: Boolean
)

class LuzBarinasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EnergyRepository(application)
    private val userPrefs = application.getSharedPreferences("luz_barinas_user_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        LuzBarinasUiState(
            isLoading = true,
            userAddress = userPrefs.getString("saved_address", null),
            isOnboardingOpen = !userPrefs.getBoolean("onboarding_done", false)
        )
    )
    val uiState: StateFlow<LuzBarinasUiState> = _uiState.asStateFlow()

    init {
        PacSchedulePrefs.loadSchedule(getApplication())
        _uiState.update {
            it.copy(
                pacMatrix = PacScheduleData.getMatrixSnapshot(),
                pacSlots = PacScheduleData.getSlotsSnapshot(),
                scheduleVersion = PacScheduleData.scheduleVersion
            )
        }

        // Start real-time cloud sync with Firestore (live citizen reports & PAC updates)
        repository.cloudSync.startRealtimeSync()

        viewModelScope.launch {
            repository.initializePreloadedDataIfEmpty()
        }

        // Collect sectors reactively from Room
        viewModelScope.launch {
            repository.allSectorsFlow.collectLatest { list ->
                _uiState.update { current ->
                    val savedSectorId = userPrefs.getString("saved_sector_id", null)
                    val currentSelected = current.selectedSector
                    val updatedSelected = (if (currentSelected != null) list.find { it.id == currentSelected.id } else null)
                        ?: (if (savedSectorId != null) list.find { it.id == savedSectorId } else null)
                        ?: list.find { it.id == "sec_a_alto_barinas_1" }
                        ?: list.firstOrNull()

                    current.copy(
                        sectors = list,
                        selectedSector = updatedSelected,
                        isLoading = false
                    )
                }

                // Refresh prediction whenever sectors change
                _uiState.value.selectedSector?.let { sector ->
                    refreshPrediction(sector.id)
                    repository.cloudSync.listenToSector(sector.id)
                }
            }
        }

        // Collect unsynced reports count
        viewModelScope.launch {
            repository.unsyncedCountFlow.collectLatest { count ->
                _uiState.update { it.copy(unsyncedReportsCount = count) }
            }
        }

        // Collect recent reports
        viewModelScope.launch {
            repository.allReportsFlow.collectLatest { reports ->
                _uiState.update { it.copy(recentReports = reports) }
            }
        }

        // Collect real-time PAC schedule updates from cloud (instantly updates UI without restart)
        viewModelScope.launch {
            repository.pacScheduleUpdatedFlow.collectLatest { _ ->
                _uiState.update { buildPacStateCopy() }
                _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
            }
        }

        // Collect official broadcast notice updates from cloud (in-app banner & alert)
        viewModelScope.launch {
            repository.broadcastNoticeFlow.collectLatest { notice ->
                _uiState.update { it.copy(activeBroadcastNotice = notice) }
            }
        }

        // Collect OTA update info
        viewModelScope.launch {
            repository.updateInfoFlow.collectLatest { updateInfo ->
                _uiState.update { it.copy(updateAvailable = updateInfo) }
            }
        }
    }

    fun selectSector(sector: Sector) {
        userPrefs.edit()
            .putString("saved_sector_id", sector.id)
            .putString("saved_address", sector.name)
            .putString("saved_sector_block", sector.rotationBlock.uppercase().replace("BLOQUE", "").trim())
            .apply()
        _uiState.update { it.copy(selectedSector = sector, userAddress = sector.name) }
        refreshPrediction(sector.id)
        repository.cloudSync.listenToSector(sector.id)
    }

    fun selectSectorByName(name: String) {
        val match = _uiState.value.sectors.find {
            it.name.contains(name, ignoreCase = true) || name.contains(it.name, ignoreCase = true)
        }
        if (match != null) {
            selectSector(match)
        }
    }

    fun setFilter(status: ServiceStatus?) {
        _uiState.update { it.copy(activeFilter = status) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(currentTab = index) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun reportPowerStatus(
        hasPower: Boolean,
        reportType: String = if (hasPower) "NORMAL" else "SIN_LUZ",
        voltage: Float? = if (hasPower) 118f else 0f
    ) {
        val targetSector = _uiState.value.selectedSector ?: return
        viewModelScope.launch {
            val result = repository.submitReport(
                sectorId = targetSector.id,
                sectorName = targetSector.name,
                hasPower = hasPower,
                reportType = reportType,
                voltage = voltage
            )

            if (result.isSuccess) {
                val actionDesc = if (hasPower) "Con Luz (118V)" else "Sin Luz (Corte reportado)"
                _uiState.update {
                    it.copy(
                        userMessage = "✅ Telemetría para ${targetSector.name} registrada como '$actionDesc'."
                    )
                }
                refreshPrediction(targetSector.id)
            } else {
                _uiState.update {
                    it.copy(userMessage = "⚠️ Guardado localmente en cola offline.")
                }
            }
        }
    }

    fun syncPendingReportsNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val syncedCount = repository.syncPendingReportsNow()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userMessage = if (syncedCount > 0) {
                        "⚡ Se sincronizaron $syncedCount reportes con el servidor."
                    } else {
                        "ℹ️ Sin conexión al servidor o no hay reportes pendientes."
                    }
                )
            }
        }
    }

    fun triggerPushAlertSimulation() {
        val sector = _uiState.value.selectedSector ?: return
        NotificationHelper.showInteractiveOutageAlert(
            context = getApplication(),
            sectorId = sector.id,
            sectorName = sector.name,
            circuitCode = sector.circuitCode
        )
        _uiState.update {
            it.copy(
                userMessage = "📲 Notificación interactiva enviada. Baja la barra de notificaciones para probar los botones de 1 toque."
            )
        }
    }

    private fun buildPacStateCopy(userMsg: String? = null): LuzBarinasUiState {
        val current = _uiState.value
        return current.copy(
            scheduleVersion = PacScheduleData.scheduleVersion,
            pacMatrix = PacScheduleData.getMatrixSnapshot(),
            pacSlots = PacScheduleData.getSlotsSnapshot(),
            sectorsA = PacScheduleData.SECTORS_BLOQUE_A.toList(),
            sectorsB = PacScheduleData.SECTORS_BLOQUE_B.toList(),
            sectorsC = PacScheduleData.SECTORS_BLOQUE_C.toList(),
            sectorsD = PacScheduleData.SECTORS_BLOQUE_D.toList(),
            userMessage = userMsg ?: current.userMessage
        )
    }

    fun setAdminOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isAdminOpen = isOpen) }
    }

    fun updateMatrixCell(slotIdx: Int, dayIdx: Int, block: String) {
        PacScheduleData.updateCell(slotIdx, dayIdx, block)
        PacSchedulePrefs.saveSchedule(getApplication())
        _uiState.update { buildPacStateCopy() }
        _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
        viewModelScope.launch { repository.cloudSync.publishPacScheduleToCloud() }
    }

    fun updateSlot(slotIdx: Int, newLabel: String, startHour: Int, endHour: Int) {
        PacScheduleData.updateSlot(slotIdx, newLabel, startHour, endHour)
        PacSchedulePrefs.saveSchedule(getApplication())
        _uiState.update { buildPacStateCopy() }
        _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
        viewModelScope.launch { repository.cloudSync.publishPacScheduleToCloud() }
    }

    fun deleteSlot(slotIdx: Int) {
        val ok = PacScheduleData.deleteSlot(slotIdx)
        if (ok) {
            PacSchedulePrefs.saveSchedule(getApplication())
            _uiState.update { buildPacStateCopy("🗑️ Turno eliminado correctamente.") }
            _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
            viewModelScope.launch { repository.cloudSync.publishPacScheduleToCloud() }
        }
    }

    fun addSlot(label: String, startHour: Int, endHour: Int) {
        PacScheduleData.addSlot(label, startHour, endHour)
        PacSchedulePrefs.saveSchedule(getApplication())
        _uiState.update { buildPacStateCopy("➕ Nuevo turno añadido al cronograma.") }
        _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
        viewModelScope.launch { repository.cloudSync.publishPacScheduleToCloud() }
    }

    fun addSectorToBlock(sectorName: String, blockCode: String) {
        PacScheduleData.addSectorToBlock(sectorName, blockCode)
        PacSchedulePrefs.saveSchedule(getApplication())
        _uiState.update { buildPacStateCopy("Sector '$sectorName' asignado a Bloque $blockCode.") }
        _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
        viewModelScope.launch { repository.cloudSync.publishPacScheduleToCloud() }
    }

    fun removeSectorFromBlock(sectorName: String, blockCode: String) {
        PacScheduleData.removeSectorFromBlock(sectorName, blockCode)
        PacSchedulePrefs.saveSchedule(getApplication())
        _uiState.update { buildPacStateCopy("Sector '$sectorName' removido de Bloque $blockCode.") }
        _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
        viewModelScope.launch { repository.cloudSync.publishPacScheduleToCloud() }
    }

    fun reassignSector(sectorName: String, targetBlock: String) {
        PacScheduleData.reassignSector(sectorName, targetBlock)
        PacSchedulePrefs.saveSchedule(getApplication())
        _uiState.update { buildPacStateCopy("Sector '$sectorName' reasignado a Bloque $targetBlock.") }
        _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
        viewModelScope.launch { repository.cloudSync.publishPacScheduleToCloud() }
    }

    fun applyDoubleTurnPreset() {
        PacScheduleData.applyDoubleTurnPreset()
        PacSchedulePrefs.saveSchedule(getApplication())
        _uiState.update {
            buildPacStateCopy("⚡ Esquema: 2 Cortes Diarios (8 hrs por bloque) [ACTIVO].")
        }
        _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
        viewModelScope.launch { repository.cloudSync.publishPacScheduleToCloud() }
    }

    fun applySingleTurnPreset() {
        PacScheduleData.applySingleTurnPreset()
        PacSchedulePrefs.saveSchedule(getApplication())
        _uiState.update {
            buildPacStateCopy("⚡ Esquema: 1 Solo Corte Diario (4 hrs por bloque) [ACTIVO].")
        }
        _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
        viewModelScope.launch { repository.cloudSync.publishPacScheduleToCloud() }
    }

    fun resetScheduleToDefault() {
        applyDoubleTurnPreset()
    }

    fun deleteTurnAndRebalance(slotIdx: Int, dayIdx: Int) {
        val result = PacScheduleData.deleteTurnAndRebalance(slotIdx, dayIdx)
        PacSchedulePrefs.saveSchedule(getApplication())
        _uiState.update {
            buildPacStateCopy("🔄 ${result.explanation}").copy(lastRebalanceResult = result)
        }
        _uiState.value.selectedSector?.let { refreshPrediction(it.id) }
        viewModelScope.launch { repository.cloudSync.publishPacScheduleToCloud() }
    }

    fun registerCommunityLocation(
        name: String,
        municipio: String = "Barinas",
        parroquia: String,
        block: String,
        circuit: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.registerCommunityLocation(name, municipio, parroquia, block, circuit)
            if (result.isSuccess) {
                val newSector = result.getOrNull()
                val loc = BarinasLocation(
                    id = "loc_com_" + name.lowercase().replace("[^a-z0-9]".toRegex(), "_"),
                    name = name.trim(),
                    type = "Comunidad",
                    parroquia = parroquia.trim(),
                    block = block.trim(),
                    circuitCode = circuit.trim().ifEmpty { "Circuito $parroquia" },
                    sectorEntityId = newSector?.id ?: "sec_a_alto_barinas_1",
                    description = "Comunidad en $parroquia • $block",
                    keywords = listOf(name.lowercase(), municipio.lowercase(), parroquia.lowercase(), block.lowercase()),
                    municipio = municipio.trim()
                )
                BarinasLocationsCatalog.addCustomLocation(loc)
                setUserLocation(loc)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "✅ ¡Comunidad '${name}' registrada y sincronizada en Firebase!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "⚠️ No se pudo registrar la comunidad en este momento."
                    )
                }
            }
        }
    }

    fun setOnboardingOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isOnboardingOpen = isOpen) }
    }

    fun setUserLocation(location: BarinasLocation) {
        // Match sector in current list or find appropriate sector for block
        val cleanBlock = location.block.replace("Bloque", "").trim().uppercase()
        val matchingSector = _uiState.value.sectors.find { it.id == location.sectorEntityId }
            ?: _uiState.value.sectors.find { it.name.contains(location.name, ignoreCase = true) }
            ?: _uiState.value.sectors.find { it.rotationBlock.contains(cleanBlock, ignoreCase = true) }
            ?: _uiState.value.selectedSector

        val resolvedSectorId = matchingSector?.id ?: location.sectorEntityId
        userPrefs.edit()
            .putString("saved_address", location.name)
            .putString("saved_block", location.block)
            .putString("saved_sector_id", resolvedSectorId)
            .putBoolean("onboarding_done", true)
            .apply()

        _uiState.update { current ->
            current.copy(
                userAddress = location.name,
                isOnboardingOpen = false,
                selectedSector = matchingSector ?: current.selectedSector,
                userMessage = "📍 Ubicación fijada en ${location.name} • ${location.block}."
            )
        }

        matchingSector?.let { 
            refreshPrediction(it.id) 
            repository.cloudSync.listenToSector(it.id)
        }
    }

    fun publishScheduleNotification() {
        val sector = _uiState.value.selectedSector ?: return

        // Instant cloud propagation to all citizens in Barinas
        viewModelScope.launch {
            repository.cloudSync.publishPacScheduleToCloud()
        }

        NotificationHelper.showInteractiveOutageAlert(
            context = getApplication(),
            sectorId = sector.id,
            sectorName = "Nuevo Cronograma PAC Barinas",
            circuitCode = "Semana Actualizada"
        )
        _uiState.update {
            it.copy(
                userMessage = "📢 Horarios PAC publicados y sincronizados en la nube."
            )
        }
    }

    fun dismissBroadcastNotice() {
        _uiState.update { it.copy(activeBroadcastNotice = null) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun refreshPrediction(sectorId: String) {
        viewModelScope.launch {
            val prediction = repository.getPredictionForSector(sectorId)
            _uiState.update { it.copy(prediction = prediction) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.cloudSync.stopRealtimeSync()
    }
}

