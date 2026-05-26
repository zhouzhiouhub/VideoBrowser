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

    fun isPlayableMediaUri(uri: Uri, mimeType: String? = null): Boolean {
        if (isPlayableMimeType(mimeType)) {
            return true
        }

        val scheme = uri.scheme?.lowercase().orEmpty()
        if (scheme == "rtsp" || scheme == "rtspt") {
            return true
        }
        if (scheme != "http" && scheme != "https" && scheme != "content" && scheme != "file") {
            return false
        }

        val path = uri.path.orEmpty().lowercase()
        if (playableExtensions.any { path.endsWith(it) }) {
            return true
        }

        val normalizedUrl = uri.toString()
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
}
