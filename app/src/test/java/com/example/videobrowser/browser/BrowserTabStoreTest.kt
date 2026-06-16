package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Tab Store Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserTabStoreTest {
    /**
     * 测试函数 `startsWithOneActiveTab`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `starts With One Active Tab` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun startsWithOneActiveTab() {
        val tabs = BrowserTabStore()

        assertEquals(1, tabs.tabs().size)
        assertEquals(tabs.activeTabId, tabs.activeTab().id)
        assertTrue(tabs.activeTab().url == null)
    }

    /**
     * 测试函数 `opensNewTabsAndActivatesTheNewTab`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `opens New Tabs And Activates The New Tab` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun opensNewTabsAndActivatesTheNewTab() {
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId

        val secondTab = tabs.openTab(url = "https://example.com", title = "Example")

        assertNotEquals(firstTabId, secondTab.id)
        assertEquals(secondTab.id, tabs.activeTabId)
        assertEquals("https://example.com", tabs.activeTab().url)
        assertEquals("Example", tabs.activeTab().title)
        assertEquals(2, tabs.tabs().size)
    }

    /**
     * 测试函数 `switchesActiveTabById`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `switches Active Tab By Id` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun switchesActiveTabById() {
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId
        tabs.openTab(url = "https://example.com")

        assertTrue(tabs.switchTo(firstTabId))

        assertEquals(firstTabId, tabs.activeTabId)
    }

    /**
     * 测试函数 `closingActiveTabActivatesNeighbor`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `closing Active Tab Activates Neighbor` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun closingActiveTabActivatesNeighbor() {
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId
        val secondTab = tabs.openTab(url = "https://example.com")

        assertTrue(tabs.closeTab(secondTab.id))

        assertEquals(firstTabId, tabs.activeTabId)
        assertEquals(1, tabs.tabs().size)
    }

    /**
     * 测试函数 `keepsLastTabOpen`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `keeps Last Tab Open` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun keepsLastTabOpen() {
        val tabs = BrowserTabStore()

        assertFalse(tabs.closeTab(tabs.activeTabId))
        assertFalse(tabs.canReopenClosedTab())

        assertEquals(1, tabs.tabs().size)
    }

    /**
     * 测试函数 `reopensRecentlyClosedTabWithNewId`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `reopens Recently Closed Tab With New Id` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun reopensRecentlyClosedTabWithNewId() {
        val tabs = BrowserTabStore()
        val secondTab = tabs.openTab(url = "https://example.com", title = "Example")

        assertTrue(tabs.closeTab(secondTab.id))
        assertTrue(tabs.canReopenClosedTab())

        val reopenedTab = requireNotNull(tabs.reopenClosedTab())
        assertNotEquals(secondTab.id, reopenedTab.id)
        assertEquals("https://example.com", reopenedTab.url)
        assertEquals("Example", reopenedTab.title)
        assertEquals(reopenedTab.id, tabs.activeTabId)
        assertFalse(tabs.canReopenClosedTab())
    }

    /**
     * 测试函数 `reopenClosedTabReturnsNullWhenNothingWasClosed`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `reopen Closed Tab Returns Null When Nothing Was Closed` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun reopenClosedTabReturnsNullWhenNothingWasClosed() {
        val tabs = BrowserTabStore()

        assertNull(tabs.reopenClosedTab())
    }

    /**
     * 测试函数 `closingOtherTabsKeepsTargetTabAndActivatesIt`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `closing Other Tabs Keeps Target Tab And Activates It` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun closingOtherTabsKeepsTargetTabAndActivatesIt() {
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId
        val secondTab = tabs.openTab(url = "https://example.com/second", title = "Second")
        val thirdTab = tabs.openTab(url = "https://example.com/third", title = "Third")

        val closedTabs = tabs.closeOtherTabs(secondTab.id)

        assertEquals(listOf(firstTabId, thirdTab.id), closedTabs.map { tab -> tab.id })
        assertEquals(listOf(secondTab.id), tabs.tabs().map { tab -> tab.id })
        assertEquals(secondTab.id, tabs.activeTabId)
        assertEquals("https://example.com/second", tabs.activeTab().url)
    }

    /**
     * 测试函数 `closingOtherTabsAddsClosedTabsToReopenStack`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `closing Other Tabs Adds Closed Tabs To Reopen Stack` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun closingOtherTabsAddsClosedTabsToReopenStack() {
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId
        val secondTab = tabs.openTab(url = "https://example.com/second", title = "Second")
        val thirdTab = tabs.openTab(url = "https://example.com/third", title = "Third")

        tabs.closeOtherTabs(firstTabId)

        val reopenedTab = requireNotNull(tabs.reopenClosedTab())
        assertNotEquals(thirdTab.id, reopenedTab.id)
        assertEquals("https://example.com/third", reopenedTab.url)
        assertEquals("Third", reopenedTab.title)
        assertEquals(listOf(firstTabId, reopenedTab.id), tabs.tabs().map { tab -> tab.id })
        assertTrue(tabs.canReopenClosedTab())

        val nextReopenedTab = requireNotNull(tabs.reopenClosedTab())
        assertNotEquals(secondTab.id, nextReopenedTab.id)
        assertEquals("https://example.com/second", nextReopenedTab.url)
    }

    /**
     * 测试函数 `closingOtherTabsRejectsUnknownTarget`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `closing Other Tabs Rejects Unknown Target` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun closingOtherTabsRejectsUnknownTarget() {
        val tabs = BrowserTabStore()
        val secondTab = tabs.openTab(url = "https://example.com")

        val closedTabs = tabs.closeOtherTabs(99L)

        assertEquals(emptyList<BrowserTab>(), closedTabs)
        assertEquals(2, tabs.tabs().size)
        assertEquals(secondTab.id, tabs.activeTabId)
    }

    /**
     * 测试函数 `closingAllTabsCreatesNewBlankActiveTabAndKeepsClosedTabsReopenable`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `closing All Tabs Creates New Blank Active Tab And Keeps Closed Tabs Reopenable` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun closingAllTabsCreatesNewBlankActiveTabAndKeepsClosedTabsReopenable() {
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId
        tabs.updateActiveTab(url = "https://first.example.com", title = "First")
        val secondTab = tabs.openTab(url = "https://second.example.com", title = "Second")

        val closedTabs = tabs.closeAllTabs()

        assertEquals(listOf(firstTabId, secondTab.id), closedTabs.map { tab -> tab.id })
        assertEquals(1, tabs.tabs().size)
        assertEquals(tabs.tabs().single().id, tabs.activeTabId)
        assertEquals(null, tabs.activeTab().url)
        assertEquals("", tabs.activeTab().title)
        assertTrue(tabs.canReopenClosedTab())

        val reopenedTab = requireNotNull(tabs.reopenClosedTab())
        assertEquals("https://second.example.com", reopenedTab.url)
        assertEquals("Second", reopenedTab.title)
    }

    /**
     * 测试函数 `updatesActiveTabMetadata`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `updates Active Tab Metadata` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun updatesActiveTabMetadata() {
        val tabs = BrowserTabStore()

        tabs.updateActiveTab(url = "https://example.com/video", title = "Video")

        assertEquals("https://example.com/video", tabs.activeTab().url)
        assertEquals("Video", tabs.activeTab().title)
    }

    /**
     * 测试函数 `restoresTabsAndActiveTab`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `restores Tabs And Active Tab` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun restoresTabsAndActiveTab() {
        val tabs = BrowserTabStore()

        assertTrue(
            tabs.restore(
                restoredTabs = listOf(
                    BrowserTab(id = 4L, url = "https://a.example.com", title = "A"),
                    BrowserTab(id = 8L, url = "https://b.example.com", title = "B")
                ),
                restoredActiveTabId = 8L
            )
        )

        assertEquals(2, tabs.tabs().size)
        assertEquals(8L, tabs.activeTabId)
        assertEquals("https://b.example.com", tabs.activeTab().url)
    }

    /**
     * 测试函数 `restoringTabsClearsRecentlyClosedTabs`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `restoring Tabs Clears Recently Closed Tabs` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun restoringTabsClearsRecentlyClosedTabs() {
        val tabs = BrowserTabStore()
        val secondTab = tabs.openTab(url = "https://example.com")
        tabs.closeTab(secondTab.id)

        assertTrue(tabs.canReopenClosedTab())

        assertTrue(
            tabs.restore(
                restoredTabs = listOf(BrowserTab(id = 4L, url = "https://a.example.com")),
                restoredActiveTabId = 4L
            )
        )

        assertFalse(tabs.canReopenClosedTab())
    }

    /**
     * 测试函数 `restoreRejectsEmptyInput`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `restore Rejects Empty Input` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun restoreRejectsEmptyInput() {
        val tabs = BrowserTabStore()
        val initialActiveTabId = tabs.activeTabId

        assertFalse(tabs.restore(emptyList(), restoredActiveTabId = null))

        assertEquals(initialActiveTabId, tabs.activeTabId)
        assertEquals(1, tabs.tabs().size)
    }
}
