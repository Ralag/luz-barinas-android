package com.example.data.model

import android.content.Context

data class PacAlertSettings(
    val advanceMinutes: Int = 10,
    val isNotificationEnabled: Boolean = true,
    val isAlarmEnabled: Boolean = false,
    val isRestoreAlarmEnabled: Boolean = false,
    val isVibrationEnabled: Boolean = true
)

object PacAlertPrefs {
    private const val PREFS_NAME = "pac_alert_settings"
    private const val KEY_ADVANCE_MINUTES = "advance_minutes"
    private const val KEY_NOTIF_ENABLED = "notification_enabled"
    private const val KEY_ALARM_ENABLED = "alarm_enabled"
    private const val KEY_RESTORE_ALARM_ENABLED = "restore_alarm_enabled"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"

    fun getSettings(context: Context): PacAlertSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return PacAlertSettings(
            advanceMinutes = prefs.getInt(KEY_ADVANCE_MINUTES, 10),
            isNotificationEnabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true),
            isAlarmEnabled = prefs.getBoolean(KEY_ALARM_ENABLED, false),
            isRestoreAlarmEnabled = prefs.getBoolean(KEY_RESTORE_ALARM_ENABLED, false),
            isVibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        )
    }

    fun saveSettings(context: Context, settings: PacAlertSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_ADVANCE_MINUTES, settings.advanceMinutes.coerceIn(1, 180))
            .putBoolean(KEY_NOTIF_ENABLED, settings.isNotificationEnabled)
            .putBoolean(KEY_ALARM_ENABLED, settings.isAlarmEnabled)
            .putBoolean(KEY_RESTORE_ALARM_ENABLED, settings.isRestoreAlarmEnabled)
            .putBoolean(KEY_VIBRATION_ENABLED, settings.isVibrationEnabled)
            .apply()
    }
}
