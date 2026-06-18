package com.example.videobrowser.utils

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Utf8UrlCodec {
    fun encodeFormComponent(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
    }

    fun decodeFormComponent(value: String): String? {
        return runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        }.getOrNull()
    }

    fun decodeFormComponentOr(value: String, fallback: String): String {
        return decodeFormComponent(value) ?: fallback
    }
}
