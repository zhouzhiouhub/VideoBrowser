package com.example.videobrowser.functioncenter

import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.download.AndroidSystemDownloadRemover
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
    private val systemDownloadRemover = AndroidSystemDownloadRemover(activity)
    private val downloadCanceller = DownloadCanceller(downloadRecordRepository, systemDownloadRemover)
    private val downloadRecordRemover = DownloadRecordRemover(downloadRecordRepository, systemDownloadRemover)
    private val downloadRecordCleaner = DownloadRecordCleaner(downloadRecordRepository, systemDownloadRemover)

    fun copyDownloadSourceUrl(record: DownloadRecord) {
        ClipboardTextActions.copyPlainText(
            activity = activity,
            labelResId = R.string.clipboard_download_source_url,
            text = record.sourceUrl,
            toastResId = R.string.toast_download_source_copied
        )
    }

    fun cancelDownload(record: DownloadRecord): DownloadCancellationResult {
        return downloadCanceller.cancel(record)
    }

    fun removeDownloadRecord(record: DownloadRecord): DownloadRecordRemoveResult {
        return downloadRecordRemover.remove(record)
    }

    fun clearRecordsAndFiles() {
        downloadRecordCleaner.clearRecordsAndFiles()
    }
}
