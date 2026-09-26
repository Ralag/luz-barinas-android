package com.example.data.model

import android.content.Context

data class PacAlertSettings(
    val advanceMinutes: Int = 10,
    val isNotificationEnabled: Boolean = true,
    val isAlarmEnabled: Boolean = false,
    val isRestoreAlarmEnabled: Boolean = false,
    val isVibrationEnabled: Boolean = true,
    val useLegacyAlarmSiren: Boolean = false
)

object PacAlertPrefs {
    private const val PREFS_NAME = "pac_alert_settings"
    private const val KEY_ADVANCE_MINUTES = "advance_minutes"
    private const val KEY_NOTIF_ENABLED = "notification_enabled"
    private const val KEY_ALARM_ENABLED = "alarm_enabled"
    private const val KEY_RESTORE_ALARM_ENABLED = "restore_alarm_enabled"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    private const val KEY_USE_LEGACY_ALARM = "use_legacy_alarm"

    fun getSettings(context: Context): PacAlertSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val legacy = prefs.getBoolean(KEY_USE_LEGACY_ALARM, false)
        return PacAlertSettings(
            advanceMinutes = prefs.getInt(KEY_ADVANCE_MINUTES, 10),
            isNotificationEnabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true),
            isAlarmEnabled = prefs.getBoolean(KEY_ALARM_ENABLED, legacy),
            isRestoreAlarmEnabled = prefs.getBoolean(KEY_RESTORE_ALARM_ENABLED, false),
            isVibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true),
            useLegacyAlarmSiren = legacy
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
            .putBoolean(KEY_USE_LEGACY_ALARM, settings.useLegacyAlarmSiren || settings.isAlarmEnabled)
            .apply()
    }
}
