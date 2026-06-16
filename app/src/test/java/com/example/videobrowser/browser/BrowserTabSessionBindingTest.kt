package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Tab Session Binding Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
