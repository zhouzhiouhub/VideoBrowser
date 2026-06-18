package com.example.videobrowser.functioncenter

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.download.DownloadCancellationResult
import com.example.videobrowser.download.DownloadCanceller
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.download.DownloadRecordCleaner
import com.example.videobrowser.download.DownloadRecordRemoveResult
import com.example.videobrowser.download.DownloadRecordRemover
import com.example.videobrowser.download.DownloadRecordRepository

class DownloadRecordPageOperations(
    private val activity: AppCompatActivity,
    private val downloadRecordRepository: DownloadRecordRepository
) {
    fun copyDownloadSourceUrl(record: DownloadRecord) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                activity.getString(R.string.clipboard_download_source_url),
                record.sourceUrl
            )
        )
        Toast.makeText(activity, R.string.toast_download_source_copied, Toast.LENGTH_SHORT).show()
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
