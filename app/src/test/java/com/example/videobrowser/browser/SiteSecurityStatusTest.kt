package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Site Security Status Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class SiteSecurityStatusTest {
    /**
     * 测试函数 `fromUrl_marksHttpsSecure`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `from Url marks Https Secure` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fromUrl_marksHttpsSecure() {
        val status = SiteSecurityStatus.fromUrl("https://example.com")

        assertEquals(SiteSecurityStatus.SECURE, status)
        assertEquals("HTTPS", status.protocolDisplayName())
        assertEquals(true, status.isEncryptedConnection())
    }

    /**
     * 测试函数 `fromUrl_marksHttpNotSecure`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `from Url marks Http Not Secure` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fromUrl_marksHttpNotSecure() {
        val status = SiteSecurityStatus.fromUrl("http://example.com")

        assertEquals(SiteSecurityStatus.NOT_SECURE, status)
        assertEquals("HTTP", status.protocolDisplayName())
        assertEquals(false, status.isEncryptedConnection())
    }

    /**
     * 测试函数 `fromUrl_ignoresNonWebAndBlankUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `from Url ignores Non Web And Blank Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fromUrl_ignoresNonWebAndBlankUrls() {
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl("about:blank"))
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl("mailto:a@example.com"))
        assertEquals(SiteSecurityStatus.UNKNOWN, SiteSecurityStatus.fromUrl(null))
        assertEquals("未知", SiteSecurityStatus.UNKNOWN.protocolDisplayName())
        assertEquals(false, SiteSecurityStatus.UNKNOWN.isEncryptedConnection())
    }
}
