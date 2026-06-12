package com.example.videobrowser.download

data class DownloadProgress(
    val bytesDownloaded: Long?,
    val totalBytes: Long?
) {
    val hasDownloadedBytes: Boolean
        get() = bytesDownloaded != null && bytesDownloaded >= 0L

    val hasKnownTotal: Boolean
        get() = totalBytes != null && totalBytes > 0L

    fun percent(): Int? {
        val downloaded = bytesDownloaded?.takeIf { it >= 0L } ?: return null
        val total = totalBytes?.takeIf { it > 0L } ?: return null
        return ((downloaded.coerceAtMost(total) * 100L) / total).toInt().coerceIn(0, 100)
    }
}
