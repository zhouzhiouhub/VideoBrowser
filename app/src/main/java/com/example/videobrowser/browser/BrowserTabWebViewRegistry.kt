package com.example.videobrowser.browser

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

    fun activeTab(): BrowserTab {
        return requireTabs().activeTab()
    }

    fun activeWebView(): T {
        return viewsByTabId.getValue(activeViewTabId)
    }

    fun activeView(): T {
        return activeWebView()
    }

    fun webViewFor(tabId: Long): T? {
        return viewsByTabId[tabId]
    }

    fun viewFor(tabId: Long): T? {
        return webViewFor(tabId)
    }

    fun activate(tabId: Long): T {
        val currentWebView = activeWebView()
        val nextWebView = viewsByTabId.getOrPut(tabId) {
            requireCreateWebView().invoke()
        }
        activeViewTabId = tabId
        if (currentWebView !== nextWebView) {
            hideWebView(currentWebView)
            showWebView(nextWebView)
        }
        return nextWebView
    }

    fun close(tabId: Long, fallbackActiveTabId: Long): T? {
        val closedWebView = viewsByTabId.remove(tabId) ?: return null
        val fallbackWebView = if (activeViewTabId == tabId) {
            viewsByTabId.getOrPut(fallbackActiveTabId) {
                requireCreateWebView().invoke()
            }.also { nextWebView ->
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

    fun switchTo(tabId: Long): SwitchResult<T>? {
        val previousView = activeWebView()
        val tabStore = requireTabs()
        if (!tabStore.switchTo(tabId)) {
            return null
        }
        val nextView = viewsByTabId.getOrPut(tabId) {
            requireCreateWebView().invoke()
        }
        activeViewTabId = tabId
        return SwitchResult(
            previousView = previousView,
            activeView = nextView,
            activeTab = tabStore.activeTab()
        )
    }

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
            viewsByTabId.getOrPut(activeViewTabId) {
                requireCreateWebView().invoke()
            }
        }
        return CloseResult(
            closedView = closedView,
            activeView = activeWebView(),
            activeTab = tabStore.activeTab()
        )
    }

    fun closeOtherTabs(tabId: Long): CloseOthersResult<T>? {
        val tabStore = requireTabs()
        val closedTabs = tabStore.closeOtherTabs(tabId)
        if (closedTabs.isEmpty()) {
            return null
        }
        val closedViews = closedTabs.mapNotNull { tab -> viewsByTabId.remove(tab.id) }
        val activeView = viewsByTabId.getOrPut(tabId) {
            requireCreateWebView().invoke()
        }
        activeViewTabId = tabId
        return CloseOthersResult(
            closedViews = closedViews,
            activeView = activeView,
            activeTab = tabStore.activeTab()
        )
    }

    fun destroyAll(destroyView: (T) -> Unit = destroyWebView) {
        val views = viewsByTabId.values.toList()
        viewsByTabId.clear()
        views.forEach(destroyView)
    }

    private fun requireTabs(): BrowserTabStore {
        return requireNotNull(tabs) { "BrowserTabStore is required for this operation." }
    }

    private fun requireCreateWebView(): () -> T {
        return requireNotNull(createWebView) { "createWebView is required for this operation." }
    }
}
