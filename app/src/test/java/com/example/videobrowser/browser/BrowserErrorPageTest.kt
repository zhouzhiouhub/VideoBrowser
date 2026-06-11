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
}
