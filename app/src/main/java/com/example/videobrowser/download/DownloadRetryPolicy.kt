package com.example.videobrowser.download

object DownloadRetryPolicy {
    fun canRetry(record: DownloadRecord): Boolean {
        return record.status == DownloadStatus.FAILED || record.status == DownloadStatus.CANCELED
    }
}
