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
    @Test
    fun fromUrl_extractsNormalizedHostFromHttpUrl() {
        assertEquals(
            "www.example.com",
            SiteHost.fromUrl("https://WWW.Example.COM:443/watch?v=1")
        )
    }

    @Test
    fun normalize_trimsCaseAndTrailingDots() {
        assertEquals("video.example.com", SiteHost.normalize(" Video.Example.COM. "))
    }

    @Test
    fun fromUrl_returnsNullForNonSiteUrls() {
        assertNull(SiteHost.fromUrl("about:blank"))
    }
}
