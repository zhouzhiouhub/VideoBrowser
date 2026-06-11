package com.example.videobrowser.utils

import android.net.Uri

object MediaUrlUtils {
    private val playableExtensions = listOf(
        ".mp4",
        ".m4v",
        ".webm",
        ".mkv",
        ".mov",
        ".3gp",
        ".ts",
        ".mpeg",
        ".mpg",
        ".flv",
        ".m3u8",
        ".mpd"
    )

    private val playableMimeTypes = listOf(
        "application/dash+xml",
        "application/vnd.apple.mpegurl",
        "application/vnd.ms-sstr+xml",
        "application/x-mpegurl",
        "audio/mpegurl",
        "audio/x-mpegurl"
    )

    fun isPlayableMediaUri(url: String, mimeType: String? = null): Boolean {
        val rawUrl = url.trim()
        val scheme = rawUrl.substringBefore(':', missingDelimiterValue = "")
            .takeIf { it.matches(SCHEME_PATTERN) }
            ?.lowercase()
            .orEmpty()
        val path = runCatching { java.net.URI(rawUrl).path }
            .getOrNull()
            .orEmpty()
            .lowercase()
        return isPlayableMedia(
            scheme = scheme,
            path = path,
            rawUrl = rawUrl,
            mimeType = mimeType
        )
    }

    fun isPlayableMediaUri(uri: Uri, mimeType: String? = null): Boolean {
        return isPlayableMedia(
            scheme = uri.scheme?.lowercase().orEmpty(),
            path = uri.path.orEmpty().lowercase(),
            rawUrl = uri.toString(),
            mimeType = mimeType
        )
    }

    private fun isPlayableMedia(
        scheme: String,
        path: String,
        rawUrl: String,
        mimeType: String?
    ): Boolean {
        if (isPlayableMimeType(mimeType)) {
            return true
        }

        if (scheme == "rtsp" || scheme == "rtspt") {
            return true
        }
        if (scheme != "http" && scheme != "https" && scheme != "content" && scheme != "file") {
            return false
        }

        if (playableExtensions.any { path.endsWith(it) }) {
            return true
        }

        val normalizedUrl = rawUrl
            .substringBefore('#')
            .substringBefore('?')
            .lowercase()
        return playableExtensions.any { normalizedUrl.endsWith(it) } ||
            normalizedUrl.contains(".ism/manifest")
    }

    fun isPlayableMimeType(mimeType: String?): Boolean {
        val normalizedMimeType = mimeType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase()
            .orEmpty()
        return normalizedMimeType.startsWith("video/") ||
            normalizedMimeType in playableMimeTypes
    }

    private val SCHEME_PATTERN = Regex("^[A-Za-z][A-Za-z0-9+.-]*$")
}
