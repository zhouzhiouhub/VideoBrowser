package com.example.videobrowser.functioncenter

import android.app.DownloadManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.download.DownloadCancellationResult
import com.example.videobrowser.download.DownloadCanceller
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.download.DownloadRecordCleaner
import com.example.videobrowser.download.DownloadRecordRemoveResult
import com.example.videobrowser.download.DownloadRecordRemover
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.utils.ClipboardTextActions

class DownloadRecordPageOperations(
    private val activity: AppCompatActivity,
    private val downloadRecordRepository: DownloadRecordRepository
) {
    fun copyDownloadSourceUrl(record: DownloadRecord) {
        ClipboardTextActions.copyPlainText(
            activity = activity,
            labelResId = R.string.clipboard_download_source_url,
            text = record.sourceUrl,
            toastResId = R.string.toast_download_source_copied
        )
    }

    fun cancelDownload(record: DownloadRecord): DownloadCancellationResult {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return DownloadCanceller(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.cancel(record)
    }

    fun removeDownloadRecord(record: DownloadRecord): DownloadRecordRemoveResult {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return DownloadRecordRemover(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.remove(record)
    }

    fun clearRecordsAndFiles() {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        DownloadRecordCleaner(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.clearRecordsAndFiles()
    }
}
