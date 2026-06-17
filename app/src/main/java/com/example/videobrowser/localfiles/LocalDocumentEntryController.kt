package com.example.videobrowser.localfiles

/**
 * 初学者阅读提示：
 * 这个文件属于“本地文件模块”。
 * 文件名 LocalDocumentEntryController 可以拆开理解为“Local Document Entry Controller”，
 * 表示它只负责本地文件入口和本地文档打开后的浏览器接入。
 * 主要职责：初始化本地文件选择器、显示本地文件页、把本地 Uri 交给页面动作控制器、
 * 以及把 MHTML/MHT 归档 URL 加载到当前浏览器标签页。
 * 阅读顺序：先看构造参数了解它连接哪些模块，再看 openLocalDocumentUri() 和
 * loadLocalDocumentUrlInBrowser() 的分工。
 */
import android.net.Uri
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.BrowserSessionController
import com.example.videobrowser.browser.PageActionsController
import com.example.videobrowser.video.ExternalSubtitleCandidate
import com.example.videobrowser.video.PlaybackQueue

/**
 * 本地文档入口控制器。
 *
 * LocalFilesController 负责读取系统文件和目录；PageActionsController 负责判断本地 Uri 应该
 * 在浏览器还是原生播放器中打开。本类只负责把这两个模块和当前浏览器状态连接起来。
 *
 * @param localFilesController 本地文件控制器，用于初始化系统文件选择器并显示本地文件页。
 * @param pageActionsController 返回页面动作控制器的函数，用于打开本地文档 Uri。
 * @param closeFunctionCenter 关闭功能中心的函数，加载本地网页归档前会调用。
 * @param currentSessionController 返回当前浏览器会话控制器的函数，用于写入当前页面 URL。
 * @param currentBrowserManager 返回当前浏览器管理器的函数，用于在 WebView 中加载本地归档 URL。
 * @param updateAddressBar 刷新地址栏文本的函数，参数是即将显示的 URL。
 * @param hideKeyboard 隐藏软键盘的函数，加载本地归档前会调用。
 * @param showHomeContent 控制首页内容显示状态的函数，参数 false 表示显示 WebView 内容。
 */
class LocalDocumentEntryController(
    private val localFilesController: LocalFilesController,
    private val pageActionsController: () -> PageActionsController,
    private val closeFunctionCenter: () -> Boolean,
    private val currentSessionController: () -> BrowserSessionController,
    private val currentBrowserManager: () -> BrowserManager,
    private val updateAddressBar: (String?) -> Unit,
    private val hideKeyboard: () -> Unit,
    private val showHomeContent: (Boolean) -> Unit
) {
    /**
     * 初始化本地文件相关系统选择器。
     *
     * @return 无返回值；后续用户点击本地文件入口时会复用这些 launcher。
     */
    fun setupFileOperationLaunchers() {
        localFilesController.setupLaunchers()
    }

    /**
     * 显示本地文件操作页面。
     *
     * @return 无返回值；具体页面内容由 LocalFilesController 构建。
     */
    fun showFileOperationsPage() {
        localFilesController.showFileOperationsPage()
    }

    /**
     * 打开系统文件选择器或本地目录返回的文档 Uri。
     *
     * @param uri 系统返回的文档 Uri，可能是媒体文件、字幕文件或网页归档。
     * @param displayName 系统或本地目录读取到的展示名称，可能为空。
     * @param mimeType 系统或本地目录读取到的 MIME 类型，可能为空。
     * @param subtitleCandidates 同目录匹配到的外部字幕候选列表。
     * @param playbackQueue 同目录媒体构建出的播放队列，打开原生播放器时会传递。
     * @return 无返回值；具体路由由 PageActionsController 决定。
     */
    fun openLocalDocumentUri(
        uri: Uri,
        displayName: String? = null,
        mimeType: String? = null,
        subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList(),
        playbackQueue: PlaybackQueue? = null
    ) {
        pageActionsController().openLocalDocumentUri(
            uri,
            displayName,
            mimeType,
            subtitleCandidates,
            playbackQueue
        )
    }

    /**
     * 在当前浏览器标签页中加载本地网页归档 URL。
     *
     * @param url 要在 WebView 中加载的本地文档 URL。
     * @return 无返回值；函数会同步地址栏、首页显示状态和当前会话 URL。
     */
    fun loadLocalDocumentUrlInBrowser(url: String) {
        closeFunctionCenter()
        currentSessionController().currentPageUrl = url
        updateAddressBar(url)
        hideKeyboard()
        showHomeContent(false)
        currentBrowserManager().load(url)
    }
}
