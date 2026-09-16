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
        prefs.edit {
            // Save matrix as JSON
            val matrixJson = JSONArray()
            for (row in PacScheduleData.activeMatrix) {
                matrixJson.put(JSONArray(row.toList()))
            }
            putString(KEY_MATRIX, matrixJson.toString())

            // Save slots as JSON
            val slotsJson = JSONArray()
            for (slot in PacScheduleData.activeSlots) {
                val slotObj = JSONObject()
                slotObj.put("timeLabel", slot.timeLabel)
                slotObj.put("startHour", slot.startHour)
                slotObj.put("endHour", slot.endHour)
                slotsJson.put(slotObj)
            }
            putString(KEY_SLOTS, slotsJson.toString())

            // Save sector assignments
            putStringSet(KEY_SECTORS_A, PacScheduleData.SECTORS_BLOQUE_A.toSet())
            putStringSet(KEY_SECTORS_B, PacScheduleData.SECTORS_BLOQUE_B.toSet())
            putStringSet(KEY_SECTORS_C, PacScheduleData.SECTORS_BLOQUE_C.toSet())
            putStringSet(KEY_SECTORS_D, PacScheduleData.SECTORS_BLOQUE_D.toSet())

            putLong(KEY_VERSION, PacScheduleData.scheduleVersion)
        }
    }

    fun loadSchedule(context: Context): Boolean {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_MATRIX)) return false

        return try {
            // Load matrix
            val matrixStr = prefs.getString(KEY_MATRIX, null) ?: return false
            val matrixJson = JSONArray(matrixStr)
            val newMatrix = Array(matrixJson.length()) { i ->
                val rowJson = matrixJson.getJSONArray(i)
                Array(rowJson.length()) { j -> rowJson.getString(j) }
            }
            PacScheduleData.activeMatrix = newMatrix

            // Load slots
            val slotsStr = prefs.getString(KEY_SLOTS, null)
            if (slotsStr != null) {
                val slotsJson = JSONArray(slotsStr)
                val newSlots = mutableListOf<PacSlot>()
                for (i in 0 until slotsJson.length()) {
                    val obj = slotsJson.getJSONObject(i)
                    newSlots.add(PacSlot(
                        index = i,
                        timeLabel = obj.getString("timeLabel"),
                        startHour = obj.getInt("startHour"),
                        endHour = obj.getInt("endHour")
                    ))
                }
                PacScheduleData.activeSlots.clear()
                PacScheduleData.activeSlots.addAll(newSlots)
            }

            // Load sector assignments
            prefs.getStringSet(KEY_SECTORS_A, null)?.let {
                PacScheduleData.SECTORS_BLOQUE_A.clear()
                PacScheduleData.SECTORS_BLOQUE_A.addAll(it.sorted())
            }
            prefs.getStringSet(KEY_SECTORS_B, null)?.let {
                PacScheduleData.SECTORS_BLOQUE_B.clear()
                PacScheduleData.SECTORS_BLOQUE_B.addAll(it.sorted())
            }
            prefs.getStringSet(KEY_SECTORS_C, null)?.let {
                PacScheduleData.SECTORS_BLOQUE_C.clear()
                PacScheduleData.SECTORS_BLOQUE_C.addAll(it.sorted())
            }
            prefs.getStringSet(KEY_SECTORS_D, null)?.let {
                PacScheduleData.SECTORS_BLOQUE_D.clear()
                PacScheduleData.SECTORS_BLOQUE_D.addAll(it.sorted())
            }

            PacScheduleData.scheduleVersion = prefs.getLong(KEY_VERSION, 0L)
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load PAC schedule from SharedPreferences: ${e.message}")
            false
        }
    }
}
