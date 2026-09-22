package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.components.PacSettingsDialog
import com.example.notification.PacAlarmScheduler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import com.example.notification.NotificationHelper
import com.example.ui.components.AdaptiveBannerAd
import com.example.ui.components.BarinasAddressDialog
import com.example.ui.components.PacScheduleView
import com.example.ui.components.UpdatePromptDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MapScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LuzBarinasViewModel


class MainActivity : ComponentActivity() {

    private val viewModel: LuzBarinasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel for alerts
        NotificationHelper.createNotificationChannel(this)

        // Initialize AdMob SDK
        com.example.ui.components.initializeAdMob(this)

        // Start predictive notification worker (checks every 30 min for upcoming outages)
        com.example.worker.PredictiveNotificationWorker.enqueue(this)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = uiState.isDarkMode) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: LuzBarinasViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Request notification permissions on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Snackbar alerts
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Alert & Alarm Settings State
    var isSettingsOpen by remember { mutableStateOf(false) }

    val currentBlock = remember(uiState.selectedSector) {
        uiState.selectedSector?.rotationBlock?.uppercase()?.replace("BLOQUE", "")?.trim() ?: "A"
    }

    // Synchronize exact PAC Alarm whenever block or sector changes
    LaunchedEffect(uiState.selectedSector) {
        PacAlarmScheduler.scheduleNextAlarm(context)
    }

    // PAC Alert & Alarm Settings Dialog
    PacSettingsDialog(
        isOpen = isSettingsOpen,
        userBlock = currentBlock,
        userSectorName = uiState.userAddress ?: "Barinas",
        onDismiss = { isSettingsOpen = false }
    )

    // Permissions Onboarding Flow
    var showAddressDialog by remember { mutableStateOf(false) }

    com.example.ui.components.OnboardingFlowDialog(
        isOpen = uiState.isOnboardingOpen && !showAddressDialog,
        onComplete = {
            showAddressDialog = true
        }
    )

    // Onboarding / Location Selector Dialog (Barinas Locations Catalog)
    BarinasAddressDialog(
        isOpen = (uiState.isOnboardingOpen && showAddressDialog) || (uiState.userAddress == null && !uiState.isOnboardingOpen),
        currentAddress = uiState.userAddress,
        onLocationSelected = { location ->
            viewModel.setUserLocation(location)
            showAddressDialog = false
        },
        onDismiss = {
            viewModel.setOnboardingOpen(false)
            showAddressDialog = false
        },
        onRegisterNewCommunity = { name, municipio, parroquia, block, circuit ->
            viewModel.registerCommunityLocation(name, municipio, parroquia, block, circuit)
            showAddressDialog = false
        }
    )

    var forceShowUpdateDialog by remember { mutableStateOf(false) }

    // App Update OTA Dialog
    var dismissedUpdateVersion by remember { mutableStateOf<Int?>(null) }
    val update = uiState.updateAvailable
    if ((update != null && dismissedUpdateVersion != update.versionCode) || forceShowUpdateDialog) {
        val displayUpdate = update ?: com.example.ui.viewmodel.AppUpdateInfo(
            versionCode = com.example.BuildConfig.VERSION_CODE,
            versionName = com.example.BuildConfig.VERSION_NAME,
            releaseNotes = "Estás utilizando la versión actual.\nSi deseas forzar una actualización manual o verificar detalles técnicos, presiona Descargar e Instalar.",
            downloadUrl = "https://github.com/Ralag/luz-barinas-android/releases",
            isMandatory = false
        )
        UpdatePromptDialog(
            updateInfo = displayUpdate,
            onDismiss = {
                dismissedUpdateVersion = displayUpdate.versionCode
                forceShowUpdateDialog = false
            }
        )
    }

    // Back navigation
    BackHandler(enabled = uiState.currentTab != 0) {
        viewModel.setTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { forceShowUpdateDialog = true }
                    ) {
                        Text(
                            text = "⚡ PAC Barinas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Control Eléctrico Oficial • En vivo",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Theme Switcher (Sun / Moon)
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("action_toggle_theme")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (uiState.isDarkMode) "Activar modo claro" else "Activar modo oscuro",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Alert & Alarm Settings Dialog
                    IconButton(
                        onClick = { isSettingsOpen = true },
                        modifier = Modifier.testTag("action_alarm_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Configurar Alertas y Alarma PAC",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Location / Address Selector
                    IconButton(
                        onClick = { viewModel.setOnboardingOpen(true) },
                        modifier = Modifier.testTag("action_select_location")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Configurar Ubicación y Bloque",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Test push notification simulation
                    IconButton(
                        onClick = { viewModel.triggerPushAlertSimulation() },
                        modifier = Modifier.testTag("action_test_push")
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Simular Alerta de Corte",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Column {
                AdaptiveBannerAd()
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    // Tab 0: Hoy (Estado actual, próximo corte y reporte rápido)
                    NavigationBarItem(
                        selected = uiState.currentTab == 0,
                        onClick = { viewModel.setTab(0) },
                        icon = {
                            Icon(
                                Icons.Outlined.Bolt,
                                contentDescription = "Hoy",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Hoy", fontSize = 11.sp, fontWeight = if (uiState.currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Tab 1: Horarios (Cronograma semanal y mensual oficial por bloques)
                    NavigationBarItem(
                        selected = uiState.currentTab == 1,
                        onClick = { viewModel.setTab(1) },
                        icon = {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = "Horarios",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Horarios", fontSize = 11.sp, fontWeight = if (uiState.currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    /*
                    // Tab 2: Mapa de Circuitos (Deshabilitado temporalmente)
                    NavigationBarItem(
                        selected = uiState.currentTab == 2,
                        onClick = { viewModel.setTab(2) },
                        icon = {
                            Icon(
                                Icons.Outlined.Map,
                                contentDescription = "Mapa",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text("Mapa", fontSize = 11.sp, fontWeight = if (uiState.currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    */
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = uiState.currentTab,
                animationSpec = tween(200),
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    0 -> DashboardScreen(
                        sectors = uiState.sectors,
                        selectedSector = uiState.selectedSector,
                        userAddress = uiState.userAddress,
                        prediction = uiState.prediction,
                        unsyncedCount = uiState.unsyncedReportsCount,
                        activeNotice = uiState.activeBroadcastNotice,
                        onDismissNotice = { viewModel.dismissBroadcastNotice() },
                        onSectorSelected = { viewModel.selectSector(it) },
                        onReportStatus = { hasPower, reportType, voltage ->
                            viewModel.reportPowerStatus(hasPower, reportType, voltage)
                        },
                        onChangeAddressClicked = { viewModel.setOnboardingOpen(true) },
                        onNavigateToSchedule = { viewModel.setTab(1) },
                        onOpenAlarmSettings = { isSettingsOpen = true }
                    )
                    1 -> PacScheduleView(
                        selectedSector = uiState.selectedSector,
                        pacMatrix = uiState.pacMatrix,
                        pacSlots = uiState.pacSlots,
                        onSectorClicked = { sectorName ->
                            viewModel.selectSectorByName(sectorName)
                            viewModel.setTab(0)
                        }
                    )
                    2 -> MapScreen(
                        sectors = uiState.sectors,
                        selectedSector = uiState.selectedSector,
                        activeFilter = uiState.activeFilter,
                        onSectorSelected = { viewModel.selectSector(it) },
                        onFilterChanged = { viewModel.setFilter(it) },
                        onQuickReport = { hasPower, reportType, voltage ->
                            viewModel.reportPowerStatus(hasPower, reportType, voltage)
                        }
                    )
                }
            }
        }
    }
}
