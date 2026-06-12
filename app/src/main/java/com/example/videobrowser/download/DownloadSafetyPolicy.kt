package com.example.videobrowser.download

import java.net.URI
import java.util.Locale

object DownloadSafetyPolicy {
    private const val DEFAULT_DOWNLOAD_FILE_NAME = "download.bin"
    private const val MAX_DOWNLOAD_FILE_NAME_LENGTH = 120
    private val invalidDownloadFileNameChars = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]")

    fun requiresConfirmation(fileName: String, mimeType: String?): Boolean {
        return DownloadCategory.from(mimeType, fileName) == DownloadCategory.APP
    }

    fun requiresInsecureTransportConfirmation(pageUrl: String?, downloadUrl: String): Boolean {
        return schemeOf(pageUrl) == "https" && schemeOf(downloadUrl) == "http"
    }

    fun isDownloadableNetworkUrl(url: String): Boolean {
        val uri = uriOf(url) ?: return false
        val scheme = uri.scheme?.lowercase(Locale.ROOT)
        return (scheme == "http" || scheme == "https") &&
            !uri.host.isNullOrBlank()
    }

    fun safeDownloadFileName(fileName: String): String {
        val sanitized = fileName
            .trim()
            .replace(invalidDownloadFileNameChars, "_")
            .replace(Regex("\\s+"), " ")
            .trim('.', ' ')
            .take(MAX_DOWNLOAD_FILE_NAME_LENGTH)
            .trim('.', ' ')
        return sanitized.ifBlank { DEFAULT_DOWNLOAD_FILE_NAME }
    }

    private fun schemeOf(url: String?): String? {
        return uriOf(url)
            ?.scheme
            ?.lowercase(Locale.ROOT)
    }

    private fun uriOf(url: String?): URI? {
        return runCatching { URI(url?.trim().orEmpty()) }.getOrNull()
    }
}
