package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Site Security Status Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class SiteSecurityStatusTest {
    @Test
    fun fromUrl_marksHttpsSecure() {
        val status = SiteSecurityStatus.fromUrl("https://example.com")

        assertEquals(SiteSecurityStatus.SECURE, status)
        assertEquals("HTTPS", status.protocolDisplayName())
        assertEquals(true, status.isEncryptedConnection())
    }

    @Test
    fun fromUrl_marksHttpNotSecure() {
        val status = SiteSecurityStatus.fromUrl("http://example.com")

        assertEquals(SiteSecurityStatus.NOT_SECURE, status)
        assertEquals("HTTP", status.protocolDisplayName())
        assertEquals(false, status.isEncryptedConnection())
    }

    @Test
    fun fromUrl_ignoresNonWebAndBlankUrls() {
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl("about:blank"))
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl("mailto:a@example.com"))
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl(null))
        assertEquals("未知", SiteSecurityStatus.UNKNOWN.protocolDisplayName())
        assertEquals(false, SiteSecurityStatus.UNKNOWN.isEncryptedConnection())
    }
}
