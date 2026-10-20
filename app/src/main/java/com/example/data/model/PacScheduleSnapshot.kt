package com.example.data.model

import kotlinx.serialization.json.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale

/** A complete, validated schedule. Nothing mutates the live schedule while parsing. */
data class PacScheduleSnapshot(
    val slots: List<PacSlot>,
    val matrix: List<List<String>>,
    val assignments: Map<String, List<String>> = emptyMap(),
    val revision: Long = 0L
) {
    fun fingerprint(): String {
        val canonical = buildJsonObject {
            put("slots", buildJsonArray { slots.forEach { slot -> add(buildJsonArray {
                add(slot.timeLabel); add(slot.startHour); add(slot.endHour)
            }) } })
            put("matrix", buildJsonArray { matrix.forEach { row -> add(buildJsonArray { row.forEach { add(it) } }) } })
            assignments.toSortedMap().forEach { (key, names) -> put(key, buildJsonArray { names.forEach { add(it) } }) }
        }.toString()
        return MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        fun parse(obj: JsonObject): PacScheduleSnapshot {
            val rawSlots = obj["slots"]?.jsonArray ?: error("Missing slots")
            require(rawSlots.size in 1..24) { "Invalid slot count" }
            val slots = rawSlots.mapIndexed { index, value ->
                val slot = value.jsonObject
                val start = slot["startHour"]?.jsonPrimitive?.intOrNull ?: error("Missing start hour")
                val end = slot["endHour"]?.jsonPrimitive?.intOrNull ?: error("Missing end hour")
                require(start in 0..23 && end in 0..24 && start != end && !(start == 0 && end == 24))
                val label = slot["timeLabel"]?.jsonPrimitive?.content?.trim().orEmpty()
                require(label.isNotEmpty() && label.length <= 100)
                PacSlot(index, label, start, end)
            }
            val rows = obj["matrixRows"]?.jsonArray?.map { it.jsonObject["cells"]!!.jsonArray }
                ?: obj["matrix"]?.jsonArray?.map { it.jsonArray } ?: error("Missing matrix")
            require(rows.size == slots.size)
            val matrix = rows.map { row ->
                require(row.size == 7)
                row.map { cell ->
                    val raw = cell.jsonPrimitive.content.trim().uppercase(Locale.ROOT)
                    if (raw in listOf("-", "", "LIBRE", "SIN CORTE", "SIN_CORTE")) "-"
                    else PacBlock.normalize(raw) ?: error("Invalid block")
                }
            }
            val assignments = listOf("sectorsA", "sectorsB", "sectorsC", "sectorsD")
                .filter { obj.containsKey(it) }.associateWith { key ->
                    obj.getValue(key).jsonArray.map { it.jsonPrimitive.content.trim() }.also { names ->
                        require(names.size <= 5000 && names.all { it.isNotEmpty() && it.length <= 300 })
                    }.distinct()
                }
            val revision = maxOf(parseRevision(obj["updatedAt"]), parseRevision(obj["version"]))
            return PacScheduleSnapshot(slots, matrix, assignments, revision)
        }

        private fun parseRevision(value: JsonElement?): Long {
            val text = (value as? JsonPrimitive)?.content ?: return 0L
            text.toLongOrNull()?.let { return it.coerceAtLeast(0L) }
            for (pattern in listOf("yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX")) {
                try {
                    return SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(text)?.time ?: 0L
                } catch (_: java.text.ParseException) { /* Try the next supported ISO form. */ }
            }
            return 0L
        }
    }
}
