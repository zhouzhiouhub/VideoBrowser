package com.example.videobrowser.utils

import java.util.Locale

object MimeTypeNormalizer {
    fun withoutParameters(mimeType: String?): String? {
        return mimeType
            ?.substringBefore(';')
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    fun normalize(mimeType: String?): String? {
        return withoutParameters(mimeType)?.lowercase(Locale.ROOT)
    }

    fun isVideo(mimeType: String?): Boolean {
        return hasTopLevelType(mimeType, "video")
    }

    fun isImage(mimeType: String?): Boolean {
        return hasTopLevelType(mimeType, "image")
    }

    fun isAudio(mimeType: String?): Boolean {
        return hasTopLevelType(mimeType, "audio")
    }

    fun isText(mimeType: String?): Boolean {
        return hasTopLevelType(mimeType, "text")
    }

    private fun hasTopLevelType(mimeType: String?, topLevelType: String): Boolean {
        return normalize(mimeType)?.startsWith("$topLevelType/") == true
    }
}
