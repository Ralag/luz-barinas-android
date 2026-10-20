package com.example.data.model

import java.net.URI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object TutorialLink {
    private val current = MutableStateFlow<String?>(null)
    val url = current.asStateFlow()
    fun update(value: String?) { current.value = validate(value) }

    fun validate(value: String?): String? {
        if (value == null || value.length > 2000) return null
        return try {
            val uri = URI(value)
            if (uri.scheme != "https" || uri.userInfo != null || uri.port !in listOf(-1, 443)) return null
            val host = uri.host?.lowercase() ?: return null
            val valid = when (host) {
                "youtu.be" -> uri.path.matches(Regex("/[A-Za-z0-9_-]{11}"))
                "youtube.com", "www.youtube.com", "m.youtube.com" ->
                    (uri.path == "/watch" && uri.rawQuery.orEmpty().split('&').any { it.matches(Regex("v=[A-Za-z0-9_-]{11}")) }) ||
                        uri.path.matches(Regex("/(shorts|embed)/[A-Za-z0-9_-]{11}"))
                else -> false
            }
            if (valid) value else null
        } catch (_: Exception) { null }
    }
}
