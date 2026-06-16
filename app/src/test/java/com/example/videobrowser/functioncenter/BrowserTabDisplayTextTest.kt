package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Tab Display Text Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
