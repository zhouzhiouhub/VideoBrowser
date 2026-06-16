package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 RuntimePrivateBrowsingState 可以拆开理解为“Runtime Private Browsing State”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
class RuntimePrivateBrowsingState(
    private val onPrivateCleanup: () -> Unit = {}
) {
    var mode: BrowserMode = BrowserMode.STANDARD
        private set

    val isPrivate: Boolean
        get() = mode == BrowserMode.PRIVATE

    /**
     * 函数 `enterPrivate`：封装 `enter Private` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun enterPrivate(): Boolean {
        if (isPrivate) {
            return false
        }
        mode = BrowserMode.PRIVATE
        return true
    }

    /**
     * 函数 `exitPrivate`：封装 `exit Private` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun exitPrivate(): Boolean {
        if (!isPrivate) {
            return false
        }
        onPrivateCleanup()
        mode = BrowserMode.STANDARD
        return true
    }

    /**
     * 函数 `resetToStandard`：封装 `reset To Standard` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun resetToStandard() {
        if (isPrivate) {
            onPrivateCleanup()
        }
        mode = BrowserMode.STANDARD
    }
}
