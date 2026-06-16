package com.example.videobrowser.browser.search

/**
 * 初学者阅读提示：
 * 这个文件属于“搜索入口与地址建议模块”。
 * 文件名 SearchProvider 可以拆开理解为“Search Provider”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：把地址栏输入、默认搜索引擎、远程搜索建议、收藏和历史候选项整理成用户可以点击的建议列表。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.graphics.Color

data class SearchProvider(
    val id: String,
    val name: String,
    val badge: String,
    val homeUrl: String,
    val searchUrlPrefix: String,
    val accentColor: Int
)

object SearchProviders {
    val defaults: List<SearchProvider> = listOf(
        SearchProvider(
            id = "sogou",
            name = "搜狗",
            badge = "搜",
            homeUrl = "https://m.sogou.com/",
            searchUrlPrefix = "https://www.sogou.com/web?query=",
            accentColor = Color.parseColor("#13B56B")
        ),
        SearchProvider(
            id = "so",
            name = "360搜索",
            badge = "360",
            homeUrl = "https://m.so.com/",
            searchUrlPrefix = "https://www.so.com/s?q=",
            accentColor = Color.parseColor("#20A052")
        ),
        SearchProvider(
            id = "quark",
            name = "夸克搜索",
            badge = "夸",
            homeUrl = "https://quark.sm.cn/",
            searchUrlPrefix = "https://quark.sm.cn/s?q=",
            accentColor = Color.parseColor("#2F6FED")
        ),
        SearchProvider(
            id = "uc",
            name = "UC",
            badge = "UC",
            homeUrl = "https://so.m.sm.cn/",
            searchUrlPrefix = "https://so.m.sm.cn/s?q=",
            accentColor = Color.parseColor("#F28C20")
        ),
        SearchProvider(
            id = "baidu",
            name = "百度",
            badge = "百",
            homeUrl = "https://m.baidu.com/",
            searchUrlPrefix = "https://m.baidu.com/s?ie=utf-8&word=",
            accentColor = Color.parseColor("#315EFB")
        ),
        SearchProvider(
            id = "edge",
            name = "Bing",
            badge = "B",
            homeUrl = "https://www.bing.com/",
            searchUrlPrefix = "https://www.bing.com/search?q=",
            accentColor = Color.parseColor("#12837A")
        )
    )
}
