package com.example.videobrowser.download

data class DownloadRecord(
    val downloadId: Long,
    val title: String,
    val sourceUrl: String,
    val fileName: String,
    val mimeType: String?,
    val createdAtMillis: Long,
    val status: DownloadStatus = DownloadStatus.COMPLETED,
    val statusReason: Int? = null,
    val bytesDownloaded: Long? = null,
    val totalBytes: Long? = null
) {
    val progress: DownloadProgress
        get() = DownloadProgress(bytesDownloaded = bytesDownloaded, totalBytes = totalBytes)
}
