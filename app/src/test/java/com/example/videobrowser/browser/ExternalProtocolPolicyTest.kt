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
    /**
     * 测试函数 `shouldOpenExternally_allowsAppLinkSchemes`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Open Externally allows App Link Schemes` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun shouldOpenExternally_allowsAppLinkSchemes() {
        assertTrue(ExternalProtocolPolicy.shouldOpenExternally("mailto"))
        assertTrue(ExternalProtocolPolicy.shouldOpenExternally("tel"))
        assertTrue(ExternalProtocolPolicy.shouldOpenExternally("intent"))
        assertTrue(ExternalProtocolPolicy.shouldOpenExternally("custom-app"))
    }

    /**
     * 测试函数 `shouldOpenExternally_blocksWebAndBrowserInternalSchemes`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `should Open Externally blocks Web And Browser Internal Schemes` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
    fun shouldOpenUrlExternally_extractsSchemeWithSharedParser() {
        assertTrue(ExternalProtocolPolicy.shouldOpenUrlExternally("intent://scan/#Intent;end"))
        assertTrue(ExternalProtocolPolicy.shouldOpenUrlExternally(" custom-app://open "))

        assertFalse(ExternalProtocolPolicy.shouldOpenUrlExternally("https://example.com"))
        assertFalse(ExternalProtocolPolicy.shouldOpenUrlExternally("not a url"))
        assertFalse(ExternalProtocolPolicy.shouldOpenUrlExternally(null))
    }

    @Test
    fun isIntentUrl_matchesIntentSchemeOnly() {
        assertTrue(ExternalProtocolPolicy.isIntentUrl("intent://scan/#Intent;end"))

        assertFalse(ExternalProtocolPolicy.isIntentUrl("custom-app://open"))
        assertFalse(ExternalProtocolPolicy.isIntentUrl("https://example.com"))
    }

    /**
     * 测试函数 `isWebUrl_requiresHttpOrHttpsUrlWithHost`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `is Web Url requires Http Or Https Url With Host` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `fallbackUrlFromIntentUri_extractsWebFallback`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fallback Url From Intent Uri extracts Web Fallback` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `fallbackUrlFromIntentUri_rejectsNonWebFallback`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fallback Url From Intent Uri rejects Non Web Fallback` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fallbackUrlFromIntentUri_rejectsNonWebFallback() {
        val fallback = ExternalProtocolPolicy.fallbackUrlFromIntentUri(
            "intent://scan/#Intent;S.browser_fallback_url=javascript%3Aalert(1);end"
        )

        assertNull(fallback)
    }

    /**
     * 测试函数 `fallbackUrlFromIntentUri_rejectsWebFallbackWithoutHost`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fallback Url From Intent Uri rejects Web Fallback Without Host` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fallbackUrlFromIntentUri_rejectsWebFallbackWithoutHost() {
        val fallback = ExternalProtocolPolicy.fallbackUrlFromIntentUri(
            "intent://scan/#Intent;S.browser_fallback_url=https%3A%2Fmissing-host;end"
        )

        assertNull(fallback)
    }
}
