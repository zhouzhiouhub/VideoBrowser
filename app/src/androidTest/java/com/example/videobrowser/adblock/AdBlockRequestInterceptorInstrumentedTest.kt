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
    /**
     * 测试函数 `intercept_returnsEmptyResponseForMatchingAdUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `intercept returns Empty Response For Matching Ad Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `intercept_returnsNoopJsResponseForSupportedRedirectRule`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `intercept returns Noop Js Response For Supported Redirect Rule` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `intercept_doesNotRedirectMainFrameRequests`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `intercept does Not Redirect Main Frame Requests` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
