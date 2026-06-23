package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器标签页动作模块”。
 * 标签页的数据由 BrowserTabStore 保存，标准模式的每个标签页还会绑定一个独立 WebView。
 * 主要职责：打开、恢复、切换、关闭、关闭其他/全部、复制标签页，并把当前标签页内容展示出来。
 * 阅读顺序：先看 openNewTab 和 switchTab，再看 closeTab/closeOtherTabs/closeAllTabs，最后看 showActiveTab。
 */
import android.webkit.WebView

/**
 * 浏览器标签页动作控制器。
 *
 * MainActivity 只保留给功能中心和上下文菜单调用的入口；本类负责标准/无痕两套标签页动作的分支。
 *
 * @param standardTabStore 参数类型为 `BrowserTabStore`，表示标准模式标签页数据源。
 * @param privateTabStore 参数类型为 `BrowserTabStore`，表示无痕模式标签页数据源。
 * @param standardTabWebViews 参数类型为 `BrowserTabWebViewRegistry<WebView>`，表示标准标签页 ID 到 WebView 的映射表。
 * @param standardSessionController 参数类型为 `BrowserSessionController`，表示标准模式页面状态控制器，用来恢复标签页标题和 URL。
 * @param isPrivateBrowsingActive 参数类型为 `() -> Boolean`，表示读取当前是否处于无痕模式的回调。
 * @param createStandardTabWebView 参数类型为 `() -> WebView`，表示创建标准标签页 WebView 的回调。
 * @param showStandardTabWebView 参数类型为 `(WebView) -> Unit`，表示把标准标签页 WebView 切到前台的回调。
 * @param hideStandardTabWebView 参数类型为 `(WebView) -> Unit`，表示隐藏标准标签页 WebView 的回调。
 * @param destroyStandardTabWebView 参数类型为 `(WebView) -> Unit`，表示销毁标准标签页 WebView 的回调。
 * @param closeFunctionCenter 参数类型为 `() -> Boolean`，表示关闭功能中心面板的回调。
 * @param saveStandardTabSession 参数类型为 `() -> Unit`，表示保存标准标签页会话的回调。
 * @param loadUrl 参数类型为 `(String) -> Unit`，表示加载标签页 URL 的回调。
 * @param openHomePage 参数类型为 `() -> Unit`，表示打开主页的回调。
 */
