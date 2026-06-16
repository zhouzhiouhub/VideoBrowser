package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“网页离线归档模块”。
 * 文件名 PageArchiveController 可以拆开理解为“Page Archive Controller”，表示它专门负责把当前 WebView 保存成离线 MHTML 文件。
 * 主要职责：触发 WebView 保存归档、生成导出文件名、接收 SAF 返回的目标 URI，并在导出后清理临时文件。
 * 阅读顺序：先看 saveCurrentPageArchive，再看 handleExportResult，最后看私有的文件名和复制逻辑。
 */
import android.net.Uri
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import java.io.File

/**
 * 当前页面离线归档控制器。
 *
 * MainActivity 负责注册 Android SAF 导出 launcher，本类负责当前 WebView 的 MHTML 生成和临时文件生命周期。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来访问缓存目录、ContentResolver、字符串资源和 Toast。
 * @param currentActionableUrl 参数类型为 `() -> String?`，表示当前可执行页面动作的 URL；为空时说明当前页不能保存归档。
 * @param currentPageTitle 参数类型为 `() -> String`，表示当前页面标题，用来生成用户看到的导出文件名。
 * @param activeWebView 参数类型为 `() -> WebView`，表示当前正在显示的 WebView，保存归档时会调用它的 saveWebArchive。
 * @param launchArchiveExport 参数类型为 `(String) -> Unit`，表示启动系统导出文件选择器的回调，参数是建议导出文件名。
 */
class PageArchiveController(
    private val activity: AppCompatActivity,
    private val currentActionableUrl: () -> String?,
    private val currentPageTitle: () -> String,
    private val activeWebView: () -> WebView,
    private val launchArchiveExport: (String) -> Unit
) {
    private var pendingArchiveFile: File? = null

    /**
     * 函数 `saveCurrentPageArchive`：把当前 WebView 保存成临时 MHTML 文件，并启动系统导出流程。
     *
     * 初学者阅读提示：WebView 会先保存到 app cache 里的临时文件，等用户选择目标位置后再复制到用户文件。
     */
    fun saveCurrentPageArchive() {
        val pageUrl = currentActionableUrl()
        if (pageUrl == null) {
            Toast.makeText(activity, R.string.toast_page_archive_unavailable, Toast.LENGTH_SHORT).show()
            return
        }

        val archiveDirectory = File(activity.cacheDir, TEMP_DIR_NAME).apply {
            mkdirs()
        }
        val archiveFile = File(archiveDirectory, TEMP_FILE_NAME)
        clearPendingArchive()
        archiveFile.delete()

        activeWebView().saveWebArchive(
            archiveFile.absolutePath,
            false
        ) { savedPath ->
            val savedFile = savedPath
                ?.let(::File)
                ?.takeIf { file -> file.isFile }
            if (savedFile == null) {
                archiveFile.delete()
                Toast.makeText(activity, R.string.toast_page_archive_failed, Toast.LENGTH_SHORT).show()
                return@saveWebArchive
            }

            pendingArchiveFile = savedFile
            runCatching {
                launchArchiveExport(archiveFileName(pageUrl))
            }.onFailure {
                clearPendingArchive()
                Toast.makeText(activity, R.string.toast_page_archive_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 函数 `handleExportResult`：处理系统导出文件选择器返回的目标 URI。
     *
     * 初学者阅读提示：无论用户取消还是导出完成，临时归档文件都要清理，避免 cache 里留下旧页面内容。
     *
     * @param uri 参数类型为 `Uri?`，表示用户选择的导出目标；为空表示用户取消导出，此时只清理临时文件。
     */
    fun handleExportResult(uri: Uri?) {
        val archiveFile = pendingArchiveFile
        pendingArchiveFile = null
        if (uri != null && archiveFile != null) {
            exportArchiveFileToUri(archiveFile, uri)
        }
        archiveFile?.delete()
    }

    /**
     * 函数 `dispose`：释放页面归档流程持有的临时文件状态。
     *
     * 初学者阅读提示：Activity 销毁时调用，确保用户尚未完成导出的临时 MHTML 文件不会留在 cache 目录。
     */
    fun dispose() {
        clearPendingArchive()
    }

    /**
     * 函数 `archiveFileName`：为当前页面生成用户可见的 MHTML 导出文件名。
     *
     * 初学者阅读提示：优先使用页面标题和 URL，缺失时回退到应用名称，具体清洗规则由 PageArchiveFileName 统一处理。
     *
     * @param pageUrl 参数类型为 `String`，表示要保存归档的页面地址，用来参与文件名生成和兜底命名。
     * @return 返回适合传给 SAF CreateDocument 的文件名。
     */
    private fun archiveFileName(pageUrl: String): String {
        return PageArchiveFileName.create(
            pageTitle = currentPageTitle(),
            pageUrl = pageUrl,
            fallbackName = activity.getString(R.string.app_name)
        )
    }

    /**
     * 函数 `exportArchiveFileToUri`：把临时 MHTML 文件复制到用户选择的目标 URI。
     *
     * 初学者阅读提示：这里使用 ContentResolver 输出流，兼容系统文件选择器返回的各种文档提供方。
     *
     * @param archiveFile 参数类型为 `File`，表示 WebView 已生成的临时 MHTML 文件，本函数会读取它的内容。
     * @param uri 参数类型为 `Uri`，表示用户选择的导出目标，本函数会向它写入归档内容。
     */
    private fun exportArchiveFileToUri(archiveFile: File, uri: Uri) {
        runCatching {
            activity.contentResolver.openOutputStream(uri)?.use { output ->
                archiveFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: error("Unable to open page archive export target")
        }.onSuccess {
            Toast.makeText(activity, R.string.toast_page_archive_saved, Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(activity, R.string.toast_page_archive_failed, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 函数 `clearPendingArchive`：删除尚未导出的临时归档文件并清空内部状态。
     *
     * 初学者阅读提示：新的保存流程开始前或启动导出失败后调用，避免后续导出拿到旧文件。
     */
    private fun clearPendingArchive() {
        pendingArchiveFile?.delete()
        pendingArchiveFile = null
    }

    companion object {
        /**
         * MHTML 归档的 MIME 类型。
         *
         * 初学者阅读提示：MainActivity 会把它传给 CreateDocument，让系统知道这次创建的是网页归档文件。
         */
        const val MIME_TYPE = "multipart/related"

        private const val TEMP_DIR_NAME = "page-archives"
        private const val TEMP_FILE_NAME = "current-page.mhtml"
    }
}
