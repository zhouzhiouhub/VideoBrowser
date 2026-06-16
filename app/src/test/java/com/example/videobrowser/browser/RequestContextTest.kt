package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Request Context Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class RequestContextTest {
    /**
     * 测试函数 `requestContext_normalizesRequestAndPageHosts`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `request Context normalizes Request And Page Hosts` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun requestContext_normalizesRequestAndPageHosts() {
        val context = RequestContext(
            requestUrl = "https://CDN.Example.com/assets/player.js",
            pageUrl = "https://Video.Example.com/watch",
            method = "GET",
            requestHeaders = mapOf("Sec-Fetch-Dest" to "script")
        )

        assertEquals("cdn.example.com", context.requestHost)
        assertEquals("video.example.com", context.pageHost)
        assertEquals("https", context.requestScheme)
        assertEquals("GET", context.method)
        assertEquals(ResourceType.SCRIPT, context.resourceType)
    }
}
