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

        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
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
        downloadRecordRepository.updateStatus(downloadId, status)
    }

    private fun queryDownloadStatus(downloadId: Long): DownloadStatus? {
        val downloadManager =
            activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = runCatching {
            downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        }.getOrNull() ?: return DownloadStatus.FAILED

        cursor.use {
            if (!it.moveToFirst()) {
                return null
            }
            val statusColumn = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusColumn < 0) {
                return null
            }
            return when (it.getInt(statusColumn)) {
                DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.COMPLETED
                DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                else -> null
            }
        }
    }
}
