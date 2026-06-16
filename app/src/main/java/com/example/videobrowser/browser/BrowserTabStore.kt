package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserTabStore 可以拆开理解为“Browser Tab Store”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
/**
 * 纯内存的标签页列表。
 *
 * 这里不操作 WebView，只维护 BrowserTab 数据：当前标签、关闭标签、恢复最近关闭标签。
 * 这样标签页行为可以用普通单元测试验证，不需要启动 Android 设备。
 */
class BrowserTabStore(
    private val idGenerator: () -> Long = IdSequence()
) {
    private val tabs = mutableListOf(BrowserTab(id = idGenerator()))
    private val recentlyClosedTabs = mutableListOf<BrowserTab>()

    var activeTabId: Long = tabs.first().id
        private set

    fun tabs(): List<BrowserTab> {
        return tabs.toList()
    }

    fun activeTab(): BrowserTab {
        return tabs.first { tab -> tab.id == activeTabId }
    }

    fun openTab(url: String? = null, title: String = ""): BrowserTab {
        val tab = BrowserTab(
            id = nextUniqueId(),
            url = url,
            title = title
        )
        tabs += tab
        activeTabId = tab.id
        return tab
    }

    fun switchTo(tabId: Long): Boolean {
        if (tabs.none { tab -> tab.id == tabId }) {
            return false
        }
        activeTabId = tabId
        return true
    }

    fun closeTab(tabId: Long): Boolean {
        // 浏览器至少保留一个标签页，避免 UI 没有 activeTab 可以显示。
        if (tabs.size == 1) {
            return false
        }
        val index = tabs.indexOfFirst { tab -> tab.id == tabId }
        if (index < 0) {
            return false
        }
        val closedTab = tabs.removeAt(index)
        rememberClosedTabs(listOf(closedTab))
        if (activeTabId == tabId) {
            activeTabId = tabs.getOrNull(index)?.id ?: tabs.last().id
        }
        return true
    }

    fun closeOtherTabs(tabId: Long): List<BrowserTab> {
        val targetTab = tabs.firstOrNull { tab -> tab.id == tabId } ?: return emptyList()
        val closedTabs = tabs.filter { tab -> tab.id != tabId }
        if (closedTabs.isEmpty()) {
            return emptyList()
        }

        rememberClosedTabs(closedTabs)
        tabs.clear()
        tabs += targetTab
        activeTabId = tabId
        return closedTabs
    }

    fun closeAllTabs(): List<BrowserTab> {
        val closedTabs = tabs.toList()
        rememberClosedTabs(closedTabs)
        val blankTab = BrowserTab(id = nextUniqueId())
        tabs.clear()
        tabs += blankTab
        activeTabId = blankTab.id
        return closedTabs
    }

    fun canReopenClosedTab(): Boolean {
        return recentlyClosedTabs.isNotEmpty()
    }

    fun reopenClosedTab(): BrowserTab? {
        if (recentlyClosedTabs.isEmpty()) {
            return null
        }
        val closedTab = recentlyClosedTabs.removeAt(recentlyClosedTabs.lastIndex)
        return openTab(url = closedTab.url, title = closedTab.title)
    }

    fun updateActiveTab(url: String? = activeTab().url, title: String = activeTab().title) {
        updateTab(activeTabId, url, title)
    }

    fun updateTab(tabId: Long, url: String?, title: String): Boolean {
        val index = tabs.indexOfFirst { tab -> tab.id == tabId }
        if (index < 0) {
            return false
        }
        tabs[index] = tabs[index].copy(url = url, title = title)
        return true
    }

    fun restore(restoredTabs: List<BrowserTab>, restoredActiveTabId: Long?): Boolean {
        val normalizedTabs = restoredTabs
            .filter { tab -> tab.id > 0L }
            .distinctBy { tab -> tab.id }
            .takeIf { it.isNotEmpty() }
            ?: return false

        tabs.clear()
        tabs.addAll(normalizedTabs)
        recentlyClosedTabs.clear()
        activeTabId = restoredActiveTabId
            ?.takeIf { tabId -> tabs.any { tab -> tab.id == tabId } }
            ?: tabs.first().id
        return true
    }

    private fun rememberClosedTabs(closedTabs: List<BrowserTab>) {
        // 最近关闭列表只保留有限数量，防止用户长时间使用后内存无限增长。
        recentlyClosedTabs.addAll(closedTabs)
        if (recentlyClosedTabs.size > MAX_RECENTLY_CLOSED_TABS) {
            recentlyClosedTabs.subList(0, recentlyClosedTabs.size - MAX_RECENTLY_CLOSED_TABS).clear()
        }
    }

    private fun nextUniqueId(): Long {
        while (true) {
            val candidate = idGenerator()
            if (tabs.none { tab -> tab.id == candidate }) {
                return candidate
            }
        }
    }

    private class IdSequence : () -> Long {
        private var nextId = 1L

        override fun invoke(): Long {
            return nextId++
        }
    }

    private companion object {
        private const val MAX_RECENTLY_CLOSED_TABS = 20
    }
}
