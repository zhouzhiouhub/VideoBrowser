package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class BrowserTabSessionBindingTest {
    @Test
    fun syncsPageMetadataToActiveTab() {
        val tabs = BrowserTabStore()
        val binding = BrowserTabSessionBinding(tabs)

        binding.handlePageMetadataChanged(
            url = "https://example.com/watch",
            title = "Watch"
        )

        assertEquals("https://example.com/watch", tabs.activeTab().url)
        assertEquals("Watch", tabs.activeTab().title)
    }

    @Test
    fun keepsExistingTitleWhenOnlyUrlChanges() {
        val tabs = BrowserTabStore()
        val binding = BrowserTabSessionBinding(tabs)
        binding.handlePageMetadataChanged(url = "https://example.com", title = "Example")

        binding.handlePageMetadataChanged(url = "https://example.com/next", title = null)

        assertEquals("https://example.com/next", tabs.activeTab().url)
        assertEquals("Example", tabs.activeTab().title)
    }
}
