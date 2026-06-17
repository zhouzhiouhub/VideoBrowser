package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Url State Controller”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserUrlStateControllerTest {
    /**
     * 测试函数 `currentActionableUrlPrefersCurrentPageUrl`：验证会话 URL 和 WebView URL 都可用时，优先使用会话 URL。
     */
    @Test
    fun currentActionableUrlPrefersCurrentPageUrl() {
        val controller = controller(
            pageUrl = "https://page.example.com/watch",
            webViewUrl = "https://webview.example.com/watch"
        )

        assertEquals("https://page.example.com/watch", controller.currentActionableUrl())
        assertEquals("https://page.example.com/watch", controller.currentShareableUrl())
    }

    /**
     * 测试函数 `currentActionableUrlFallsBackToWebViewUrl`：验证会话 URL 不适合分享时，会回退到 WebView URL。
     */
    @Test
    fun currentActionableUrlFallsBackToWebViewUrl() {
        val controller = controller(
            pageUrl = "about:blank",
            webViewUrl = "https://webview.example.com/watch"
        )

        assertEquals("https://webview.example.com/watch", controller.currentActionableUrl())
    }

    /**
     * 测试函数 `currentSiteHostUsesCurrentPageUrl`：验证站点 host 来自当前会话 URL。
     */
    @Test
    fun currentSiteHostUsesCurrentPageUrl() {
        val controller = controller(
            pageUrl = "https://Video.Example.COM:443/watch",
            webViewUrl = "https://fallback.example.com"
        )

        assertEquals("video.example.com", controller.currentSiteHost())
    }

    /**
     * 测试函数 `isShareableUrlOnlyAcceptsHttpAndHttps`：验证只有 http 和 https 地址可以作为普通网页地址处理。
     */
    @Test
    fun isShareableUrlOnlyAcceptsHttpAndHttps() {
        val controller = controller(pageUrl = null, webViewUrl = null)

        assertTrue(controller.isShareableUrl("https://example.com"))
        assertTrue(controller.isShareableUrl("http://example.com"))
        assertFalse(controller.isShareableUrl("about:blank"))
        assertFalse(controller.isShareableUrl("file:///tmp/video.mp4"))
        assertNull(controller.currentActionableUrl())
    }

    /**
     * 创建被测 URL 状态控制器。
     *
     * @param pageUrl 当前会话记录的页面 URL。
     * @param webViewUrl 当前 WebView 实际显示的 URL。
     * @return 返回可用于断言的 BrowserUrlStateController。
     */
    private fun controller(pageUrl: String?, webViewUrl: String?): BrowserUrlStateController {
        return BrowserUrlStateController(
            currentPageUrl = { pageUrl },
            currentWebViewUrl = { webViewUrl }
        )
    }
}
