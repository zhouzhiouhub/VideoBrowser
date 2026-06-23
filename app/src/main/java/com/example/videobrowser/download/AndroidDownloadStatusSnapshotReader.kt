package com.example.videobrowser.download

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor

class AndroidDownloadStatusSnapshotReader(
    private val context: Context
) {
    fun query(
        downloadId: Long,
        queryFailureSnapshot: DownloadStatusSnapshot? = null
    ): DownloadStatusSnapshot? {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = runCatching {
            downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        }.getOrElse {
            return queryFailureSnapshot
        }
        return cursor.use(::snapshotFromCursor)
    }

    private fun snapshotFromCursor(cursor: Cursor): DownloadStatusSnapshot? {
        if (!cursor.moveToFirst()) {
            return null
        }
        val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        if (statusColumn < 0) {
            return null
        }
        val reasonColumn = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val downloadedColumn = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
        val totalColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
        val statusReason = reasonColumn
            .takeIf { column -> column >= 0 }
            ?.let { column -> cursor.getInt(column) }
        val bytesDownloaded = downloadedColumn
            .takeIf { column -> column >= 0 }
            ?.let { column -> cursor.getLong(column) }
            ?.takeIf { value -> value >= 0L }
        val totalBytes = totalColumn
            .takeIf { column -> column >= 0 }
            ?.let { column -> cursor.getLong(column) }
            ?.takeIf { value -> value >= 0L }

        return when (cursor.getInt(statusColumn)) {
            DownloadManager.STATUS_SUCCESSFUL -> DownloadStatusSnapshot(
                status = DownloadStatus.COMPLETED,
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes
            )
            DownloadManager.STATUS_FAILED -> DownloadStatusSnapshot(
                status = DownloadStatus.FAILED,
                statusReason = statusReason,
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes
            )
            DownloadManager.STATUS_PENDING,
            DownloadManager.STATUS_PAUSED,
            DownloadManager.STATUS_RUNNING -> DownloadStatusSnapshot(
                status = DownloadStatus.IN_PROGRESS,
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes
            )
            else -> null
        }
    }
}
