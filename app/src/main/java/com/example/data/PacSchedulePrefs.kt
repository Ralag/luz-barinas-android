package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.data.model.PacScheduleData
import com.example.data.model.PacSlot
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists admin changes to PAC schedule in SharedPreferences.
 * Saves: activeMatrix, activeSlots, and sector block assignments.
 */
object PacSchedulePrefs {
    private const val TAG = "PacSchedulePrefs"
    private const val PREFS_NAME = "pac_schedule_prefs"
    private const val KEY_MATRIX = "pac_matrix"
    private const val KEY_SLOTS = "pac_slots"
    private const val KEY_SECTORS_A = "sectors_bloque_a"
    private const val KEY_SECTORS_B = "sectors_bloque_b"
    private const val KEY_SECTORS_C = "sectors_bloque_c"
    private const val KEY_SECTORS_D = "sectors_bloque_d"
    private const val KEY_VERSION = "schedule_version"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSchedule(context: Context) {
        val prefs = getPrefs(context)
        val snapshot = PacScheduleData.getScheduleSnapshot()
        prefs.edit {
            // Save matrix as JSON
            val matrixJson = JSONArray()
            for (row in snapshot.matrix) {
                matrixJson.put(JSONArray(row.toList()))
            }
            putString(KEY_MATRIX, matrixJson.toString())

            // Save slots as JSON
            val slotsJson = JSONArray()
            for (slot in snapshot.slots) {
                val slotObj = JSONObject()
                slotObj.put("timeLabel", slot.timeLabel)
                slotObj.put("startHour", slot.startHour)
                slotObj.put("endHour", slot.endHour)
                slotsJson.put(slotObj)
            }
            putString(KEY_SLOTS, slotsJson.toString())

            // Save sector assignments
            putStringSet(KEY_SECTORS_A, snapshot.assignments.getValue("sectorsA").toSet())
            putStringSet(KEY_SECTORS_B, snapshot.assignments.getValue("sectorsB").toSet())
            putStringSet(KEY_SECTORS_C, snapshot.assignments.getValue("sectorsC").toSet())
            putStringSet(KEY_SECTORS_D, snapshot.assignments.getValue("sectorsD").toSet())

            putLong(KEY_VERSION, snapshot.revision)
        }
    }

    fun loadSchedule(context: Context): Boolean {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_MATRIX)) return false

        return try {
            val matrix = JSONArray(prefs.getString(KEY_MATRIX, null) ?: return false)
            val slots = JSONArray(prefs.getString(KEY_SLOTS, null) ?: return false)
            val obj = JSONObject().put("matrix", matrix).put("slots", slots)
            listOf("A" to KEY_SECTORS_A, "B" to KEY_SECTORS_B, "C" to KEY_SECTORS_C, "D" to KEY_SECTORS_D).forEach { (block, key) ->
                prefs.getStringSet(key, null)?.let { obj.put("sectors$block", JSONArray(it.sorted())) }
            }
            val snapshot = com.example.data.model.PacScheduleSnapshot.parse(
                kotlinx.serialization.json.Json.parseToJsonElement(obj.toString()) as kotlinx.serialization.json.JsonObject)
            synchronized(PacScheduleData) {
                val version = prefs.getLong(KEY_VERSION, 0L)
                if (version >= PacScheduleData.scheduleVersion) PacScheduleData.applySnapshot(snapshot, version)
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load PAC schedule from SharedPreferences: ${e.message}")
            false
        }
    }
}
