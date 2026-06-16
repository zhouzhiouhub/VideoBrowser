package com.example.videobrowser.localfiles

/**
 * 初学者阅读提示：
 * 这个文件属于“本地文件模块”。
 * 文件名 LocalFileLaunchers 可以拆开理解为“Local File Launchers”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：管理目录授权、读取本地文档列表，并把本地媒体交给浏览器或播放器打开。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.video.ExternalSubtitleCandidate
import com.example.videobrowser.video.PlaybackQueue

class LocalFileLaunchers(
    private val activity: AppCompatActivity,
    private val directoryPermissions: LocalDirectoryPermissionManager,
    private val documentRepository: LocalDocumentRepository,
    private val logTag: String,
    private val onOpenDocumentUri: (
        Uri,
        String?,
        String?,
        List<ExternalSubtitleCandidate>,
        PlaybackQueue?
    ) -> Unit,
    private val onDirectoryReady: (Uri) -> Unit,
    private val onDirectoryUnavailable: () -> Unit
) {
    private lateinit var openLocalFileLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var openLocalDirectoryLauncher: ActivityResultLauncher<Uri?>

    /**
     * 函数 `setup`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun setup() {
        openLocalFileLauncher =
            activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    onOpenDocumentUri(uri, null, null, emptyList(), null)
                }
            }

        openLocalDirectoryLauncher =
            activity.registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                if (uri != null) {
                    handleSelectedDirectory(uri)
                }
            }
    }

    /**
     * 函数 `openFile`：启动或加载 `open File` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun openFile() {
        openLocalFileLauncher.launch(arrayOf("*/*"))
    }

    /**
     * 函数 `openDirectory`：启动或加载 `open Directory` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param initialUri 参数类型为 `Uri?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    fun openDirectory(initialUri: Uri?) {
        openLocalDirectoryLauncher.launch(initialUri)
    }

    /**
     * 函数 `handleSelectedDirectory`：处理 `handle Selected Directory` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param uri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    private fun handleSelectedDirectory(uri: Uri) {
        val previousUri = directoryPermissions.savedDirectoryUri()
        if (!directoryPermissions.persistReadWritePermission(uri)) {
            onDirectoryUnavailable()
            return
        }

        val rootDocumentId = documentRepository.rootDocumentId(uri)
        if (rootDocumentId == null) {
            directoryPermissions.releaseReadWritePermission(uri)
            onDirectoryUnavailable()
            return
        }

        val isDirectoryUsable = runCatching {
            documentRepository.queryDocuments(uri, rootDocumentId)
        }.onFailure {
            Log.w(logTag, "Unable to access selected local directory $uri", it)
            directoryPermissions.releaseReadWritePermission(uri)
            onDirectoryUnavailable()
        }.isSuccess
        if (!isDirectoryUsable) {
            return
        }

        if (previousUri != null && previousUri != uri) {
            directoryPermissions.releaseReadWritePermission(previousUri)
        }
        directoryPermissions.saveDirectoryUri(uri)
        onDirectoryReady(uri)
    }
}
