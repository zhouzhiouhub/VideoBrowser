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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        /**
         * 函数 `onReceive`：处理 `on Receive` 对应的事件或请求，集中完成校验、状态更新和回调通知。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param context 参数类型为 `Context?`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
         * @param intent 参数类型为 `Intent?`，表示函数执行 `intent` 相关逻辑时需要读取或处理的输入。
         */
        override fun onReceive(context: Context?, intent: Intent?) {
            handleDownloadComplete(intent)
        }
    }
    private val enqueueController = DownloadEnqueueController(
        activity = activity,
        browserManager = browserManager,
        downloadRecordRepository = downloadRecordRepository
    )
    private val statusSnapshotReader = AndroidDownloadStatusSnapshotReader(activity)
    private var receiverRegistered = false

    init {
        registerDownloadCompletionReceiver()
    }

    /**
     * 函数 `attachTo`：封装 `attach To` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param browserManager 参数类型为 `BrowserManager`，表示函数执行 `browserManager` 相关逻辑时需要读取或处理的输入。
     */
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

    /**
     * 函数 `attachTo`：封装 `attach To` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param browserManagers 参数类型为 `Iterable<BrowserManager>`，表示函数执行 `browserManagers` 相关逻辑时需要读取或处理的输入。
     */
    fun attachTo(browserManagers: Iterable<BrowserManager>) {
        browserManagers.forEach(::attachTo)
    }

    /**
     * 函数 `dispose`：封装 `dispose` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun dispose() {
        if (!receiverRegistered) {
            return
        }
        runCatching {
            activity.unregisterReceiver(downloadCompleteReceiver)
        }
        receiverRegistered = false
    }

    /**
     * 函数 `enqueue`：封装 `enqueue` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param userAgent 参数类型为 `String?`，表示函数执行 `userAgent` 相关逻辑时需要读取或处理的输入。
     * @param contentDisposition 参数类型为 `String?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param mimeType 参数类型为 `String?`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
     */
    fun enqueue(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?
    ) {
        enqueueController.enqueue(
            url = url,
            userAgent = userAgent,
            contentDisposition = contentDisposition,
            mimeType = mimeType
        )
    }

    /**
     * 函数 `retry`：封装 `retry` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     */
    fun retry(record: DownloadRecord) {
        enqueue(
            url = record.sourceUrl,
            userAgent = null,
            contentDisposition = null,
            mimeType = record.mimeType
        )
    }

    /**
     * 函数 `registerDownloadCompletionReceiver`：封装 `register Download Completion Receiver` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 函数 `handleDownloadComplete`：处理 `handle Download Complete` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param intent 参数类型为 `Intent?`，表示函数执行 `intent` 相关逻辑时需要读取或处理的输入。
     */
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

    /**
     * 函数 `queryDownloadStatus`：封装 `query Download Status` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param downloadId 参数类型为 `Long`，表示函数执行 `downloadId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun queryDownloadStatus(downloadId: Long): DownloadStatusSnapshot? {
        return statusSnapshotReader.query(
            downloadId = downloadId,
            queryFailureSnapshot = DownloadStatusSnapshot(status = DownloadStatus.FAILED)
        )
    }
}
