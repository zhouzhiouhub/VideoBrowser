package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“External Protocol Policy Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalProtocolPolicyTest {
    @Test
    fun shouldOpenExternally_allowsAppLinkSchemes() {
        assertTrue(ExternalProtocolPolicy.shouldOpenExternally("mailto"))
        assertTrue(ExternalProtocolPolicy.shouldOpenExternally("tel"))
        assertTrue(ExternalProtocolPolicy.shouldOpenExternally("intent"))
        assertTrue(ExternalProtocolPolicy.shouldOpenExternally("custom-app"))
    }

    @Test
    fun shouldOpenExternally_blocksWebAndBrowserInternalSchemes() {
        assertFalse(ExternalProtocolPolicy.shouldOpenExternally("http"))
        assertFalse(ExternalProtocolPolicy.shouldOpenExternally("https"))
        assertFalse(ExternalProtocolPolicy.shouldOpenExternally("about"))
        assertFalse(ExternalProtocolPolicy.shouldOpenExternally("javascript"))
        assertFalse(ExternalProtocolPolicy.shouldOpenExternally("data"))
        assertFalse(ExternalProtocolPolicy.shouldOpenExternally("file"))
        assertFalse(ExternalProtocolPolicy.shouldOpenExternally(null))
    }

    @Test
    fun isWebUrl_requiresHttpOrHttpsUrlWithHost() {
        assertTrue(ExternalProtocolPolicy.isWebUrl("https://example.com/page"))
        assertTrue(ExternalProtocolPolicy.isWebUrl(" http://example.com/page "))

        assertFalse(ExternalProtocolPolicy.isWebUrl("https:/missing-host"))
        assertFalse(ExternalProtocolPolicy.isWebUrl("http:example.com"))
        assertFalse(ExternalProtocolPolicy.isWebUrl("javascript:alert(1)"))
        assertFalse(ExternalProtocolPolicy.isWebUrl("file:///sdcard/page.html"))
        assertFalse(ExternalProtocolPolicy.isWebUrl(null))
    }

    @Test
    fun fallbackUrlFromIntentUri_extractsWebFallback() {
        val fallback = ExternalProtocolPolicy.fallbackUrlFromIntentUri(
            "intent://scan/#Intent;" +
                "scheme=zxing;" +
                "S.browser_fallback_url=https%3A%2F%2Fexample.com%2Finstall%3Ffrom%3Dscan;" +
                "end"
        )

        assertEquals("https://example.com/install?from=scan", fallback)
    }

    @Test
    fun fallbackUrlFromIntentUri_rejectsNonWebFallback() {
        val fallback = ExternalProtocolPolicy.fallbackUrlFromIntentUri(
            "intent://scan/#Intent;S.browser_fallback_url=javascript%3Aalert(1);end"
        )

        assertNull(fallback)
    }

    @Test
    fun fallbackUrlFromIntentUri_rejectsWebFallbackWithoutHost() {
        val fallback = ExternalProtocolPolicy.fallbackUrlFromIntentUri(
            "intent://scan/#Intent;S.browser_fallback_url=https%3A%2Fmissing-host;end"
        )

        assertNull(fallback)
    }
}
