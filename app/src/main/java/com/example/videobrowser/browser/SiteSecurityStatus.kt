package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 SiteSecurityStatus 可以拆开理解为“Site Security Status”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import java.util.Locale

enum class SiteSecurityStatus {
    SECURE,
    NOT_SECURE,
    UNKNOWN;

    fun protocolDisplayName(): String {
        return when (this) {
            SECURE -> "HTTPS"
            NOT_SECURE -> "HTTP"
            UNKNOWN -> "未知"
        }
    }

    fun isEncryptedConnection(): Boolean {
        return this == SECURE
    }

    companion object {
        fun fromUrl(url: String?): SiteSecurityStatus {
            val scheme = url
                ?.substringBefore(':', missingDelimiterValue = "")
                ?.trim()
                ?.lowercase(Locale.ROOT)
                .orEmpty()
            return when (scheme) {
                "https" -> SECURE
                "http" -> NOT_SECURE
                else -> UNKNOWN
            }
        }
    }
}
