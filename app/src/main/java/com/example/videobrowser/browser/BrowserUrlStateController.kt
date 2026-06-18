package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserUrlStateController 可以拆开理解为“Browser Url State Controller”，
 * 表示它只负责根据当前浏览器状态计算 URL 相关信息。
 * 主要职责：取得当前可分享 URL、当前可操作 URL、当前站点 host，并判断 URL 是否适合分享。
 * 阅读顺序：先看构造参数了解 URL 从哪里来，再看 currentActionableUrl() 的筛选规则。
 */
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.WebSchemePolicy

/**
 * 浏览器 URL 状态控制器。
 *
 * MainActivity 仍负责知道“当前会话”和“当前 WebView”是什么；这个控制器只负责把这些 URL
 * 规整成页面操作、分享和站点设置需要的数据。
 *
 * @param currentPageUrl 返回当前会话记录的页面 URL 的函数，通常来自 BrowserSessionController。
 * @param currentWebViewUrl 返回当前 WebView 实际 URL 的函数，用于补充会话状态尚未同步时的地址。
 */
class BrowserUrlStateController(
    private val currentPageUrl: () -> String?,
    private val currentWebViewUrl: () -> String?
) {
    /**
     * 取得当前可分享 URL。
     *
     * @return 当前最适合分享的 http 或 https URL；没有可分享地址时返回 null。
     */
    fun currentShareableUrl(): String? {
        return currentActionableUrl()
    }

    /**
     * 取得当前可执行页面操作的 URL。
     *
     * @return 优先返回当前会话 URL，其次返回 WebView URL；只接受非空的 http 或 https URL。
     */
    fun currentActionableUrl(): String? {
        return listOf(currentPageUrl(), currentWebViewUrl())
            .firstOrNull { url -> !url.isNullOrBlank() && isShareableUrl(url) }
    }

    /**
     * 取得当前页面的站点 host。
     *
     * @return 当前会话 URL 解析出的 host；当前没有页面 URL 或 URL 无法解析时返回 null。
     */
    fun currentSiteHost(): String? {
        return SiteHost.fromUrl(currentPageUrl())
    }

    /**
     * 判断 URL 是否适合执行分享、复制、收藏等页面动作。
     *
     * @param url 要检查的 URL 文本，通常来自当前会话或 WebView。
     * @return true 表示 URL 使用 http 或 https 协议，可以作为普通网页地址处理。
     */
    fun isShareableUrl(url: String): Boolean {
        return WebSchemePolicy.isHttpOrHttpsScheme(SafeUriParser.scheme(url))
    }
}
