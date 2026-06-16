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
    /**
     * 测试函数 `shouldBlock_blocksHttpImageSubresourcesWhenEnabled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Block blocks Http Image Subresources When Enabled` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `shouldBlock_allowsMainFrameImagesSoDirectImagePagesStillOpen`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Block allows Main Frame Images So Direct Image Pages Still Open` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `shouldBlock_allowsNonImageAndNonHttpResources`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Block allows Non Image And Non Http Resources` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `shouldBlock_respectsGlobalAndCurrentSiteSwitches`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Block respects Global And Current Site Switches` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
