package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 HistoryRecordPolicy 可以拆开理解为“History Record Policy”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import java.net.URI
import java.util.Locale

class HistoryRecordPolicy(
    private val homeUrls: () -> List<String>
) {
    fun shouldRecord(url: String?): Boolean {
        val currentUrl = WebUrl.from(url) ?: return false
        return homeUrls()
            .mapNotNull(WebUrl::from)
            .none { homeUrl -> homeUrl.isSamePageAs(currentUrl) }
    }

    private data class WebUrl(
        val scheme: String,
        val host: String,
        val port: Int,
        val path: String
    ) {
        fun isSamePageAs(other: WebUrl): Boolean {
            return scheme == other.scheme &&
                host == other.host &&
                port == other.port &&
                path == other.path
        }

        companion object {
            fun from(value: String?): WebUrl? {
                val uri = try {
                    URI(value?.trim().orEmpty())
                } catch (_: IllegalArgumentException) {
                    return null
                }
                val scheme = uri.scheme?.lowercase(Locale.US) ?: return null
                if (scheme != "http" && scheme != "https") {
                    return null
                }
                val host = uri.host
                    ?.trimEnd('.')
                    ?.lowercase(Locale.US)
                    ?.takeIf { it.isNotBlank() }
                    ?: return null
                return WebUrl(
                    scheme = scheme,
                    host = host,
                    port = normalizedPort(scheme, uri.port),
                    path = uri.rawPath.orEmpty().trim('/')
                )
            }

            private fun normalizedPort(scheme: String, port: Int): Int {
                if (port >= 0) {
                    return port
                }
                return when (scheme) {
                    "http" -> 80
                    "https" -> 443
                    else -> -1
                }
            }
        }
    }
}
