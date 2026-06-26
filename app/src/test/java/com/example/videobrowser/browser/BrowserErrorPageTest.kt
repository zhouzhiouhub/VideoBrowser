package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Error Page Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserErrorPageTest {
    /**
     * 测试函数 `rendersEscapedNetworkErrorDetails`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `renders Escaped Network Error Details` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun rendersEscapedNetworkErrorDetails() {
        val html = BrowserErrorPage.render(
            BrowserPageError.Network(
                url = "https://example.com/?q=<script>",
                code = -2,
                description = "Host <not found>"
            )
        )

        assertTrue(html.contains("网页无法打开"))
        assertTrue(html.contains("https://example.com/?q=&lt;script&gt;"))
        assertTrue(html.contains("href=\"https://example.com/?q=&lt;script&gt;\""))
        assertTrue(html.contains("重试"))
        assertTrue(html.contains("Host &lt;not found&gt;"))
        assertFalse(html.contains("<script>"))
    }

    /**
     * 测试函数 `rendersSslErrorsAsBlockedConnections`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `renders Ssl Errors As Blocked Connections` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun rendersSslErrorsAsBlockedConnections() {
        val html = BrowserErrorPage.render(
            BrowserPageError.Ssl(
                url = "https://expired.example",
                description = "SSL 证书错误"
            )
        )

        assertTrue(html.contains("连接已被阻止"))
        assertTrue(html.contains("SSL 证书错误"))
        assertTrue(html.contains("https://expired.example"))
    }

    @Test
    fun rendersHttpDiagnosticsForUnauthorizedErrors() {
        val html = BrowserErrorPage.render(
            BrowserPageError.Http(
                url = "https://target.example.com/private?token=<secret>",
                statusCode = 401,
                reasonPhrase = "Unauthorized",
                diagnostics = BrowserHttpErrorDiagnostics(
                    finalUrl = "https://target.example.com/private?token=<secret>",
                    currentPageUrl = "https://m.baidu.com/s?word=target",
                    userAgent = "VideoBrowser <UA>",
                    isSearchResultPage = true
                )
            )
        )

        assertTrue(html.contains("HTTP 401 Unauthorized"))
        assertTrue(html.contains("不是地址无法解析"))
        assertTrue(html.contains("诊断信息"))
        assertTrue(html.contains("最终 URL"))
        assertTrue(html.contains("https://target.example.com/private?token=&lt;secret&gt;"))
        assertTrue(html.contains("当前页面"))
        assertTrue(html.contains("https://m.baidu.com/s?word=target"))
        assertTrue(html.contains("User-Agent"))
        assertTrue(html.contains("VideoBrowser &lt;UA&gt;"))
        assertTrue(html.contains("内置搜索结果页"))
        assertFalse(html.contains("<secret>"))
        assertFalse(html.contains("VideoBrowser <UA>"))
    }

    /**
     * 测试函数 `rendersSafeBrowsingErrorsAsBlockedConnectionsWithoutRetryLink`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `renders Safe Browsing Errors As Blocked Connections Without Retry Link` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun rendersSafeBrowsingErrorsAsBlockedConnectionsWithoutRetryLink() {
        val html = BrowserErrorPage.render(
            BrowserPageError.SafeBrowsing(
                url = "https://malware.example",
                threatType = 1,
                description = "Safe Browsing 已阻止恶意软件风险。"
            )
        )

        assertTrue(html.contains("连接已被阻止"))
        assertTrue(html.contains("Safe Browsing 已阻止恶意软件风险。"))
        assertTrue(html.contains("https://malware.example"))
        assertFalse(html.contains("href="))
        assertFalse(html.contains("重试"))
    }

    /**
     * 测试函数 `rendersRendererCrashesAsRetryablePageCrashes`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `renders Renderer Crashes As Retryable Page Crashes` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun rendersRendererCrashesAsRetryablePageCrashes() {
        val html = BrowserErrorPage.render(
            BrowserPageError.RenderProcessGone(
                url = "https://video.example.com/watch",
                didCrash = true
            )
        )

        assertTrue(html.contains("网页已崩溃"))
        assertTrue(html.contains("网页渲染进程已崩溃"))
        assertTrue(html.contains("https://video.example.com/watch"))
        assertTrue(html.contains("重试"))
    }

    /**
     * 测试函数 `omitsRetryLinkWhenUrlIsMissing`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `omits Retry Link When Url Is Missing` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun omitsRetryLinkWhenUrlIsMissing() {
        val html = BrowserErrorPage.render(
            BrowserPageError.Network(
                url = null,
                code = -6,
                description = "连接失败"
            )
        )

        assertFalse(html.contains("href="))
    }

    /**
     * 测试函数 `omitsRetryLinkForNonWebUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `omits Retry Link For Non Web Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun omitsRetryLinkForNonWebUrls() {
        val html = BrowserErrorPage.render(
            BrowserPageError.Network(
                url = "javascript:alert(1)",
                code = -10,
                description = "不支持的地址"
            )
        )

        assertTrue(html.contains("javascript:alert(1)"))
        assertFalse(html.contains("href="))
    }
}