class BrowserTabActionsController(
    private val standardTabStore: BrowserTabStore,
    private val privateTabStore: BrowserTabStore,
    private val standardTabWebViews: BrowserTabWebViewRegistry<WebView>,
    private val standardSessionController: BrowserSessionController,
    private val isPrivateBrowsingActive: () -> Boolean,
    private val createStandardTabWebView: () -> WebView,
    private val showStandardTabWebView: (WebView) -> Unit,
    private val hideStandardTabWebView: (WebView) -> Unit,
    private val destroyStandardTabWebView: (WebView) -> Unit,
    private val closeFunctionCenter: () -> Boolean,
    private val saveStandardTabSession: () -> Unit,
    private val loadUrl: (String) -> Unit,
    private val openHomePage: () -> Unit
) {
    /**
     * 函数 `currentTabs`：返回当前浏览模式下的标签页列表。
     *
     * @return 返回 `List<BrowserTab>`，调用方通常用它渲染标签页列表。
     */
    fun currentTabs(): List<BrowserTab> {
        return currentTabStore().tabs()
    }

    /**
     * 函数 `activeTabId`：返回当前浏览模式下的活动标签页 ID。
     *
     * @return 返回 `Long`，表示当前高亮/展示的标签页 ID。
     */
    fun activeTabId(): Long {
        return currentTabStore().activeTabId
    }

    /**
     * 函数 `openNewTab`：打开一个空白新标签页并跳转主页。
     *
     * 初学者阅读提示：标准模式需要创建新 WebView；无痕模式只维护内存标签数据。
     */
    fun openNewTab() {
        closeFunctionCenter()
        if (!isPrivateBrowsingActive()) {
            openStandardTab()
        } else {
            currentTabStore().openTab()
            saveStandardTabSession()
        }
        openHomePage()
    }

    /**
     * 函数 `canReopenClosedTab`：判断当前模式是否有可恢复的关闭标签页。
     *
     * @return 返回 `Boolean`，true 表示“恢复关闭标签页”按钮可以启用。
     */
    fun canReopenClosedTab(): Boolean {
        return currentTabStore().canReopenClosedTab()
    }

    /**
     * 函数 `reopenClosedTab`：恢复最近关闭的标签页。
     *
     * 初学者阅读提示：标准模式恢复后要激活对应 WebView；无痕模式恢复后直接加载恢复 URL。
     */
    fun reopenClosedTab() {
        if (!isPrivateBrowsingActive()) {
            val reopenedTab = standardTabStore.reopenClosedTab() ?: return
            standardTabWebViews.activate(reopenedTab.id)
            saveStandardTabSession()
            reopenedTab.url?.let(loadUrl) ?: openHomePage()
            return
        }

        val reopenedTab = currentTabStore().reopenClosedTab() ?: return
        reopenedTab.url?.let(loadUrl) ?: openHomePage()
    }

    /**
     * 函数 `switchTab`：切换到指定标签页。
     *
     * @param tabId 参数类型为 `Long`，表示要切换到的标签页 ID。
     */
    fun switchTab(tabId: Long) {
        closeFunctionCenter()
        if (!isPrivateBrowsingActive()) {
            val result = standardTabWebViews.switchTo(tabId) ?: return
            if (result.previousView !== result.activeView) {
                hideStandardTabWebView(result.previousView)
                showStandardTabWebView(result.activeView)
            }
            showActiveTab(result.activeTab)
            saveStandardTabSession()
        } else {
            val tabStore = currentTabStore()
            if (!tabStore.switchTo(tabId)) {
                return
            }
            showActiveTab(tabStore.activeTab())
        }
    }

    /**
     * 函数 `closeTab`：关闭指定标签页。
     *
     * @param tabId 参数类型为 `Long`，表示要关闭的标签页 ID。
     */
    fun closeTab(tabId: Long) {
        if (!isPrivateBrowsingActive()) {
            val closingActiveTab = standardTabStore.activeTabId == tabId
            val result = standardTabWebViews.closeTab(tabId) ?: return
            if (closingActiveTab && result.closedView !== result.activeView) {
                showStandardTabWebView(result.activeView)
            }
            result.closedView?.let(destroyStandardTabWebView)
            if (closingActiveTab) {
                showActiveTab(result.activeTab)
            }
            saveStandardTabSession()
            return
        }

        val tabStore = currentTabStore()
        val closingActiveTab = tabStore.activeTabId == tabId
        if (!tabStore.closeTab(tabId) || !closingActiveTab) {
            return
        }
        showActiveTab(tabStore.activeTab())
    }

    /**
     * 函数 `closeOtherTabs`：关闭指定标签页之外的所有标签页。
     *
     * @param tabId 参数类型为 `Long`，表示需要保留的标签页 ID。
     */
    fun closeOtherTabs(tabId: Long) {
        if (!isPrivateBrowsingActive()) {
            val previousView = standardTabWebViews.activeWebView()
            val result = standardTabWebViews.closeOtherTabs(tabId) ?: return
            if (previousView !== result.activeView) {
                hideStandardTabWebView(previousView)
                showStandardTabWebView(result.activeView)
            }
            result.closedViews.forEach(destroyStandardTabWebView)
            showActiveTab(result.activeTab)
            saveStandardTabSession()
            return
        }

        val tabStore = currentTabStore()
        val closedTabs = tabStore.closeOtherTabs(tabId)
        if (closedTabs.isEmpty()) {
            return
        }
        showActiveTab(tabStore.activeTab())
    }

    /**
     * 函数 `closeAllTabs`：关闭当前模式下的全部标签页并打开主页。
     */
    fun closeAllTabs() {
        if (!isPrivateBrowsingActive()) {
            val result = standardTabWebViews.closeAllTabs()
            showStandardTabWebView(result.activeView)
            result.closedViews.forEach(destroyStandardTabWebView)
            saveStandardTabSession()
            openHomePage()
            return
        }

        currentTabStore().closeAllTabs()
        openHomePage()
    }

    /**
     * 函数 `duplicateTab`：复制指定标签页并切换到新标签页。
     *
     * @param tabId 参数类型为 `Long`，表示要复制的源标签页 ID。
     */
    fun duplicateTab(tabId: Long) {
        val sourceTab = currentTabStore().tabs().firstOrNull { tab -> tab.id == tabId } ?: return
        if (!isPrivateBrowsingActive()) {
            openStandardTab(
                url = sourceTab.url,
                title = sourceTab.title
            )
        } else {
            currentTabStore().openTab(url = sourceTab.url, title = sourceTab.title)
        }
        sourceTab.url?.let(loadUrl) ?: openHomePage()
    }

    /**
     * 函数 `openUrlInNewTab`：在新标签页中打开指定 URL。
     *
     * @param url 参数类型为 `String`，表示要在新标签页加载的目标 URL。
     */
    fun openUrlInNewTab(url: String) {
        if (!isPrivateBrowsingActive()) {
            openStandardTab(url = url)
        } else {
            currentTabStore().openTab(url = url)
        }
        loadUrl(url)
    }

    private fun openStandardTab(url: String? = null, title: String = "") {
        val result = standardTabWebViews.openTab(
            view = createStandardTabWebView(),
            url = url,
            title = title
        )
        hideStandardTabWebView(result.previousView)
        showStandardTabWebView(result.activeView)
        saveStandardTabSession()
    }

    /**
     * 函数 `showActiveTab`：把标签页数据同步到当前浏览显示状态。
     *
     * @param tab 参数类型为 `BrowserTab`，表示要展示的标签页数据。
     */
    private fun showActiveTab(tab: BrowserTab) {
        if (!isPrivateBrowsingActive()) {
            standardTabWebViews.viewFor(tab.id)?.let(showStandardTabWebView)
            standardSessionController.restorePageMetadata(tab.url, tab.title)
            return
        }

        tab.url?.let(loadUrl) ?: openHomePage()
    }

    /**
     * 函数 `currentTabStore`：按当前浏览模式选择标准或无痕标签页数据源。
     *
     * @return 返回 `BrowserTabStore`，后续动作会在该数据源上执行。
     */
    private fun currentTabStore(): BrowserTabStore {
        return if (isPrivateBrowsingActive()) privateTabStore else standardTabStore
    }
}
