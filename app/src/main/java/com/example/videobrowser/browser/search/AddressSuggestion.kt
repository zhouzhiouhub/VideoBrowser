package com.example.videobrowser.browser.search

/**
 * 初学者阅读提示：
 * 这个文件属于“搜索入口与地址建议模块”。
 * 文件名 AddressSuggestion 可以拆开理解为“Address Suggestion”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：把地址栏输入、默认搜索引擎、远程搜索建议、收藏和历史候选项整理成用户可以点击的建议列表。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
sealed class AddressSuggestion {
    data class Bookmark(
        val title: String,
        val url: String,
        val displayUrl: String
    ) : AddressSuggestion()

    data class History(
        val title: String,
        val url: String,
        val displayUrl: String
    ) : AddressSuggestion()

    data class Remote(
        val keyword: String
    ) : AddressSuggestion()

    data class Fallback(
        val keyword: String
    ) : AddressSuggestion()
}
