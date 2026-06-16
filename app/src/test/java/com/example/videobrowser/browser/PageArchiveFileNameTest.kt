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
    /**
     * 测试函数 `createUsesSanitizedPageTitle`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `create Uses Sanitized Page Title` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun createUsesSanitizedPageTitle() {
        val fileName = PageArchiveFileName.create(
            pageTitle = "  My / Video: Page?  ",
            pageUrl = "https://example.com/watch",
            fallbackName = "VideoBrowser"
        )

        assertEquals("My _ Video_ Page.mhtml", fileName)
    }

    /**
     * 测试函数 `createFallsBackToHostWhenTitleIsBlank`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `create Falls Back To Host When Title Is Blank` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun createFallsBackToHostWhenTitleIsBlank() {
        val fileName = PageArchiveFileName.create(
            pageTitle = "   ",
            pageUrl = "https://m.example.com/watch",
            fallbackName = "VideoBrowser"
        )

        assertEquals("m.example.com.mhtml", fileName)
    }

    /**
     * 测试函数 `createFallsBackToAppNameWhenTitleAndUrlAreBlank`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `create Falls Back To App Name When Title And Url Are Blank` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun createFallsBackToAppNameWhenTitleAndUrlAreBlank() {
        val fileName = PageArchiveFileName.create(
            pageTitle = "",
            pageUrl = null,
            fallbackName = "VideoBrowser"
        )

        assertEquals("VideoBrowser.mhtml", fileName)
    }

    /**
     * 测试函数 `createKeepsExtensionAfterTruncatingLongTitles`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `create Keeps Extension After Truncating Long Titles` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
