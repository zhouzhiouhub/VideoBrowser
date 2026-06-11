package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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

        assertEquals(1, tabs.tabs().size)
    }

    @Test
    fun updatesActiveTabMetadata() {
        val tabs = BrowserTabStore()

        tabs.updateActiveTab(url = "https://example.com/video", title = "Video")

        assertEquals("https://example.com/video", tabs.activeTab().url)
        assertEquals("Video", tabs.activeTab().title)
    }
}
