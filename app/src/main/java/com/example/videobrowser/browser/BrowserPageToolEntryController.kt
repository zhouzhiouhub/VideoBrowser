package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserPageToolEntryController 可以拆开理解为“Browser Page Tool Entry Controller”，
 * 表示它只负责把页面工具入口转发给具体功能控制器。
 * 主要职责：打开页内查找、保存当前页面归档、打印当前页面，以及从播放历史继续打开内容。
 * 阅读顺序：先看构造参数了解它依赖哪些页面工具，再看公开函数对应功能中心里的哪些动作。
 */
import com.example.videobrowser.functioncenter.PlaybackHistoryDisplayText
import com.example.videobrowser.video.PlaybackHistorySource
import com.example.videobrowser.video.PlaybackProgress

/**
 * 页面工具入口控制器。
 *
 * FunctionCenterPages 只需要“用户点击了某个页面工具”的回调；这个控制器把这些回调转给
 * FindInPageDialogController、PageArchiveController、PagePrintController 或播放入口。
 *
 * @param findInPageDialogController 页内查找弹窗控制器，用于打开当前 WebView 的查找界面。
 * @param pageArchiveController 页面归档控制器，用于把当前页面保存成 Web Archive。
 * @param pagePrintController 页面打印控制器，用于调用 Android 打印框架打印当前 WebView。
 * @param loadUrl 在当前浏览器中打开 URL 的函数，用于继续网页播放历史。
 * @param openNativePlayer 打开原生播放器的函数，参数分别是媒体 URL 和可选标题。
 */
class BrowserPageToolEntryController(
    private val findInPageDialogController: FindInPageDialogController,
    private val pageArchiveController: PageArchiveController,
    private val pagePrintController: PagePrintController,
    private val loadUrl: (String) -> Unit,
    private val openNativePlayer: (String, String?) -> Unit
) {
    /**
     * 打开页内查找弹窗。
     *
     * @return 无返回值；具体弹窗展示由 FindInPageDialogController 负责。
     */
    fun showFindInPageDialog() {
        findInPageDialogController.showDialog()
    }

    /**
     * 保存当前页面归档。
     *
     * @return 无返回值；具体归档文件名、导出和失败提示由 PageArchiveController 负责。
     */
    fun saveCurrentPageArchive() {
        pageArchiveController.saveCurrentPageArchive()
    }

    /**
     * 打印当前页面。
     *
     * @return 无返回值；具体打印任务由 PagePrintController 负责。
     */
    fun printCurrentPage() {
        pagePrintController.printCurrentPage()
    }

    /**
     * 从播放历史继续打开内容。
     *
     * @param progress 播放历史记录，包含来源类型、媒体标识、标题和播放位置等信息。
     * @return 无返回值；网页来源会回到浏览器，原生媒体来源会打开原生播放器。
     */
    fun openPlaybackHistoryItem(progress: PlaybackProgress) {
        if (progress.source == PlaybackHistorySource.WEB_PAGE) {
            loadUrl(progress.mediaIdentity)
        } else {
            openNativePlayer(
                progress.mediaIdentity,
                PlaybackHistoryDisplayText.title(progress)
            )
        }
    }
}
