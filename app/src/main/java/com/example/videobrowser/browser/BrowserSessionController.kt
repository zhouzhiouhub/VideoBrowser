package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserSessionController 可以拆开理解为“Browser Session Controller”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R

/**
 * 单个浏览会话的页面状态机。
 *
 * 它不直接持有 WebView，只记录“当前 URL、标题、加载中、是否首页、进度”等状态。
 * MainActivity 为普通模式和无痕模式各创建一个实例，从而让两个模式的页面状态互不干扰。
 */
class BrowserSessionController(
    private val activity: AppCompatActivity,
    private val isActive: () -> Boolean,
    private val clearElementPickerState: () -> Unit,
    private val exitPageFullscreenIfNeeded: () -> Unit,
    private val isProviderHomeUrl: (String?) -> Boolean,
    private val updateAddressBar: (String?) -> Unit,
    private val showHomeContent: (Boolean) -> Unit,
    private val setPageProgress: (Int) -> Unit,
    private val updatePageProgressVisibility: (Boolean) -> Unit,
    private val updateNavigationButtons: () -> Unit,
    private val addHistoryEntry: (String?) -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val onPageMetadataChanged: (String?, String?) -> Unit = { _, _ -> }
) {
    var currentPageTitle = ""
        private set

    @Volatile
    var currentPageUrl: String? = null

    var isPageLoading = false
        private set

    var isHomePageVisible = true
        private set

    private var pageProgress = 0

    /**
     * 页面开始加载时重置标题、进度和元素选择状态。
     */
    fun handlePageStarted(url: String?) {
        if (isActive()) {
            clearElementPickerState()
            exitPageFullscreenIfNeeded()
        }
        currentPageUrl = url ?: currentPageUrl
        isHomePageVisible = isProviderHomeUrl(url)
        resetPageTitle()
        isPageLoading = true
        pageProgress = 0
        notifyPageMetadataChanged()
        renderIfActive()
    }

    /**
     * 函数 `handlePageFinished`：处理 `handle Page Finished` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    fun handlePageFinished(url: String?) {
        // 页面结束加载后才写历史并注入页面增强脚本，避免还没加载完就操作 DOM。
        currentPageUrl = url ?: currentPageUrl
        isHomePageVisible = isProviderHomeUrl(url)
        isPageLoading = false
        pageProgress = 100
        addHistoryEntry(url)
        notifyPageMetadataChanged()
        if (isActive()) {
            renderCurrentState(forceProgressHidden = true)
            injectPageFeatures()
        }
    }

    /**
     * 函数 `handlePageFailed`：处理 `handle Page Failed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    fun handlePageFailed(url: String?) {
        currentPageUrl = url ?: currentPageUrl
        isHomePageVisible = false
        isPageLoading = false
        pageProgress = 100
        notifyPageMetadataChanged()
        if (isActive()) {
            renderCurrentState(forceProgressHidden = true)
        }
    }

    /**
     * 函数 `handlePageProgressChanged`：处理 `handle Page Progress Changed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param newProgress 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    fun handlePageProgressChanged(newProgress: Int) {
        val normalizedProgress = newProgress.coerceIn(0, 100)
        isPageLoading = normalizedProgress in 1..99
        pageProgress = normalizedProgress
        if (isActive()) {
            setPageProgress(pageProgress)
            updatePageProgressVisibility(false)
            updateNavigationButtons()
        }
    }

    /**
     * 函数 `handlePageTitleReceived`：处理 `handle Page Title Received` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    fun handlePageTitleReceived(title: String) {
        val normalizedTitle = title.trim()
        currentPageTitle = normalizedTitle
        notifyPageMetadataChanged()
        updateActivityTitleIfActive()
    }

    /**
     * 函数 `renderCurrentState`：封装 `render Current State` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param forceProgressHidden 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    fun renderCurrentState(forceProgressHidden: Boolean = !isPageLoading) {
        // renderCurrentState 是“把内存状态同步到 UI”的唯一出口，切换标签或退出无痕时会复用。
        if (!isActive()) {
            return
        }
        updateActivityTitleIfActive()
        updateAddressBar(currentPageUrl)
        showHomeContent(isHomePageVisible)
        setPageProgress(pageProgress)
        updatePageProgressVisibility(forceProgressHidden)
        updateNavigationButtons()
    }

    /**
     * 函数 `reset`：封装 `reset` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun reset() {
        currentPageTitle = ""
        currentPageUrl = null
        isPageLoading = false
        isHomePageVisible = true
        pageProgress = 0
        notifyPageMetadataChanged()
        renderCurrentState(forceProgressHidden = true)
    }

    /**
     * 函数 `restorePageMetadata`：封装 `restore Page Metadata` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    fun restorePageMetadata(url: String?, title: String) {
        currentPageUrl = url
        currentPageTitle = title.trim()
        isHomePageVisible = url?.let(isProviderHomeUrl) ?: true
        isPageLoading = false
        pageProgress = if (url == null) 0 else 100
        notifyPageMetadataChanged()
        renderCurrentState(forceProgressHidden = true)
    }

    /**
     * 函数 `resetPageTitle`：封装 `reset Page Title` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun resetPageTitle() {
        currentPageTitle = ""
        updateActivityTitleIfActive()
    }

    /**
     * 函数 `renderIfActive`：封装 `render If Active` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun renderIfActive() {
        if (isActive()) {
            renderCurrentState(forceProgressHidden = false)
        }
    }

    /**
     * 函数 `updateActivityTitleIfActive`：根据最新状态刷新 `update Activity Title If Active` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updateActivityTitleIfActive() {
        if (!isActive()) {
            return
        }
        activity.title = currentPageTitle.takeIf { it.isNotBlank() }
            ?: activity.getString(R.string.app_name)
    }

    /**
     * 函数 `notifyPageMetadataChanged`：封装 `notify Page Metadata Changed` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun notifyPageMetadataChanged() {
        onPageMetadataChanged(currentPageUrl, currentPageTitle)
    }
}
