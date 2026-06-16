package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 FindInPageController 可以拆开理解为“Find In Page Controller”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
class FindInPageController(
    findAll: (String) -> Unit,
    findNext: (Boolean) -> Unit,
    clearMatches: () -> Unit
) {
    private val findAll = findAll
    private val moveToMatch = findNext
    private val clearMatches = clearMatches

    var currentQuery: String? = null
        private set

    fun search(query: String): Boolean {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            return false
        }
        currentQuery = normalizedQuery
        findAll(normalizedQuery)
        return true
    }

    fun findNext(forward: Boolean = true): Boolean {
        if (currentQuery.isNullOrBlank()) {
            return false
        }
        moveToMatch(forward)
        return true
    }

    fun findPrevious(): Boolean {
        return findNext(forward = false)
    }

    fun clear() {
        currentQuery = null
        clearMatches()
    }
}
