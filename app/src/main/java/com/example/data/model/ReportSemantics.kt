package com.example.data.model

import java.util.Locale

/** Report kind takes precedence: an irregular supply may still have voltage/power. */
object ReportSemantics {
    fun status(reportType: String?, hasPower: Boolean): ServiceStatus = when (reportType?.trim()?.uppercase(Locale.ROOT)) {
        "FALLA", "FALLA_IRREGULAR", "IRREGULAR_OUTAGE", "IRREGULAR", "AVERIA", "AVERÍA", "BAJON", "BAJÓN" -> ServiceStatus.IRREGULAR_OUTAGE
        "SIN_LUZ", "SCHEDULED_OUTAGE" -> ServiceStatus.SCHEDULED_OUTAGE
        else -> if (hasPower) ServiceStatus.NORMAL else ServiceStatus.SCHEDULED_OUTAGE
    }

    fun canonicalType(reportType: String?, hasPower: Boolean): String = when (status(reportType, hasPower)) {
        ServiceStatus.NORMAL, ServiceStatus.UNKNOWN -> "NORMAL"
        ServiceStatus.SCHEDULED_OUTAGE -> "SIN_LUZ"
        ServiceStatus.IRREGULAR_OUTAGE -> if (reportType?.uppercase(Locale.ROOT) in setOf("BAJON", "BAJÓN")) "BAJON" else "FALLA_IRREGULAR"
    }

    fun remoteStatus(raw: String?): ServiceStatus = when (raw?.trim()?.uppercase(Locale.ROOT)) {
        "NORMAL" -> ServiceStatus.NORMAL
        "SCHEDULED_OUTAGE", "SIN_LUZ" -> ServiceStatus.SCHEDULED_OUTAGE
        "FALLA", "FALLA_IRREGULAR", "IRREGULAR_OUTAGE", "IRREGULAR", "AVERIA", "AVERÍA", "BAJON", "BAJÓN" -> ServiceStatus.IRREGULAR_OUTAGE
        else -> ServiceStatus.UNKNOWN
    }
}
