package com.example.data.model

import java.util.Locale

/** The stored block is a code, never the first matching letter of its display label. */
object PacBlock {
    private val validCodes = setOf("A", "B", "C", "D")
    fun normalize(value: String?): String? {
        val code = value?.trim()?.uppercase(Locale.ROOT)
            ?.removePrefix("BLOQUE")?.trim()
        return code?.takeIf { it in validCodes }
    }
}
