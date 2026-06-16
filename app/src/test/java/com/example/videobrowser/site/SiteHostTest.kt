package com.example.videobrowser.site

/**
 * 测试阅读提示：
 * 这个测试文件验证“Site Host Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SiteHostTest {
    /**
     * 测试函数 `fromUrl_extractsNormalizedHostFromHttpUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `from Url extracts Normalized Host From Http Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fromUrl_extractsNormalizedHostFromHttpUrl() {
        assertEquals(
            "www.example.com",
            SiteHost.fromUrl("https://WWW.Example.COM:443/watch?v=1")
        )
    }

    /**
     * 测试函数 `normalize_trimsCaseAndTrailingDots`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `normalize trims Case And Trailing Dots` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun normalize_trimsCaseAndTrailingDots() {
        assertEquals("video.example.com", SiteHost.normalize(" Video.Example.COM. "))
    }

    /**
     * 测试函数 `fromUrl_returnsNullForNonSiteUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `from Url returns Null For Non Site Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fromUrl_returnsNullForNonSiteUrls() {
        assertNull(SiteHost.fromUrl("about:blank"))
    }
}
