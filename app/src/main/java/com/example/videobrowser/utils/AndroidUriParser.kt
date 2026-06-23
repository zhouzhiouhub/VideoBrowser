package com.example.videobrowser.utils

import android.net.Uri

object AndroidUriParser {
    fun parse(value: String): Uri {
        return Uri.parse(value)
    }

    fun parseTrimmedOrNull(value: String?): Uri? {
        val normalized = value?.trim().orEmpty()
        if (normalized.isEmpty()) {
            return null
        }
        return parse(normalized)
    }
}
