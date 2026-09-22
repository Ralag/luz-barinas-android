package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.PacSchedulePrefs
import com.example.data.local.AppDatabase
import com.example.data.model.BroadcastNotice
import com.example.data.model.PacScheduleData
import com.example.data.model.PacSlot
import com.example.notification.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Cloud Synchronization Repository using Firebase Firestore.
 * 
 * Provides:
 * 1. Real-time sector status updates from citizen reports across Barinas.
 * 2. Instant propagation of PAC schedule changes made by the admin to all users.
 * 3. Citizen report syncing with offline-first support.
 */
class CloudSyncRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var sectorsListener: ListenerRegistration? = null
    private var pacScheduleListener: ListenerRegistration? = null
    private var broadcastListener: ListenerRegistration? = null

    private val _broadcastNoticeFlow = MutableStateFlow<BroadcastNotice?>(null)
    val broadcastNoticeFlow: StateFlow<BroadcastNotice?> = _broadcastNoticeFlow.asStateFlow()

    private val _updateInfoFlow = MutableStateFlow<com.example.ui.viewmodel.AppUpdateInfo?>(null)
    val updateInfoFlow: StateFlow<com.example.ui.viewmodel.AppUpdateInfo?> = _updateInfoFlow.asStateFlow()

    private val _pacScheduleUpdatedFlow = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val pacScheduleUpdatedFlow: SharedFlow<Long> = _pacScheduleUpdatedFlow.asSharedFlow()

    private val syncPrefs = context.getSharedPreferences("cloud_sync_prefs", Context.MODE_PRIVATE)

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not available. Running in offline mode: ${e.message}")
            null
        }
    }

    /**
     * Starts real-time listeners for live updates from the cloud.
     * Call once when the app or ViewModel starts.
     */
    fun startRealtimeSync() {
        val db = firestore ?: return

        // 1. Real-time listener for PAC schedule changes published by admin
        try {
            pacScheduleListener = db.collection("app_config")
                .document("pac_schedule")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error listening to PAC schedule updates", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        scope.launch {
                            applyRemotePacSchedule(snapshot.data ?: emptyMap())
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach PAC schedule listener", e)
        }

        // 2. Real-time listener for sector status updates
        try {
            sectorsListener = db.collection("sectors")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(TAG, "Error listening to sector updates", error)
                        return@addSnapshotListener
                    }
                    if (snapshots != null && !snapshots.isEmpty) {
                        scope.launch {
                            val sectorDao = database.sectorDao()
                            for (doc in snapshots.documents) {
                                val sectorId = doc.id
                                val existing = sectorDao.getSectorById(sectorId)
                                if (existing != null) {
                                    val status = doc.getString("status") ?: existing.status
                                    val voltage = doc.getDouble("voltage")?.toFloat() ?: existing.voltage
                                    val count = doc.getLong("confirmedReportsCount")?.toInt() ?: existing.confirmedReportsCount
                                    val pct = doc.getLong("withoutPowerPercentage")?.toInt() ?: existing.withoutPowerPercentage
                                    val lastUpdated = doc.getLong("lastUpdatedMillis") ?: existing.lastUpdatedMillis

                                    sectorDao.updateSector(
                                        existing.copy(
                                            status = status,
                                            voltage = voltage,
                                            confirmedReportsCount = count,
                                            withoutPowerPercentage = pct,
                                            lastUpdatedMillis = lastUpdated
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach sectors listener", e)
        }

        // 3. Real-time listener for emergency broadcast notices
        try {
            broadcastListener = db.collection("app_config")
                .document("broadcast_notice")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error listening to broadcast updates", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val active = snapshot.getBoolean("active") ?: false
                        if (active) {
                            val title = snapshot.getString("title") ?: "Aviso Oficial"
                            val message = snapshot.getString("message") ?: ""
                            val level = snapshot.getString("level") ?: "INFO"
                            val timestamp = snapshot.getLong("timestamp") ?: System.currentTimeMillis()

                            val notice = BroadcastNotice(title, message, level, timestamp, active)
                            _broadcastNoticeFlow.value = notice

                            val lastSeen = syncPrefs.getLong("last_seen_broadcast_timestamp", 0L)
                            if (timestamp > lastSeen) {
                                syncPrefs.edit().putLong("last_seen_broadcast_timestamp", timestamp).apply()
                                NotificationHelper.showBroadcastNoticeNotification(context, title, message, level)
                            }
                        } else {
                            _broadcastNoticeFlow.value = null
                        }
                    } else {
                        _broadcastNoticeFlow.value = null
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach broadcast notice listener", e)
        }

        // 4. Real-time listener for OTA updates
        try {
            db.collection("app_config")
                .document("version")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val versionCode = snapshot.getLong("versionCode")?.toInt() ?: 1
                        val currentVersionCode = com.example.BuildConfig.VERSION_CODE
                        
                        if (versionCode > currentVersionCode) {
                            _updateInfoFlow.value = com.example.ui.viewmodel.AppUpdateInfo(
                                versionCode = versionCode,
                                versionName = snapshot.getString("versionName") ?: "1.0",
                                releaseNotes = snapshot.getString("releaseNotes") ?: "Nueva actualización disponible.",
                                downloadUrl = snapshot.getString("apkDownloadUrl") ?: "",
                                isMandatory = snapshot.getBoolean("isMandatory") ?: false
                            )
                        } else {
                            _updateInfoFlow.value = null
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach version listener", e)
        }
    }

    /**
     * Publishes a citizen power report to Firestore in real time.
     */
    suspend fun uploadCitizenReport(
        sectorId: String,
        sectorName: String,
        hasPower: Boolean,
        reportType: String,
        voltage: Float?
    ): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false

        try {
            val reportData = hashMapOf(
                "sectorId" to sectorId,
                "sectorName" to sectorName,
                "hasPower" to hasPower,
                "reportType" to reportType,
                "voltage" to (voltage ?: if (hasPower) 118f else 0f),
                "timestamp" to System.currentTimeMillis(),
                "deviceOrigin" to "LuzBarinas_Citizen_App"
            )

            // Save report in collection
            db.collection("citizen_reports").add(reportData).await()

            // Optimistically update sector aggregates in Firestore
            val sectorRef = db.collection("sectors").document(sectorId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(sectorRef)
                if (snapshot.exists()) {
                    val currentCount = snapshot.getLong("confirmedReportsCount") ?: 0L
                    val currentPct = snapshot.getLong("withoutPowerPercentage") ?: 0L

                    val newCount = currentCount + 1
                    val newPct = if (hasPower) {
                        (currentPct - 10).coerceAtLeast(0)
                    } else {
                        (currentPct + 15).coerceAtMost(100)
                    }
                    val newStatus = if (hasPower) "NORMAL" else if (reportType == "AVERIA") "IRREGULAR_OUTAGE" else "SCHEDULED_OUTAGE"

                    transaction.update(sectorRef, mapOf(
                        "status" to newStatus,
                        "confirmedReportsCount" to newCount,
                        "withoutPowerPercentage" to newPct,
                        "lastUpdatedMillis" to System.currentTimeMillis()
                    ))
                }
            }.await()

            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upload citizen report to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Admin method: Publishes the active PAC schedule to Firestore so that
     * all citizen apps in Barinas update their schedules in real time.
     */
    suspend fun publishPacScheduleToCloud(): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false

        try {
            val matrixRows = PacScheduleData.activeMatrix.map { row ->
                mapOf("cells" to row.toList())
            }
            val slotsList = PacScheduleData.activeSlots.map { slot ->
                mapOf(
                    "timeLabel" to slot.timeLabel,
                    "startHour" to slot.startHour,
                    "endHour" to slot.endHour
                )
            }

            val payload = hashMapOf(
                "version" to PacScheduleData.scheduleVersion,
                "updatedAt" to System.currentTimeMillis(),
                "matrixRows" to matrixRows,
                "slots" to slotsList,
                "sectorsA" to PacScheduleData.SECTORS_BLOQUE_A.toList(),
                "sectorsB" to PacScheduleData.SECTORS_BLOQUE_B.toList(),
                "sectorsC" to PacScheduleData.SECTORS_BLOQUE_C.toList(),
                "sectorsD" to PacScheduleData.SECTORS_BLOQUE_D.toList()
            )

            db.collection("app_config")
                .document("pac_schedule")
                .set(payload, SetOptions.merge())
                .await()

            Log.i(TAG, "PAC Schedule successfully published to Firestore (version ${PacScheduleData.scheduleVersion})")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to publish PAC schedule to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Seed Firestore with initial sector catalog and active PAC schedule if empty.
     */
    suspend fun seedSectorsIfEmpty() = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val snapshot = db.collection("sectors").limit(1).get().await()
            if (snapshot.isEmpty) {
                val allEntities = database.sectorDao().getAllSectorsList()
                if (allEntities.isNotEmpty()) {
                    // Firestore batches allow up to 500 operations
                    val chunks = allEntities.chunked(400)
                    for (chunk in chunks) {
                        val batch = db.batch()
                        for (sector in chunk) {
                            val ref = db.collection("sectors").document(sector.id)
                            batch.set(ref, mapOf(
                                "id" to sector.id,
                                "name" to sector.name,
                                "circuitCode" to sector.circuitCode,
                                "status" to sector.status,
                                "voltage" to sector.voltage,
                                "confirmedReportsCount" to sector.confirmedReportsCount,
                                "withoutPowerPercentage" to sector.withoutPowerPercentage,
                                "rotationBlock" to sector.rotationBlock,
                                "lastUpdatedMillis" to sector.lastUpdatedMillis
                            ))
                        }
                        batch.commit().await()
                    }
                    Log.i(TAG, "Firestore successfully seeded with ${allEntities.size} sectors!")
                }
            }

            // Also seed active PAC schedule if missing
            val pacDoc = db.collection("app_config").document("pac_schedule").get().await()
            if (!pacDoc.exists()) {
                publishPacScheduleToCloud()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Sectors seeding skipped: ${e.message}")
        }
    }

    /**
     * Uploads a community-submitted sector denomination (e.g. Cincuentena 3, Parroquia El Carmen, Bloque C).
     * Builds crowdsourced geography in Firebase in real time.
     */
    suspend fun uploadCommunityLocation(
        name: String,
        municipio: String = "Barinas",
        parroquia: String,
        block: String,
        circuit: String
    ): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val cleanId = "sec_community_" + name.lowercase().replace("[^a-z0-9]".toRegex(), "_")
            val data = hashMapOf(
                "id" to cleanId,
                "name" to name.trim(),
                "municipio" to municipio.trim(),
                "parroquia" to parroquia.trim(),
                "block" to block.trim(),
                "circuitCode" to circuit.trim().ifEmpty { "Circuito Urbano" },
                "submittedAt" to System.currentTimeMillis(),
                "status" to "NORMAL",
                "voltage" to 118f,
                "confirmedReportsCount" to 1,
                "withoutPowerPercentage" to 0,
                "rotationBlock" to block.trim()
            )

            // 1. Save in community submissions
            db.collection("community_locations").document(cleanId).set(data, SetOptions.merge()).await()

            // 2. Also register directly as active sector in sectors collection
            db.collection("sectors").document(cleanId).set(data, SetOptions.merge()).await()

            Log.i(TAG, "Community location '$name' ($municipio, $parroquia, $block) successfully published to Firebase!")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upload community location: ${e.message}")
            false
        }
    }

    private fun applyRemotePacSchedule(data: Map<String, Any>) {
        try {
            val remoteVersion = (data["version"] as? Number)?.toLong() ?: 0L
            val remoteUpdatedAt = (data["updatedAt"] as? Number)?.toLong() ?: remoteVersion
            val lastApplied = syncPrefs.getLong("last_applied_pac_updated_at", 0L)

            if (remoteUpdatedAt > lastApplied || remoteVersion > PacScheduleData.scheduleVersion || (remoteUpdatedAt > 0L && PacScheduleData.scheduleVersion == 0L)) {
                // 1. Apply remote slots if present
                @Suppress("UNCHECKED_CAST")
                val slotsRaw = data["slots"] as? List<Map<String, Any>>
                if (!slotsRaw.isNullOrEmpty()) {
                    PacScheduleData.activeSlots.clear()
                    slotsRaw.forEachIndexed { idx, sMap ->
                        val label = sMap["timeLabel"] as? String ?: "Turno ${idx + 1}"
                        val startH = (sMap["startHour"] as? Number)?.toInt() ?: 0
                        val endH = (sMap["endHour"] as? Number)?.toInt() ?: 4
                        PacScheduleData.activeSlots.add(PacSlot(idx, label, startH, endH))
                    }
                }

                // 2. Apply remote matrix
                @Suppress("UNCHECKED_CAST")
                val matrixRows = data["matrixRows"] as? List<Map<String, Any>>
                if (matrixRows != null && matrixRows.isNotEmpty()) {
                    PacScheduleData.activeMatrix = Array(matrixRows.size) { i ->
                        @Suppress("UNCHECKED_CAST")
                        val cells = matrixRows[i]["cells"] as? List<String>
                        cells?.toTypedArray() ?: Array(7) { "-" }
                    }
                } else {
                    @Suppress("UNCHECKED_CAST")
                    val matrixRaw = data["matrix"] as? List<List<String>>
                    if (matrixRaw != null && matrixRaw.isNotEmpty()) {
                        PacScheduleData.activeMatrix = Array(matrixRaw.size) { i ->
                            matrixRaw[i].toTypedArray()
                        }
                    }
                }

                // 3. Apply remote sector assignments
                @Suppress("UNCHECKED_CAST")
                (data["sectorsA"] as? List<String>)?.let {
                    PacScheduleData.SECTORS_BLOQUE_A.clear()
                    PacScheduleData.SECTORS_BLOQUE_A.addAll(it)
                }
                @Suppress("UNCHECKED_CAST")
                (data["sectorsB"] as? List<String>)?.let {
                    PacScheduleData.SECTORS_BLOQUE_B.clear()
                    PacScheduleData.SECTORS_BLOQUE_B.addAll(it)
                }
                @Suppress("UNCHECKED_CAST")
                (data["sectorsC"] as? List<String>)?.let {
                    PacScheduleData.SECTORS_BLOQUE_C.clear()
                    PacScheduleData.SECTORS_BLOQUE_C.addAll(it)
                }
                @Suppress("UNCHECKED_CAST")
                (data["sectorsD"] as? List<String>)?.let {
                    PacScheduleData.SECTORS_BLOQUE_D.clear()
                    PacScheduleData.SECTORS_BLOQUE_D.addAll(it)
                }

                PacScheduleData.scheduleVersion = remoteUpdatedAt
                syncPrefs.edit().putLong("last_applied_pac_updated_at", remoteUpdatedAt).apply()

                // Persist locally
                PacSchedulePrefs.saveSchedule(context)
                Log.i(TAG, "Updated local PAC schedule to remote updatedAt $remoteUpdatedAt (version $remoteVersion)")

                // Notify UI state flow reactively
                _pacScheduleUpdatedFlow.tryEmit(remoteUpdatedAt)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying remote PAC schedule", e)
        }
    }

    fun stopRealtimeSync() {
        sectorsListener?.remove()
        pacScheduleListener?.remove()
        broadcastListener?.remove()
    }

    companion object {
        private const val TAG = "CloudSyncRepository"
    }
}
