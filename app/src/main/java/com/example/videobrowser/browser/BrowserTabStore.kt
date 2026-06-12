package com.example.videobrowser.browser

class BrowserTabStore(
    private val idGenerator: () -> Long = IdSequence()
) {
    private val tabs = mutableListOf(BrowserTab(id = idGenerator()))

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
        tabs.removeAt(index)
        if (activeTabId == tabId) {
            activeTabId = tabs.getOrNull(index)?.id ?: tabs.last().id
        }
        return true
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
        activeTabId = restoredActiveTabId
            ?.takeIf { tabId -> tabs.any { tab -> tab.id == tabId } }
            ?: tabs.first().id
        return true
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
}
