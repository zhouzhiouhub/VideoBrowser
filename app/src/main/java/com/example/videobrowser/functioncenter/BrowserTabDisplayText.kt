package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 BrowserTabDisplayText 可以拆开理解为“Browser Tab Display Text”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.browser.BrowserTab
import com.example.videobrowser.utils.UrlUtils
import java.net.URI

object BrowserTabDisplayText {
    fun title(tab: BrowserTab, untitledText: String): String {
        return tab.title
            .takeIf { it.isNotBlank() }
            ?: tab.url?.let(::compactUrlTitle)?.takeIf { it.isNotBlank() }
            ?: untitledText
    }

    private fun compactUrlTitle(url: String): String {
        val parsed = runCatching { URI(url.trim()) }.getOrNull()
        return parsed?.rawAuthority
            ?.takeIf { it.isNotBlank() }
            ?: UrlUtils.displayUrl(url)
    }
}
