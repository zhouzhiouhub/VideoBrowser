package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Smart No Image Request Policy Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartNoImageRequestPolicyTest {
    @Test
    fun shouldBlock_blocksHttpImageSubresourcesWhenEnabled() {
        assertTrue(
            SmartNoImageRequestPolicy.shouldBlock(
                enabled = true,
                context = RequestContext(
                    requestUrl = "https://cdn.example.com/poster.webp",
                    pageUrl = "https://video.example.com/watch",
                    isForMainFrame = false
                )
            )
        )
    }

    @Test
    fun shouldBlock_allowsMainFrameImagesSoDirectImagePagesStillOpen() {
        assertFalse(
            SmartNoImageRequestPolicy.shouldBlock(
                enabled = true,
                context = RequestContext(
                    requestUrl = "https://cdn.example.com/poster.webp",
                    isForMainFrame = true
                )
            )
        )
    }

    @Test
    fun shouldBlock_allowsNonImageAndNonHttpResources() {
        assertFalse(
            SmartNoImageRequestPolicy.shouldBlock(
                enabled = true,
                context = RequestContext(
                    requestUrl = "https://cdn.example.com/app.js",
                    requestHeaders = mapOf("Sec-Fetch-Dest" to "script")
                )
            )
        )
        assertFalse(
            SmartNoImageRequestPolicy.shouldBlock(
                enabled = true,
                context = RequestContext(
                    requestUrl = "data:image/png;base64,AA==",
                    requestHeaders = mapOf("Accept" to "image/png")
                )
            )
        )
    }

    @Test
    fun shouldBlock_respectsGlobalAndCurrentSiteSwitches() {
        val context = RequestContext(
            requestUrl = "https://cdn.example.com/poster.jpg",
            pageUrl = "https://video.example.com/watch"
        )

        assertFalse(
            SmartNoImageRequestPolicy.shouldBlock(
                enabled = false,
                context = context
            )
        )
        assertFalse(
            SmartNoImageRequestPolicy.shouldBlock(
                enabled = true,
                siteSmartNoImageDisabled = true,
                context = context
            )
        )
    }
}
