package com.example.videobrowser.storage

/**
 * 初学者阅读提示：
 * 这个文件属于“收藏与历史存储模块”。
 * 文件名 SavedPageSearch 可以拆开理解为“Saved Page Search”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：读写收藏夹、浏览历史、导入导出数据，并提供搜索和过滤能力。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.utils.SearchQueryTerms

object SavedPageSearch {
    /**
     * 函数 `filter`：封装 `filter` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pages 参数类型为 `List<SavedPage>`，表示函数执行 `pages` 相关逻辑时需要读取或处理的输入。
     * @param query 参数类型为 `String?`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun filter(pages: List<SavedPage>, query: String?): List<SavedPage> {
        val terms = SearchQueryTerms.parse(query)
        if (terms.isEmpty()) {
            return pages
        }

        return pages.filter { page ->
            val haystack = "${page.title}\n${page.url}\n${page.folder}"
            SearchQueryTerms.containsAll(haystack, terms)
        }
    }
}
