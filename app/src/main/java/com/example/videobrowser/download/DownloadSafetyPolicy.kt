package com.example.videobrowser.download

import java.net.URI
import java.util.Locale

object DownloadSafetyPolicy {
    fun requiresConfirmation(fileName: String, mimeType: String?): Boolean {
        return DownloadCategory.from(mimeType, fileName) == DownloadCategory.APP
    }

    fun requiresInsecureTransportConfirmation(pageUrl: String?, downloadUrl: String): Boolean {
        return schemeOf(pageUrl) == "https" && schemeOf(downloadUrl) == "http"
    }

    private fun schemeOf(url: String?): String? {
        return runCatching {
            URI(url?.trim().orEmpty()).scheme
                ?.lowercase(Locale.ROOT)
        }.getOrNull()
    }
}
