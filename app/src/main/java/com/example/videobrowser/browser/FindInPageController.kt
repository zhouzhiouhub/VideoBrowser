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

    /**
     * 函数 `search`：封装 `search` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param query 参数类型为 `String`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun search(query: String): Boolean {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            return false
        }
        currentQuery = normalizedQuery
        findAll(normalizedQuery)
        return true
    }

    /**
     * 函数 `findNext`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param forward 参数类型为 `Boolean`，表示函数执行 `forward` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun findNext(forward: Boolean = true): Boolean {
        if (currentQuery.isNullOrBlank()) {
            return false
        }
        moveToMatch(forward)
        return true
    }

    /**
     * 函数 `findPrevious`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun findPrevious(): Boolean {
        return findNext(forward = false)
    }

    /**
     * 函数 `clear`：封装 `clear` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clear() {
        currentQuery = null
        clearMatches()
    }
}
