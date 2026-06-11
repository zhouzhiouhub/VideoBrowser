package com.example.videobrowser.download

data class DownloadRecord(
    val downloadId: Long,
    val title: String,
    val sourceUrl: String,
    val fileName: String,
    val mimeType: String?,
    val createdAtMillis: Long,
    val status: DownloadStatus = DownloadStatus.COMPLETED
)
