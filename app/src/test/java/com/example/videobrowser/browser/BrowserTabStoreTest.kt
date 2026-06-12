package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserTabStoreTest {
    @Test
    fun startsWithOneActiveTab() {
        val tabs = BrowserTabStore()

        assertEquals(1, tabs.tabs().size)
        assertEquals(tabs.activeTabId, tabs.activeTab().id)
        assertTrue(tabs.activeTab().url == null)
    }

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

    @Test
    fun switchesActiveTabById() {
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId
        tabs.openTab(url = "https://example.com")

        assertTrue(tabs.switchTo(firstTabId))

        assertEquals(firstTabId, tabs.activeTabId)
    }

    @Test
    fun closingActiveTabActivatesNeighbor() {
        val tabs = BrowserTabStore()
        val firstTabId = tabs.activeTabId
        val secondTab = tabs.openTab(url = "https://example.com")

        assertTrue(tabs.closeTab(secondTab.id))

        assertEquals(firstTabId, tabs.activeTabId)
        assertEquals(1, tabs.tabs().size)
    }

    @Test
    fun keepsLastTabOpen() {
        val tabs = BrowserTabStore()

        assertFalse(tabs.closeTab(tabs.activeTabId))
        assertFalse(tabs.canReopenClosedTab())

        assertEquals(1, tabs.tabs().size)
    }

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

    @Test
    fun reopenClosedTabReturnsNullWhenNothingWasClosed() {
        val tabs = BrowserTabStore()

        assertNull(tabs.reopenClosedTab())
    }

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

    @Test
    fun closingOtherTabsRejectsUnknownTarget() {
        val tabs = BrowserTabStore()
        val secondTab = tabs.openTab(url = "https://example.com")

        val closedTabs = tabs.closeOtherTabs(99L)

        assertEquals(emptyList<BrowserTab>(), closedTabs)
        assertEquals(2, tabs.tabs().size)
        assertEquals(secondTab.id, tabs.activeTabId)
    }

    @Test
    fun updatesActiveTabMetadata() {
        val tabs = BrowserTabStore()

        tabs.updateActiveTab(url = "https://example.com/video", title = "Video")

        assertEquals("https://example.com/video", tabs.activeTab().url)
        assertEquals("Video", tabs.activeTab().title)
    }

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

    @Test
    fun restoreRejectsEmptyInput() {
        val tabs = BrowserTabStore()
        val initialActiveTabId = tabs.activeTabId

        assertFalse(tabs.restore(emptyList(), restoredActiveTabId = null))

        assertEquals(initialActiveTabId, tabs.activeTabId)
        assertEquals(1, tabs.tabs().size)
    }
}
