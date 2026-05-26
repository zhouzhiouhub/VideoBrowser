package com.example.videobrowser.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object UrlUtils {
    fun resolveAddressInput(
        input: String,
        searchUrlPrefix: String
    ): String? {
        val value = input.trim()
        if (value.isEmpty()) {
            return null
        }

        return when {
            isDirectLoadUrl(value) -> value
            looksLikeLocalAddress(value) || looksLikeIpAddress(value) -> "http://$value"
            looksLikeDomain(value) -> "https://$value"
            else -> {
                val encodedQuery = URLEncoder.encode(value, StandardCharsets.UTF_8.name())
                "$searchUrlPrefix$encodedQuery"
            }
        }
    }

    private fun isDirectLoadUrl(value: String): Boolean {
        return value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true) ||
            value.startsWith("about:", ignoreCase = true)
    }

    private fun looksLikeLocalAddress(value: String): Boolean {
        if (value.hasWhitespace()) {
            return false
        }

        val host = extractHost(value).lowercase()
        return host == "localhost" ||
            host == "10.0.2.2" ||
            host == "127.0.0.1"
    }

    private fun looksLikeIpAddress(value: String): Boolean {
        val parts = extractHost(value).split(".")
        return parts.size == 4 && parts.all { part ->
            part.toIntOrNull()?.let { it in 0..255 } == true
        }
    }

    private fun looksLikeDomain(value: String): Boolean {
        if (value.hasWhitespace()) {
            return false
        }

        val host = extractHost(value)
        return host.contains(".") &&
            host.any { it.isLetter() } &&
            host.split(".").all { it.isNotEmpty() }
    }

    private fun extractHost(value: String): String {
        return value
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore("#")
            .substringBefore(":")
    }

    private fun String.hasWhitespace(): Boolean {
        return any { it.isWhitespace() }
    }
}
