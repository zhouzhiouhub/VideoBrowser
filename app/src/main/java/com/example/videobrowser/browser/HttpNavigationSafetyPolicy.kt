package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 HttpNavigationSafetyPolicy 可以拆开理解为“Http Navigation Safety Policy”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import java.net.URI
import java.util.Locale

object HttpNavigationSafetyPolicy {
    fun requiresInsecureNavigationConfirmation(pageUrl: String?, targetUrl: String): Boolean {
        return schemeOf(pageUrl) == "https" && isHttpNetworkUrl(targetUrl)
    }

    private fun isHttpNetworkUrl(url: String): Boolean {
        val uri = uriOf(url) ?: return false
        return uri.scheme?.lowercase(Locale.ROOT) == "http" && !uri.host.isNullOrBlank()
    }

    private fun schemeOf(url: String?): String? {
        return uriOf(url)
            ?.scheme
            ?.lowercase(Locale.ROOT)
    }

    private fun uriOf(url: String?): URI? {
        return runCatching { URI(url?.trim().orEmpty()) }.getOrNull()
    }
}
