package com.example.videobrowser.functioncenter

import com.example.videobrowser.browser.BrowserTab
import org.junit.Assert.assertEquals
import org.junit.Test

class BrowserTabDisplayTextTest {
    @Test
    fun titlePrefersPageTitle() {
        val tab = BrowserTab(id = 1L, url = "https://example.com", title = "Example")

        assertEquals("Example", BrowserTabDisplayText.title(tab, untitledText = "新标签页"))
    }

    @Test
    fun titleFallsBackToUrlThenUntitledText() {
        assertEquals(
            "example.com",
            BrowserTabDisplayText.title(
                BrowserTab(id = 1L, url = "https://example.com", title = ""),
                untitledText = "新标签页"
            )
        )
        assertEquals(
            "新标签页",
            BrowserTabDisplayText.title(
                BrowserTab(id = 2L, url = null, title = ""),
                untitledText = "新标签页"
            )
        )
    }
}
