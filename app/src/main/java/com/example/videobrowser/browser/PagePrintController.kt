package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“网页打印模块”。
 * 文件名 PagePrintController 可以拆开理解为“Page Print Controller”，表示它专门负责把当前 WebView 交给 Android 打印框架。
 * 主要职责：判断当前页面是否可打印、生成打印任务名称、创建 WebView 打印适配器并启动系统打印流程。
 * 阅读顺序：先看 printCurrentPage，再看 printJobName 了解打印任务标题如何生成。
 */
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.utils.TextWhitespaceNormalizer

/**
 * 当前页面打印控制器。
 *
 * MainActivity 只提供当前页面状态和 WebView，本类负责与 Android Print Framework 交互。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来读取系统 PrintManager、字符串资源和展示失败 Toast。
 * @param currentActionableUrl 参数类型为 `() -> String?`，表示当前可执行页面动作的 URL；为空时说明当前页不能打印。
 * @param currentPageTitle 参数类型为 `() -> String`，表示当前页面标题，用来生成用户在系统打印界面看到的任务名称。
 * @param activeWebView 参数类型为 `() -> WebView`，表示当前正在显示的 WebView，本类会从它创建打印文档适配器。
 */
class PagePrintController(
    private val activity: AppCompatActivity,
    private val currentActionableUrl: () -> String?,
    private val currentPageTitle: () -> String,
    private val activeWebView: () -> WebView
) {
    /**
     * 函数 `printCurrentPage`：把当前 WebView 交给 Android 打印框架。
     *
     * 初学者阅读提示：如果当前页面没有可打印 URL，会直接提示不可用；否则创建打印适配器并启动系统打印界面。
     */
    fun printCurrentPage() {
        val pageUrl = currentActionableUrl()
        if (pageUrl == null) {
            Toast.makeText(activity, R.string.toast_print_page_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        val jobName = printJobName(pageUrl)
        runCatching {
            val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = activeWebView().createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }.onFailure {
            Toast.makeText(activity, R.string.toast_print_page_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 函数 `printJobName`：生成系统打印界面显示的任务名称。
     *
     * 初学者阅读提示：优先使用页面标题；标题为空时使用 URL host；还为空时使用应用名称，并限制长度避免系统 UI 里显示过长。
     *
     * @param pageUrl 参数类型为 `String`，表示当前要打印的页面地址，用来在标题为空时提取 host 作为任务名。
     * @return 返回本地化后的打印任务名称，例如“打印 - 示例页面”。
     */
    private fun printJobName(pageUrl: String): String {
        val title = TextWhitespaceNormalizer
            .collapse(currentPageTitle())
            .ifBlank { SiteHost.fromUrl(pageUrl).orEmpty() }
            .ifBlank { activity.getString(R.string.app_name) }
            .take(MAX_JOB_TITLE_LENGTH)
        return activity.getString(R.string.print_job_name, title)
    }

    companion object {
        private const val MAX_JOB_TITLE_LENGTH = 80
    }
}
