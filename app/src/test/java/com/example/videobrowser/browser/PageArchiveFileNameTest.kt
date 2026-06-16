package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Page Archive File Name Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PageArchiveFileNameTest {
    @Test
    fun createUsesSanitizedPageTitle() {
        val fileName = PageArchiveFileName.create(
            pageTitle = "  My / Video: Page?  ",
            pageUrl = "https://example.com/watch",
            fallbackName = "VideoBrowser"
        )

        assertEquals("My _ Video_ Page.mhtml", fileName)
    }

    @Test
    fun createFallsBackToHostWhenTitleIsBlank() {
        val fileName = PageArchiveFileName.create(
            pageTitle = "   ",
            pageUrl = "https://m.example.com/watch",
            fallbackName = "VideoBrowser"
        )

        assertEquals("m.example.com.mhtml", fileName)
    }

    @Test
    fun createFallsBackToAppNameWhenTitleAndUrlAreBlank() {
        val fileName = PageArchiveFileName.create(
            pageTitle = "",
            pageUrl = null,
            fallbackName = "VideoBrowser"
        )

        assertEquals("VideoBrowser.mhtml", fileName)
    }

    @Test
    fun createKeepsExtensionAfterTruncatingLongTitles() {
        val fileName = PageArchiveFileName.create(
            pageTitle = "A".repeat(120),
            pageUrl = "https://example.com",
            fallbackName = "VideoBrowser"
        )

        assertTrue(fileName.endsWith(".mhtml"))
        assertEquals(86, fileName.length)
    }
}
