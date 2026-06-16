package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterProfileActionCatalog 可以拆开理解为“Function Center Profile Action Catalog”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
enum class FunctionCenterProfileAction {
    HISTORY,
    PLAYBACK_HISTORY,
    BOOKMARKS,
    DOWNLOADS,
    FILE_OPERATIONS,
    USER_MANUAL_RULES,
    ABOUT
}

object FunctionCenterProfileActionCatalog {
    fun shortcuts(isPrivateBrowsing: Boolean): List<FunctionCenterProfileAction> {
        return listOfNotNull(
            FunctionCenterProfileAction.HISTORY,
            FunctionCenterProfileAction.PLAYBACK_HISTORY,
            FunctionCenterProfileAction.BOOKMARKS,
            FunctionCenterProfileAction.DOWNLOADS,
            FunctionCenterProfileAction.FILE_OPERATIONS,
            FunctionCenterProfileAction.USER_MANUAL_RULES.takeIf { !isPrivateBrowsing },
            FunctionCenterProfileAction.ABOUT
        )
    }
}
