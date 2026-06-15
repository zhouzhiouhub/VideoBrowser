package com.example.videobrowser.browser

import java.net.URI

object PageArchiveFileName {
    private const val MAX_BASE_NAME_LENGTH = 80
    private const val EXTENSION = ".mhtml"
    private val invalidFileNameCharacters = Regex("""[\u0000-\u001F\\/:*?"<>|]+""")
    private val whitespaceSequence = Regex("\\s+")

    fun create(pageTitle: String, pageUrl: String?, fallbackName: String): String {
        val baseName = listOf(
            sanitize(pageTitle),
            sanitize(hostFromUrl(pageUrl)),
            sanitize(fallbackName)
        ).firstOrNull { value -> value.isNotBlank() } ?: "page"

        return baseName
            .take(MAX_BASE_NAME_LENGTH)
            .trimEnd('.', ' ')
            .ifBlank { "page" } + EXTENSION
    }

    private fun hostFromUrl(pageUrl: String?): String {
        return runCatching {
            URI(pageUrl.orEmpty()).host.orEmpty()
        }.getOrDefault("")
    }

    private fun sanitize(value: String): String {
        return value
            .replace(whitespaceSequence, " ")
            .replace(invalidFileNameCharacters, "_")
            .trim(' ', '.', '_')
    }
}
