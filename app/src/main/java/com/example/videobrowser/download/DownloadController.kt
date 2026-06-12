package com.example.videobrowser.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.video.MediaRouteAction
import com.example.videobrowser.video.MediaRouteRequest
import com.example.videobrowser.video.MediaRouteSource
import com.example.videobrowser.video.MediaRoutingController

class DownloadController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val downloadRecordRepository: DownloadRecordRepository,
    private val openNativePlayer: (
        url: String,
        mimeType: String?,
        userAgentOverride: String?,
        titleOverride: String?
    ) -> Unit,
    private val openExternalUrl: (String) -> Unit
) {
    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            handleDownloadComplete(intent)
        }
    }
    private var receiverRegistered = false

    init {
        registerDownloadCompletionReceiver()
    }

    fun attachTo(browserManager: BrowserManager) {
        browserManager.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val mediaDecision = url
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    MediaRoutingController.route(
                        MediaRouteRequest(
                            source = MediaRouteSource.DOWNLOAD,
                            url = it,
                            mimeType = mimeType,
                            userAgent = userAgent
                        )
                    )
                }
            if (mediaDecision?.action == MediaRouteAction.OPEN_NATIVE_PLAYER) {
                val mediaItem = mediaDecision.mediaItem ?: return@setDownloadListener
                openNativePlayer(
                    mediaItem.uri,
                    mediaItem.mimeType,
                    mediaItem.userAgent,
                    mediaItem.title
                )
                return@setDownloadListener
            }

            enqueue(
                url = url,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType
            )
        }
    }

    fun attachTo(browserManagers: Iterable<BrowserManager>) {
        browserManagers.forEach(::attachTo)
    }

    fun dispose() {
        if (!receiverRegistered) {
            return
        }
        runCatching {
            activity.unregisterReceiver(downloadCompleteReceiver)
        }
        receiverRegistered = false
    }

    fun enqueue(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?
    ) {
        if (url.isNullOrBlank()) {
            Toast.makeText(activity, R.string.toast_download_failed, Toast.LENGTH_SHORT).show()
            return
        }
        if (!DownloadSafetyPolicy.isDownloadableNetworkUrl(url)) {
            Toast.makeText(activity, R.string.toast_download_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        confirmDownloadIfNeeded(
            url = url,
            fileName = fileName,
            mimeType = mimeType,
            confirmed = {
                enqueueConfirmed(
                    url = url,
                    userAgent = userAgent,
                    mimeType = mimeType,
                    fileName = fileName
                )
            }
        )
    }

    private fun confirmDownloadIfNeeded(
        url: String,
        fileName: String,
        mimeType: String?,
        confirmed: () -> Unit
    ) {
        if (DownloadSafetyPolicy.requiresInsecureTransportConfirmation(browserManager().currentUrl(), url)) {
            showInsecureDownloadConfirmation(
                fileName = fileName,
                confirmed = {
                    confirmAppPackageDownloadIfNeeded(
                        fileName = fileName,
                        mimeType = mimeType,
                        confirmed = confirmed
                    )
                }
            )
            return
        }

        confirmAppPackageDownloadIfNeeded(
            fileName = fileName,
            mimeType = mimeType,
            confirmed = confirmed
        )
    }

    private fun confirmAppPackageDownloadIfNeeded(
        fileName: String,
        mimeType: String?,
        confirmed: () -> Unit
    ) {
        if (DownloadSafetyPolicy.requiresConfirmation(fileName, mimeType)) {
            showRiskyDownloadConfirmation(
                fileName = fileName,
                confirmed = confirmed
            )
            return
        }

        confirmed()
    }

    private fun showRiskyDownloadConfirmation(fileName: String, confirmed: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_confirm_app_download)
            .setMessage(activity.getString(R.string.dialog_confirm_app_download_message, fileName))
            .setPositiveButton(R.string.action_download_anyway) { _, _ -> confirmed() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showInsecureDownloadConfirmation(fileName: String, confirmed: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_confirm_insecure_download)
            .setMessage(activity.getString(R.string.dialog_confirm_insecure_download_message, fileName))
            .setPositiveButton(R.string.action_download_anyway) { _, _ -> confirmed() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun enqueueConfirmed(
        url: String,
        userAgent: String?,
        mimeType: String?,
        fileName: String
    ) {
        runCatching {
            val resolvedUserAgent = userAgent?.takeIf { it.isNotBlank() }
                ?: browserManager().userAgentString()?.takeIf { it.isNotBlank() }
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription(activity.getString(R.string.toast_download_started))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                mimeType?.takeIf { it.isNotBlank() }?.let { setMimeType(it) }
                resolvedUserAgent?.let { addRequestHeader("User-Agent", it) }
                CookieManager.getInstance().getCookie(url)?.let { addRequestHeader("Cookie", it) }
            }
            val downloadManager =
                activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)
            downloadRecordRepository.add(
                DownloadRecord(
                    downloadId = downloadId,
                    title = fileName,
                    sourceUrl = url,
                    fileName = fileName,
                    mimeType = mimeType,
                    createdAtMillis = System.currentTimeMillis(),
                    status = DownloadStatus.IN_PROGRESS
                )
            )
        }.onSuccess {
            Toast.makeText(activity, R.string.toast_download_started, Toast.LENGTH_SHORT).show()
        }.onFailure {
            openExternalUrl(url)
        }
    }

    fun retry(record: DownloadRecord) {
        enqueue(
            url = record.sourceUrl,
            userAgent = null,
            contentDisposition = null,
            mimeType = record.mimeType
        )
    }

    private fun registerDownloadCompletionReceiver() {
        if (receiverRegistered) {
            return
        }
        ContextCompat.registerReceiver(
            activity,
            downloadCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
        receiverRegistered = true
    }

    private fun handleDownloadComplete(intent: Intent?) {
        if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            return
        }
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId < 0L) {
            return
        }
        val status = queryDownloadStatus(downloadId) ?: return
        downloadRecordRepository.updateSnapshot(
            downloadId = downloadId,
            status = status.status,
            statusReason = status.statusReason,
            bytesDownloaded = status.bytesDownloaded,
            totalBytes = status.totalBytes
        )
    }

    private fun queryDownloadStatus(downloadId: Long): DownloadCompletionStatus? {
        val downloadManager =
            activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = runCatching {
            downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        }.getOrNull() ?: return DownloadCompletionStatus(
            status = DownloadStatus.FAILED,
            statusReason = null,
            bytesDownloaded = null,
            totalBytes = null
        )

        cursor.use {
            if (!it.moveToFirst()) {
                return null
            }
            val statusColumn = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusColumn < 0) {
                return null
            }
            val reasonColumn = it.getColumnIndex(DownloadManager.COLUMN_REASON)
            val statusReason = reasonColumn
                .takeIf { column -> column >= 0 }
                ?.let { column -> it.getInt(column) }
            val downloadedColumn = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalColumn = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val bytesDownloaded = downloadedColumn
                .takeIf { column -> column >= 0 }
                ?.let { column -> it.getLong(column) }
                ?.takeIf { value -> value >= 0L }
            val totalBytes = totalColumn
                .takeIf { column -> column >= 0 }
                ?.let { column -> it.getLong(column) }
                ?.takeIf { value -> value >= 0L }
            return when (it.getInt(statusColumn)) {
                DownloadManager.STATUS_SUCCESSFUL -> DownloadCompletionStatus(
                    status = DownloadStatus.COMPLETED,
                    statusReason = null,
                    bytesDownloaded = bytesDownloaded,
                    totalBytes = totalBytes
                )
                DownloadManager.STATUS_FAILED -> DownloadCompletionStatus(
                    status = DownloadStatus.FAILED,
                    statusReason = statusReason,
                    bytesDownloaded = bytesDownloaded,
                    totalBytes = totalBytes
                )
                else -> null
            }
        }
    }

    private data class DownloadCompletionStatus(
        val status: DownloadStatus,
        val statusReason: Int?,
        val bytesDownloaded: Long?,
        val totalBytes: Long?
    )
}
