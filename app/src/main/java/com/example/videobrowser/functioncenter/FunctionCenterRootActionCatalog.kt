package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterRootActionCatalog 可以拆开理解为“Function Center Root Action Catalog”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
enum class FunctionCenterRootAction {
    TABS,
    HOME,
    SHARE_PAGE,
    SAVE_PAGE_ARCHIVE,
    PRINT_PAGE,
    BOOKMARKS,
    HISTORY,
    PLAYBACK_HISTORY,
    DOWNLOADS,
    FILE_OPERATIONS,
    REFRESH,
    DESKTOP_MODE,
    ADD_BOOKMARK,
    PICK_ELEMENT,
    MORE
}

object FunctionCenterRootActionCatalog {
    fun actions(
        hasPage: Boolean,
        hasSite: Boolean,
        isPrivateBrowsing: Boolean
    ): List<FunctionCenterRootAction> {
        return listOfNotNull(
            FunctionCenterRootAction.TABS,
            FunctionCenterRootAction.HOME.takeIf { hasPage },
            FunctionCenterRootAction.SHARE_PAGE.takeIf { hasPage },
            FunctionCenterRootAction.SAVE_PAGE_ARCHIVE.takeIf { hasPage },
            FunctionCenterRootAction.PRINT_PAGE.takeIf { hasPage },
            FunctionCenterRootAction.REFRESH.takeIf { hasPage },
            FunctionCenterRootAction.DESKTOP_MODE.takeIf { hasPage && !isPrivateBrowsing },
            FunctionCenterRootAction.ADD_BOOKMARK.takeIf { hasPage },
            FunctionCenterRootAction.PICK_ELEMENT.takeIf { hasPage && hasSite && !isPrivateBrowsing },
            FunctionCenterRootAction.MORE.takeIf { hasPage && !isPrivateBrowsing && hasSite }
        )
    }
}
