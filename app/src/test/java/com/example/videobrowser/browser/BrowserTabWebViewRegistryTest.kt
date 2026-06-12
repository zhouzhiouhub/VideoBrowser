package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BrowserTabWebViewRegistryTest {
    @Test
    fun startsWithInitialViewForInitialActiveTab() {
        val registry = registry(initialTabId = 1L, initialWebView = "webView-1")

        assertEquals(1L, registry.activeTabId)
        assertEquals("webView-1", registry.activeWebView())
        assertEquals("webView-1", registry.webViewFor(1L))
    }

    @Test
    fun activatesExistingTabViewWithoutCreatingAnotherView() {
        val calls = RegistryCalls()
        val registry = registry(
            initialTabId = 1L,
            initialWebView = "webView-1",
            calls = calls
        )
        registry.activate(2L)
        calls.created.clear()
        calls.shown.clear()
        calls.hidden.clear()

        val activeView = registry.activate(1L)

        assertEquals("webView-1", activeView)
        assertEquals(1L, registry.activeTabId)
        assertEquals(emptyList<String>(), calls.created)
        assertEquals(listOf("webView-2"), calls.hidden)
        assertEquals(listOf("webView-1"), calls.shown)
    }

    @Test
    fun createsIndependentViewForNewTab() {
        val calls = RegistryCalls()
        val registry = registry(
            initialTabId = 1L,
            initialWebView = "webView-1",
            calls = calls
        )

        val activeView = registry.activate(2L)

        assertEquals("webView-2", activeView)
        assertEquals("webView-2", registry.activeWebView())
        assertEquals("webView-1", registry.webViewFor(1L))
        assertEquals("webView-2", registry.webViewFor(2L))
        assertEquals(listOf("webView-2"), calls.created)
        assertEquals(listOf("webView-1"), calls.hidden)
        assertEquals(listOf("webView-2"), calls.shown)
    }

    @Test
    fun openTabStoresInitialUrlAndTitle() {
        val tabs = BrowserTabStore()
        val registry = BrowserTabWebViewRegistry(tabs, initialView = "webView-1")

        val result = registry.openTab(
            view = "webView-2",
            url = "https://example.com/video",
            title = "Video"
        )

        assertEquals("https://example.com/video", result.tab.url)
        assertEquals("Video", result.tab.title)
        assertEquals(result.tab.id, tabs.activeTabId)
        assertEquals("webView-2", registry.activeWebView())
    }

    @Test
    fun closingActiveTabShowsFallbackViewAndDestroysClosedView() {
        val calls = RegistryCalls()
        val registry = registry(
            initialTabId = 1L,
            initialWebView = "webView-1",
            calls = calls
        )
        registry.activate(2L)
        calls.shown.clear()

        val fallbackView = registry.close(tabId = 2L, fallbackActiveTabId = 1L)

        assertEquals("webView-1", fallbackView)
        assertEquals(1L, registry.activeTabId)
        assertEquals("webView-1", registry.activeWebView())
        assertNull(registry.webViewFor(2L))
        assertEquals(listOf("webView-1"), calls.shown)
        assertEquals(listOf("webView-2"), calls.destroyed)
    }

    @Test
    fun closingInactiveTabDestroysOnlyThatView() {
        val calls = RegistryCalls()
        val registry = registry(
            initialTabId = 1L,
            initialWebView = "webView-1",
            calls = calls
        )
        registry.activate(2L)
        calls.destroyed.clear()

        val fallbackView = registry.close(tabId = 1L, fallbackActiveTabId = 2L)

        assertNull(fallbackView)
        assertEquals(2L, registry.activeTabId)
        assertEquals("webView-2", registry.activeWebView())
        assertNull(registry.webViewFor(1L))
        assertEquals(listOf("webView-1"), calls.destroyed)
    }

    @Test
    fun switchToRestoredTabCreatesMissingViewLazily() {
        val calls = RegistryCalls()
        val tabs = BrowserTabStore()
        tabs.restore(
            restoredTabs = listOf(
                BrowserTab(id = 1L, url = "https://a.example.com"),
                BrowserTab(id = 2L, url = "https://b.example.com")
            ),
            restoredActiveTabId = 1L
        )
        val registry = BrowserTabWebViewRegistry(
            tabs = tabs,
            initialView = "webView-1",
            createWebView = {
                val webView = "webView-${calls.created.size + 2}"
                calls.created += webView
                webView
            },
            showWebView = { calls.shown += it },
            hideWebView = { calls.hidden += it },
            destroyWebView = { calls.destroyed += it }
        )

        val result = registry.switchTo(2L)

        assertEquals("webView-2", result?.activeView)
        assertEquals("webView-2", registry.activeWebView())
        assertEquals(listOf("webView-2"), calls.created)
    }

    @Test
    fun closeRestoredInactiveTabWithoutViewKeepsActiveView() {
        val tabs = BrowserTabStore()
        tabs.restore(
            restoredTabs = listOf(
                BrowserTab(id = 1L, url = "https://a.example.com"),
                BrowserTab(id = 2L, url = "https://b.example.com")
            ),
            restoredActiveTabId = 1L
        )
        val registry = BrowserTabWebViewRegistry(
            tabs = tabs,
            initialView = "webView-1",
            createWebView = { "webView-new" },
            showWebView = {},
            hideWebView = {},
            destroyWebView = {}
        )

        val result = registry.closeTab(2L)

        assertNull(result?.closedView)
        assertEquals("webView-1", result?.activeView)
        assertEquals(1L, registry.activeTabId)
        assertEquals(1, tabs.tabs().size)
    }

    @Test
    fun closeOtherTabsReturnsClosedViewsAndKeepsTargetViewActive() {
        val calls = RegistryCalls()
        val tabs = BrowserTabStore()
        tabs.restore(
            restoredTabs = listOf(
                BrowserTab(id = 1L, url = "https://a.example.com"),
                BrowserTab(id = 2L, url = "https://b.example.com"),
                BrowserTab(id = 3L, url = "https://c.example.com")
            ),
            restoredActiveTabId = 1L
        )
        val registry = BrowserTabWebViewRegistry(
            tabs = tabs,
            initialView = "webView-1",
            createWebView = {
                val webView = "webView-${calls.created.size + 2}"
                calls.created += webView
                webView
            },
            showWebView = { calls.shown += it },
            hideWebView = { calls.hidden += it },
            destroyWebView = { calls.destroyed += it }
        )
        registry.switchTo(2L)
        registry.switchTo(3L)

        val result = registry.closeOtherTabs(2L)

        assertEquals(listOf("webView-1", "webView-3"), result?.closedViews)
        assertEquals("webView-2", result?.activeView)
        assertEquals(2L, result?.activeTab?.id)
        assertEquals(2L, registry.activeTabId)
        assertEquals(listOf(2L), tabs.tabs().map { tab -> tab.id })
        assertNull(registry.webViewFor(1L))
        assertEquals("webView-2", registry.webViewFor(2L))
        assertNull(registry.webViewFor(3L))
    }

    @Test
    fun closeOtherTabsCreatesMissingTargetViewForRestoredTab() {
        val calls = RegistryCalls()
        val tabs = BrowserTabStore()
        tabs.restore(
            restoredTabs = listOf(
                BrowserTab(id = 1L, url = "https://a.example.com"),
                BrowserTab(id = 2L, url = "https://b.example.com")
            ),
            restoredActiveTabId = 1L
        )
        val registry = BrowserTabWebViewRegistry(
            tabs = tabs,
            initialView = "webView-1",
            createWebView = {
                val webView = "webView-${calls.created.size + 2}"
                calls.created += webView
                webView
            },
            showWebView = { calls.shown += it },
            hideWebView = { calls.hidden += it },
            destroyWebView = { calls.destroyed += it }
        )

        val result = registry.closeOtherTabs(2L)

        assertEquals(listOf("webView-1"), result?.closedViews)
        assertEquals("webView-2", result?.activeView)
        assertEquals(listOf("webView-2"), calls.created)
        assertEquals(2L, registry.activeTabId)
        assertEquals(listOf(2L), tabs.tabs().map { tab -> tab.id })
    }

    private fun registry(
        initialTabId: Long,
        initialWebView: String,
        calls: RegistryCalls = RegistryCalls()
    ): BrowserTabWebViewRegistry<String> {
        return BrowserTabWebViewRegistry(
            initialTabId = initialTabId,
            initialWebView = initialWebView,
            createWebView = {
                val webView = "webView-${calls.created.size + 2}"
                calls.created += webView
                webView
            },
            showWebView = { calls.shown += it },
            hideWebView = { calls.hidden += it },
            destroyWebView = { calls.destroyed += it }
        )
    }

    private class RegistryCalls {
        val created = mutableListOf<String>()
        val shown = mutableListOf<String>()
        val hidden = mutableListOf<String>()
        val destroyed = mutableListOf<String>()
    }
}
