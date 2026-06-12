package com.example.videobrowser.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserErrorPageTest {
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
