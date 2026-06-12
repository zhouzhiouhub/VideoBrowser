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
