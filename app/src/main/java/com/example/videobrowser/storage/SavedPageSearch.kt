package com.example.videobrowser.storage

/**
 * 初学者阅读提示：
 * 这个文件属于“收藏与历史存储模块”。
 * 文件名 SavedPageSearch 可以拆开理解为“Saved Page Search”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：读写收藏夹、浏览历史、导入导出数据，并提供搜索和过滤能力。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import java.util.Locale

object SavedPageSearch {
    fun filter(pages: List<SavedPage>, query: String?): List<SavedPage> {
        val terms = query
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.split(Regex("\\s+"))
            ?.filter { term -> term.isNotBlank() }
            ?: emptyList()
        if (terms.isEmpty()) {
            return pages
        }

        return pages.filter { page ->
            val haystack = "${page.title}\n${page.url}\n${page.folder}".lowercase(Locale.ROOT)
            terms.all { term -> haystack.contains(term) }
        }
    }
}
