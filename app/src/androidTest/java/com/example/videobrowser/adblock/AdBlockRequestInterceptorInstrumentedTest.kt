package com.example.videobrowser.adblock

/**
 * 测试阅读提示：
 * 这个测试文件验证“Ad Block Request Interceptor Instrumented Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import android.net.Uri
import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AdBlockRequestInterceptorInstrumentedTest {
    @Test
    fun intercept_returnsEmptyResponseForMatchingAdUrl() {
        val interceptor = AdBlockRequestInterceptor(AdBlockManager())
        val request = BrowserRequest.from(
            uri = Uri.parse("https://stats.g.doubleclick.net/pagead/banner.js"),
            isForMainFrame = false
        )

        val response = interceptor.intercept(request)

        assertNotNull(response)
        requireNotNull(response)
        assertEquals(204, response.statusCode)
        assertEquals("No Content", response.reasonPhrase)
        assertEquals("text/plain", response.mimeType)
        assertEquals("utf-8", response.encoding)
        assertEquals(-1, response.data.read())
    }

    @Test
    fun intercept_returnsNoopJsResponseForSupportedRedirectRule() {
        val ruleEngine = RuleEngine(
            listOf(requireNotNull(Rule.fromRequestRuleText("||ads.example.com^\$redirect=noopjs")))
        )
        val interceptor = AdBlockRequestInterceptor(
            AdBlockManager(ruleEngine = ruleEngine)
        )
        val request = BrowserRequest.from(
            uri = Uri.parse("https://ads.example.com/script.js"),
            isForMainFrame = false
        )

        val response = interceptor.intercept(request)

        assertNotNull(response)
        requireNotNull(response)
        assertEquals(200, response.statusCode)
        assertEquals("OK", response.reasonPhrase)
        assertEquals("application/javascript", response.mimeType)
        assertEquals("utf-8", response.encoding)
        val body = response.data.bufferedReader(Charsets.UTF_8).readText()
        assertEquals("/* noop */\n", body)
    }

    @Test
    fun intercept_doesNotRedirectMainFrameRequests() {
        val ruleEngine = RuleEngine(
            listOf(requireNotNull(Rule.fromRequestRuleText("||ads.example.com^\$redirect=noopjs")))
        )
        val interceptor = AdBlockRequestInterceptor(
            AdBlockManager(ruleEngine = ruleEngine)
        )
        val request = BrowserRequest.from(
            uri = Uri.parse("https://ads.example.com/"),
            isForMainFrame = true
        )

        assertNull(interceptor.intercept(request))
    }
}
