package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadController 可以拆开理解为“Download Controller”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
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

/**
 * 下载流程控制器。
 *
 * WebView 触发下载时先进入这里：可播放媒体会转给原生播放器，普通文件交给 Android DownloadManager。
 * 下载完成广播回来后，控制器再把状态同步到 DownloadRecordRepository。
 */
class DownloadController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val downloadRecordRepository: DownloadRecordRepository,
    private val openNativePlayer: (
        url: String,
        mimeType: String?,
        userAgentOverride: String?,
        titleOverride: String?
    ) -> Unit
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
        // 真正创建系统下载前先做 URL 和文件名安全检查，必要时弹出确认对话框。
        if (url.isNullOrBlank()) {
            Toast.makeText(activity, R.string.toast_download_failed, Toast.LENGTH_SHORT).show()
            return
        }
        if (!DownloadSafetyPolicy.isDownloadableNetworkUrl(url)) {
            Toast.makeText(activity, R.string.toast_download_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = DownloadSafetyPolicy.safeDownloadFileName(
            URLUtil.guessFileName(url, contentDisposition, mimeType)
        )
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
        // DownloadManager 负责后台下载和系统通知；应用自己只保存一份轻量记录用于功能中心展示。
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
            Toast.makeText(activity, R.string.toast_download_failed, Toast.LENGTH_SHORT).show()
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
        if (!downloadRecordRepository.contains(downloadId)) {
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
