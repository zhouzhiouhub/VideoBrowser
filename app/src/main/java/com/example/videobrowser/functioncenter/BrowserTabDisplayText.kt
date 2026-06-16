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
    /**
     * 函数 `title`：封装 `title` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tab 参数类型为 `BrowserTab`，表示函数执行 `tab` 相关逻辑时需要读取或处理的输入。
     * @param untitledText 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun title(tab: BrowserTab, untitledText: String): String {
        return tab.title
            .takeIf { it.isNotBlank() }
            ?: tab.url?.let(::compactUrlTitle)?.takeIf { it.isNotBlank() }
            ?: untitledText
    }

    /**
     * 函数 `compactUrlTitle`：封装 `compact Url Title` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun compactUrlTitle(url: String): String {
        val parsed = runCatching { URI(url.trim()) }.getOrNull()
        return parsed?.rawAuthority
            ?.takeIf { it.isNotBlank() }
            ?: UrlUtils.displayUrl(url)
    }
}
