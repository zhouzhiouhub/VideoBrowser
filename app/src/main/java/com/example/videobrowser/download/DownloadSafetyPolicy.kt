package com.example.videobrowser.download

object DownloadSafetyPolicy {
    fun requiresConfirmation(fileName: String, mimeType: String?): Boolean {
        return DownloadCategory.from(mimeType, fileName) == DownloadCategory.APP
    }
}
