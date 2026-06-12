package com.example.videobrowser.download

object DownloadCancellationPolicy {
    fun canCancel(record: DownloadRecord): Boolean {
        return record.status == DownloadStatus.IN_PROGRESS
    }
}
