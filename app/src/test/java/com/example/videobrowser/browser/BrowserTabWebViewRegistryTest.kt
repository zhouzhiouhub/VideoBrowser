package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Tab Web View Registry Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BrowserTabWebViewRegistryTest {
    @Test
    fun missingTabViewCreationUsesSharedHelper() {
        val registry = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabWebViewRegistry.kt"
        ).readText()

        assertEquals(1, Regex("viewsByTabId\\.getOrPut").findAll(registry).count())
        assertEquals(7, Regex("ensureViewFor\\(").findAll(registry).count())
        assertEquals(1, Regex("requireCreateWebView\\(\\)\\.invoke\\(\\)").findAll(registry).count())
        assertEquals(1, Regex("private fun ensureViewFor\\(tabId: Long\\): T").findAll(registry).count())
        assertEquals(1, Regex("private fun removeViewsFor\\(closedTabs: List<BrowserTab>\\): List<T>").findAll(registry).count())
        assertEquals(2, Regex("removeViewsFor\\(closedTabs\\)").findAll(registry).count())
        assertEquals(1, Regex("viewsByTabId\\.remove\\(tab\\.id\\)").findAll(registry).count())
    }

    /**
     * 测试函数 `startsWithInitialViewForInitialActiveTab`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `starts With Initial View For Initial Active Tab` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun startsWithInitialViewForInitialActiveTab() {
        val registry = registry(initialTabId = 1L, initialWebView = "webView-1")

        assertEquals(1L, registry.activeTabId)
        assertEquals("webView-1", registry.activeWebView())
        assertEquals("webView-1", registry.webViewFor(1L))
    }

    /**
     * 测试函数 `activatesExistingTabViewWithoutCreatingAnotherView`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `activates Existing Tab View Without Creating Another View` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `createsIndependentViewForNewTab`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `creates Independent View For New Tab` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `openTabStoresInitialUrlAndTitle`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `open Tab Stores Initial Url And Title` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `resolvesTabIdForRegisteredViewByIdentity`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolves Tab Id For Registered View By Identity` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolvesTabIdForRegisteredViewByIdentity() {
        val firstView = FakeView("first")
        val secondView = FakeView("second")
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId
        val registry = BrowserTabWebViewRegistry(tabs, initialView = firstView)

        val secondTab = registry.openTab(view = secondView).tab

        assertEquals(firstTabId, registry.tabIdFor(firstView))
        assertEquals(secondTab.id, registry.tabIdFor(secondView))
        assertNull(registry.tabIdFor(FakeView("second")))
    }

    /**
     * 测试函数 `replaceViewSwapsTabViewWithoutChangingActiveTab`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `replace View Swaps Tab View Without Changing Active Tab` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun replaceViewSwapsTabViewWithoutChangingActiveTab() {
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId
        val secondTab = tabs.openTab(url = "https://second.example.com")
        tabs.switchTo(firstTabId)
        val registry = BrowserTabWebViewRegistry(
            tabs = tabs,
            initialView = "webView-1",
            createWebView = { "webView-2" },
            showWebView = {},
            hideWebView = {},
            destroyWebView = {}
        )
        registry.activate(secondTab.id)
        registry.activate(firstTabId)

        val result = registry.replaceView(secondTab.id, "webView-2b")

        assertEquals("webView-2", result?.previousView)
        assertEquals("webView-2b", result?.replacementView)
        assertEquals(false, result?.replacedActiveView)
        assertEquals(firstTabId, registry.activeTabId)
        assertEquals("webView-1", registry.activeWebView())
        assertEquals("webView-2b", registry.webViewFor(secondTab.id))
    }

    /**
     * 测试函数 `closingActiveTabShowsFallbackViewAndDestroysClosedView`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `closing Active Tab Shows Fallback View And Destroys Closed View` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `closingInactiveTabDestroysOnlyThatView`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `closing Inactive Tab Destroys Only That View` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `switchToRestoredTabCreatesMissingViewLazily`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `switch To Restored Tab Creates Missing View Lazily` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `closeRestoredInactiveTabWithoutViewKeepsActiveView`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `close Restored Inactive Tab Without View Keeps Active View` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `closeOtherTabsReturnsClosedViewsAndKeepsTargetViewActive`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `close Other Tabs Returns Closed Views And Keeps Target View Active` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `closeOtherTabsCreatesMissingTargetViewForRestoredTab`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `close Other Tabs Creates Missing Target View For Restored Tab` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `closeAllTabsReturnsClosedViewsAndCreatesNewActiveView`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `close All Tabs Returns Closed Views And Creates New Active View` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun closeAllTabsReturnsClosedViewsAndCreatesNewActiveView() {
        val calls = RegistryCalls()
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId
        val registry = BrowserTabWebViewRegistry(
            tabs = tabs,
            initialView = "webView-1",
            createWebView = {
                val webView = "webView-${calls.created.size + 3}"
                calls.created += webView
                webView
            },
            showWebView = { calls.shown += it },
            hideWebView = { calls.hidden += it },
            destroyWebView = { calls.destroyed += it }
        )
        val secondTab = registry.openTab(
            view = "webView-2",
            url = "https://second.example.com",
            title = "Second"
        ).tab

        val result = registry.closeAllTabs()

        assertEquals(listOf("webView-1", "webView-2"), result.closedViews)
        assertEquals("webView-3", result.activeView)
        assertEquals(tabs.activeTabId, result.activeTab.id)
        assertEquals(listOf(result.activeTab.id), tabs.tabs().map { tab -> tab.id })
        assertEquals(result.activeTab.id, registry.activeTabId)
        assertNull(registry.webViewFor(firstTabId))
        assertNull(registry.webViewFor(secondTab.id))
        assertEquals("webView-3", registry.activeWebView())
    }

    /**
     * 测试函数 `registry`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `registry` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param initialTabId 参数类型为 `Long`，表示函数执行 `initialTabId` 相关逻辑时需要读取或处理的输入。
     * @param initialWebView 参数类型为 `String`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param calls 参数类型为 `RegistryCalls`，表示函数执行 `calls` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    private data class FakeView(val name: String)
}
