package com.example.videobrowser.functioncenter

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.download.DownloadRecord

internal class DownloadedFileLauncher(
    private val activity: AppCompatActivity
) {
    fun shareDownloadedFile(record: DownloadRecord) {
        val uri = downloadedFileUri(record) ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = record.mimeType ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(activity.contentResolver, record.fileName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            activity.startActivity(
                Intent.createChooser(intent, activity.getString(R.string.action_share_file))
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        } catch (_: SecurityException) {
            Toast.makeText(activity, R.string.toast_download_file_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    fun openDownloadedFile(record: DownloadRecord) {
        val uri = downloadedFileUri(record) ?: return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, record.mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            activity.startActivity(
                Intent.createChooser(intent, activity.getString(R.string.action_open_file))
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        } catch (_: SecurityException) {
            Toast.makeText(activity, R.string.toast_download_file_unavailable, Toast.LENGTH_SHORT).show()
        }
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
