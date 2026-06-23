package com.example.videobrowser.storage

/**
 * 初学者阅读提示：
 * 这个文件属于“收藏导入导出模块”。
 * 文件名 BookmarkImportExportController 可以拆开理解为“Bookmark Import Export Controller”，表示它专门负责收藏数据和系统文件 URI 之间的转换。
 * 主要职责：把收藏导出到用户选择的文件，或从用户选择的文件导入收藏，并在完成后给用户明确反馈。
 * 阅读顺序：先看 companion object 的文件名/MIME 类型，再看 exportToUri 和 importFromUri 两个入口。
 */
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.utils.ShortToast
import java.nio.charset.StandardCharsets

/**
 * 收藏导入导出控制器。
 *
 * MainActivity 负责启动 Android SAF 文件选择器，本类负责拿到 URI 后执行实际读写和仓库更新。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来访问 ContentResolver、读取字符串资源并展示 Toast。
 * @param savedPageRepository 参数类型为 `SavedPageRepository`，表示收藏/历史仓库，用来导出收藏文本和导入用户选择的收藏内容。
 * @param updateBookmarkButton 参数类型为 `() -> Unit`，表示刷新地址栏收藏按钮状态的回调，导入成功后用它同步当前页面 UI。
 */
class BookmarkImportExportController(
    private val activity: AppCompatActivity,
    private val savedPageRepository: SavedPageRepository,
    private val updateBookmarkButton: () -> Unit
) {
    /**
     * 函数 `exportToUri`：把当前收藏数据写入用户通过 SAF 选择的目标 URI。
     *
     * 初学者阅读提示：这里不负责弹出文件选择器，只负责处理选择器返回的 URI 和最终 Toast。
     *
     * @param uri 参数类型为 `Uri`，表示用户选择的导出目标文件地址，本类会通过 ContentResolver 打开输出流写入收藏文本。
     */
    fun exportToUri(uri: Uri) {
        val exported = runCatching {
            val payload = savedPageRepository.exportBookmarks().toByteArray(StandardCharsets.UTF_8)
            activity.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(payload)
            } ?: error("Unable to open bookmark export target")
        }.isSuccess

        ShortToast.show(
            activity,
            if (exported) R.string.toast_bookmarks_exported else R.string.toast_bookmarks_export_failed
        )
    }

    /**
     * 函数 `importFromUri`：从用户通过 SAF 选择的源 URI 读取收藏数据并导入仓库。
     *
     * 初学者阅读提示：读取失败会直接提示失败；读取成功后会根据实际导入数量展示“导入成功”或“没有新收藏”。
     *
     * @param uri 参数类型为 `Uri`，表示用户选择的导入源文件地址，本类会通过 ContentResolver 打开输入流读取 UTF-8 文本。
     */
    fun importFromUri(uri: Uri) {
        val result = runCatching {
            val payload = activity.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(StandardCharsets.UTF_8).readText()
            } ?: error("Unable to open bookmark import source")
            savedPageRepository.importBookmarks(payload)
        }.getOrElse {
            ShortToast.show(activity, R.string.toast_bookmarks_import_failed)
            return
        }

        val toastText = if (result.importedCount > 0) {
            activity.getString(R.string.toast_bookmarks_imported, result.importedCount)
        } else {
            activity.getString(R.string.toast_bookmarks_import_empty)
        }
        ShortToast.show(activity, toastText)
        updateBookmarkButton()
    }

    companion object {
        /**
         * 默认导出文件名。
         *
         * 初学者阅读提示：MainActivity 会把它传给 CreateDocument，让系统文件选择器使用这个名字作为初始文件名。
         */
        const val EXPORT_FILE_NAME = "videobrowser-bookmarks.txt"

        /**
         * 导入收藏时允许用户选择的文件类型。
         *
         * 初学者阅读提示：普通文本和 JSON 都允许选择，任意文件类型作为兜底，兼容不同文件管理器返回的 MIME 类型。
         */
        val IMPORT_MIME_TYPES = arrayOf("text/plain", "application/json", "*/*")
    }
}
