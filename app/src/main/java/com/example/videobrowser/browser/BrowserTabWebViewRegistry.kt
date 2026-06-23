package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserTabWebViewRegistry 可以拆开理解为“Browser Tab Web View Registry”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
class BrowserTabWebViewRegistry<T : Any> private constructor(
    private val tabs: BrowserTabStore?,
    initialTabId: Long,
    initialWebView: T,
    private val createWebView: (() -> T)?,
    private val showWebView: (T) -> Unit,
    private val hideWebView: (T) -> Unit,
    private val destroyWebView: (T) -> Unit
) {
    data class OpenedTab<T : Any>(
        val tab: BrowserTab,
        val previousView: T,
        val activeView: T
    )

    data class SwitchResult<T : Any>(
        val previousView: T,
        val activeView: T,
        val activeTab: BrowserTab
    )

    data class CloseResult<T : Any>(
        val closedView: T?,
        val activeView: T,
        val activeTab: BrowserTab
    )

    data class CloseOthersResult<T : Any>(
        val closedViews: List<T>,
        val activeView: T,
        val activeTab: BrowserTab
    )

    data class CloseAllResult<T : Any>(
        val closedViews: List<T>,
        val activeView: T,
        val activeTab: BrowserTab
    )

    data class ReplaceResult<T : Any>(
        val previousView: T,
        val replacementView: T,
        val replacedActiveView: Boolean,
        val activeTab: BrowserTab
    )

    constructor(
        tabs: BrowserTabStore,
        initialView: T
    ) : this(
        tabs = tabs,
        initialTabId = tabs.activeTabId,
        initialWebView = initialView,
        createWebView = null,
        showWebView = {},
        hideWebView = {},
        destroyWebView = {}
    )

    constructor(
        tabs: BrowserTabStore,
        initialView: T,
        createWebView: () -> T,
        showWebView: (T) -> Unit,
        hideWebView: (T) -> Unit,
        destroyWebView: (T) -> Unit
    ) : this(
        tabs = tabs,
        initialTabId = tabs.activeTabId,
        initialWebView = initialView,
        createWebView = createWebView,
        showWebView = showWebView,
        hideWebView = hideWebView,
        destroyWebView = destroyWebView
    )

    constructor(
        initialTabId: Long,
        initialWebView: T,
        createWebView: () -> T,
        showWebView: (T) -> Unit,
        hideWebView: (T) -> Unit,
        destroyWebView: (T) -> Unit
    ) : this(
        tabs = null,
        initialTabId = initialTabId,
        initialWebView = initialWebView,
        createWebView = createWebView,
        showWebView = showWebView,
        hideWebView = hideWebView,
        destroyWebView = destroyWebView
    )

    private val viewsByTabId = mutableMapOf(initialTabId to initialWebView)
    private var activeViewTabId = initialTabId

    val activeTabId: Long
        get() = tabs?.activeTabId ?: activeViewTabId

    /**
     * 函数 `activeTab`：封装 `active Tab` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun activeTab(): BrowserTab {
        return requireTabs().activeTab()
    }

    /**
     * 函数 `activeWebView`：封装 `active Web View` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun activeWebView(): T {
        return viewsByTabId.getValue(activeViewTabId)
    }

    /**
     * 函数 `activeView`：封装 `active View` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun activeView(): T {
        return activeWebView()
    }

    /**
     * 函数 `webViewFor`：封装 `web View For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun webViewFor(tabId: Long): T? {
        return viewsByTabId[tabId]
    }

    /**
     * 函数 `viewFor`：封装 `view For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun viewFor(tabId: Long): T? {
        return webViewFor(tabId)
    }

    /**
     * 函数 `tabIdFor`：封装 `tab Id For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `T`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun tabIdFor(view: T): Long? {
        return viewsByTabId.entries
            .firstOrNull { (_, tabView) -> tabView === view }
            ?.key
    }

    /**
     * 函数 `replaceView`：封装 `replace View` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @param replacementView 参数类型为 `T`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun replaceView(tabId: Long, replacementView: T): ReplaceResult<T>? {
        val previousView = viewsByTabId[tabId] ?: return null
        val tabStore = requireTabs()
        val replacedActiveView = activeViewTabId == tabId
        viewsByTabId[tabId] = replacementView
        return ReplaceResult(
            previousView = previousView,
            replacementView = replacementView,
            replacedActiveView = replacedActiveView,
            activeTab = tabStore.activeTab()
        )
    }

    /**
     * 函数 `activate`：封装 `activate` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun activate(tabId: Long): T {
        val currentWebView = activeWebView()
        val nextWebView = ensureViewFor(tabId)
        activeViewTabId = tabId
        if (currentWebView !== nextWebView) {
            hideWebView(currentWebView)
            showWebView(nextWebView)
        }
        return nextWebView
    }

    /**
     * 函数 `close`：控制 `close` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @param fallbackActiveTabId 参数类型为 `Long`，表示函数执行 `fallbackActiveTabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun close(tabId: Long, fallbackActiveTabId: Long): T? {
        val closedWebView = viewsByTabId.remove(tabId) ?: return null
        val fallbackWebView = if (activeViewTabId == tabId) {
            ensureViewFor(fallbackActiveTabId).also { nextWebView ->
                activeViewTabId = fallbackActiveTabId
                if (closedWebView !== nextWebView) {
                    showWebView(nextWebView)
                }
            }
        } else {
            null
        }
        destroyWebView(closedWebView)
        return fallbackWebView
    }

    /**
     * 函数 `openTab`：启动或加载 `open Tab` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `T`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun openTab(view: T, url: String? = null, title: String = ""): OpenedTab<T> {
        val previousView = activeWebView()
        val tab = requireTabs().openTab(url = url, title = title)
        viewsByTabId[tab.id] = view
        activeViewTabId = tab.id
        return OpenedTab(
            tab = tab,
            previousView = previousView,
            activeView = view
        )
    }

    /**
     * 函数 `switchTo`：封装 `switch To` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun switchTo(tabId: Long): SwitchResult<T>? {
        val previousView = activeWebView()
        val tabStore = requireTabs()
        if (!tabStore.switchTo(tabId)) {
            return null
        }
        val nextView = ensureViewFor(tabId)
        activeViewTabId = tabId
        return SwitchResult(
            previousView = previousView,
            activeView = nextView,
            activeTab = tabStore.activeTab()
        )
    }

    /**
     * 函数 `closeTab`：控制 `close Tab` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun closeTab(tabId: Long): CloseResult<T>? {
        val closedView = viewsByTabId[tabId]
        val tabStore = requireTabs()
        if (!tabStore.closeTab(tabId)) {
            return null
        }
        if (closedView != null) {
            viewsByTabId.remove(tabId)
        }
        if (activeViewTabId == tabId) {
            activeViewTabId = tabStore.activeTabId
            ensureViewFor(activeViewTabId)
        }
        return CloseResult(
            closedView = closedView,
            activeView = activeWebView(),
            activeTab = tabStore.activeTab()
        )
    }

    /**
     * 函数 `closeOtherTabs`：控制 `close Other Tabs` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun closeOtherTabs(tabId: Long): CloseOthersResult<T>? {
        val tabStore = requireTabs()
        val closedTabs = tabStore.closeOtherTabs(tabId)
        if (closedTabs.isEmpty()) {
            return null
        }
        val closedViews = removeViewsFor(closedTabs)
        val activeView = ensureViewFor(tabId)
        activeViewTabId = tabId
        return CloseOthersResult(
            closedViews = closedViews,
            activeView = activeView,
            activeTab = tabStore.activeTab()
        )
    }

    /**
     * 函数 `closeAllTabs`：控制 `close All Tabs` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun closeAllTabs(): CloseAllResult<T> {
        val tabStore = requireTabs()
        val closedTabs = tabStore.closeAllTabs()
        val closedViews = removeViewsFor(closedTabs)
        val activeTab = tabStore.activeTab()
        val activeView = ensureViewFor(activeTab.id)
        activeViewTabId = activeTab.id
        return CloseAllResult(
            closedViews = closedViews,
            activeView = activeView,
            activeTab = activeTab
        )
    }

    /**
     * 函数 `destroyAll`：封装 `destroy All` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param destroyView 参数类型为 `(T) -> Unit`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     */
    fun destroyAll(destroyView: (T) -> Unit = destroyWebView) {
        val views = viewsByTabId.values.toList()
        viewsByTabId.clear()
        views.forEach(destroyView)
    }

    /**
     * 函数 `requireTabs`：封装 `require Tabs` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun requireTabs(): BrowserTabStore {
        return requireNotNull(tabs) { "BrowserTabStore is required for this operation." }
    }

    private fun ensureViewFor(tabId: Long): T {
        return viewsByTabId.getOrPut(tabId) {
            requireCreateWebView().invoke()
        }
    }

    private fun removeViewsFor(closedTabs: List<BrowserTab>): List<T> {
        return closedTabs.mapNotNull { tab -> viewsByTabId.remove(tab.id) }
    }

    /**
     * 函数 `requireCreateWebView`：封装 `require Create Web View` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun requireCreateWebView(): () -> T {
        return requireNotNull(createWebView) { "createWebView is required for this operation." }
    }
}
