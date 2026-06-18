package com.example.videobrowser.utils

import java.net.URI

object SafeUriParser {
    fun parse(value: String?): URI? {
        val normalized = value?.trim().orEmpty()
        if (normalized.isEmpty()) {
            return null
        }
        return runCatching { URI(normalized) }.getOrNull()
    }

    fun scheme(value: String?): String? {
        return parse(value)?.scheme
    }
}
