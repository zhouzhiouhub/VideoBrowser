package com.example.videobrowser.browser

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
