package com.example.videobrowser.functioncenter

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.utils.ChooserIntentLauncher
import com.example.videobrowser.utils.FileOpenIntentFactory
import com.example.videobrowser.utils.FileShareIntentFactory

internal class DownloadedFileLauncher(
    private val activity: AppCompatActivity
) {
    fun shareDownloadedFile(record: DownloadRecord) {
        val uri = downloadedFileUri(record) ?: return
        val intent = FileShareIntentFactory.create(
            contentResolver = activity.contentResolver,
            uri = uri,
            displayName = record.fileName,
            mimeType = record.mimeType
        )
        startDownloadedFileIntent(intent, R.string.action_share_file)
    }

    fun openDownloadedFile(record: DownloadRecord) {
        val uri = downloadedFileUri(record) ?: return
        val intent = FileOpenIntentFactory.create(uri, record.mimeType)
        startDownloadedFileIntent(intent, R.string.action_open_file)
    }

    private fun startDownloadedFileIntent(intent: Intent, chooserTitleRes: Int) {
        ChooserIntentLauncher.start(
            activity = activity,
            intent = intent,
            chooserTitleRes = chooserTitleRes,
            securityExceptionToastRes = R.string.toast_download_file_unavailable
        )
    }

    private fun downloadedFileUri(record: DownloadRecord) =
        runCatching {
            val downloadManager =
                activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.getUriForDownloadedFile(record.downloadId)
        }.getOrNull().also { uri ->
            if (uri == null) {
                Toast.makeText(
                    activity,
                    R.string.toast_download_file_unavailable,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}
