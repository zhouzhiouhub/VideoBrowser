package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BottomBarButtonVisibility 可以拆开理解为“Bottom Bar Button Visibility”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
data class BottomBarButtonVisibility(
    val showBack: Boolean,
    val enableBack: Boolean = showBack,
    val showPageTools: Boolean,
    val showRefresh: Boolean = true,
    val showWenxin: Boolean = true,
    val showProfile: Boolean = true
) {
    companion object {
        /**
         * 函数 `forPageState`：封装 `for Page State` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param isHomePageVisible 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun forPageState(isHomePageVisible: Boolean): BottomBarButtonVisibility {
            return BottomBarButtonVisibility(
                showBack = true,
                enableBack = !isHomePageVisible,
                showPageTools = true
            )
        }
    }
}
