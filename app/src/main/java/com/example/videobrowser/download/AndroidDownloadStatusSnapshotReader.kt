package com.example.videobrowser.download

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import com.example.videobrowser.utils.intOrNull
import com.example.videobrowser.utils.longOrNull

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
        val status = cursor.intOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            ?: return null
        val statusReason = cursor.intOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
        val bytesDownloaded = cursor
            .longOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            ?.takeIf { value -> value >= 0L }
        val totalBytes = cursor
            .longOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            ?.takeIf { value -> value >= 0L }

        return when (status) {
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
