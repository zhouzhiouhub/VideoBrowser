package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Resource Type Resolver Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class ResourceTypeResolverTest {
    /**
     * 测试函数 `resolve_returnsDocumentForMainFrameRequests`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve returns Document For Main Frame Requests` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolve_returnsDocumentForMainFrameRequests() {
        val type = ResourceTypeResolver.resolve(
            requestUrl = "https://example.com/watch",
            isForMainFrame = true
        )

        assertEquals(ResourceType.DOCUMENT, type)
    }

    /**
     * 测试函数 `resolve_infersScriptAndImageFromHeaders`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve infers Script And Image From Headers` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolve_infersScriptAndImageFromHeaders() {
        assertEquals(
            ResourceType.SCRIPT,
            ResourceTypeResolver.resolve(
                requestUrl = "https://cdn.example.com/app",
                isForMainFrame = false,
                requestHeaders = mapOf("Sec-Fetch-Dest" to "script")
            )
        )
        assertEquals(
            ResourceType.IMAGE,
            ResourceTypeResolver.resolve(
                requestUrl = "https://cdn.example.com/banner",
                isForMainFrame = false,
                requestHeaders = mapOf("Accept" to "image/avif,image/webp,*/*")
            )
        )
    }

    /**
     * 测试函数 `resolve_infersMediaFromUrlSuffix`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve infers Media From Url Suffix` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolve_infersMediaFromUrlSuffix() {
        val type = ResourceTypeResolver.resolve(
            requestUrl = "https://video.example.com/live/playlist.m3u8?token=1",
            isForMainFrame = false
        )

        assertEquals(ResourceType.MEDIA, type)
    }

    /**
     * 测试函数 `resolve_returnsUnknownWhenRequestIsAmbiguous`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve returns Unknown When Request Is Ambiguous` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolve_returnsUnknownWhenRequestIsAmbiguous() {
        val type = ResourceTypeResolver.resolve(
            requestUrl = "https://example.com/api/opaque",
            isForMainFrame = false,
            requestHeaders = mapOf("Accept" to "*/*")
        )

        assertEquals(ResourceType.UNKNOWN, type)
    }
}
