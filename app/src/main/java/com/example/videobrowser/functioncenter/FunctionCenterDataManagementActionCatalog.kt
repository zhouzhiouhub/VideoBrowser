package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterDataManagementActionCatalog 可以拆开理解为“Function Center Data Management Action Catalog”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
enum class FunctionCenterDataManagementAction {
    AD_BLOCK_LOG,
    USER_WHITELIST,
    USER_MANUAL_RULES,
    SITE_PERMISSIONS,
    RULE_SUBSCRIPTIONS,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    COOKIES,
    CACHE,
    SITE_DATA,
    RESTORE_DEFAULT_SETTINGS
}

object FunctionCenterDataManagementActionCatalog {
    fun actions(isPrivateBrowsing: Boolean): List<FunctionCenterDataManagementAction> {
        return listOfNotNull(
            FunctionCenterDataManagementAction.AD_BLOCK_LOG.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.USER_WHITELIST.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.USER_MANUAL_RULES.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.SITE_PERMISSIONS.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.RULE_SUBSCRIPTIONS.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.BOOKMARKS.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.HISTORY.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.DOWNLOADS.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.COOKIES.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.CACHE,
            FunctionCenterDataManagementAction.SITE_DATA.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.RESTORE_DEFAULT_SETTINGS
        )
    }

    fun profileActions(): List<FunctionCenterDataManagementAction> {
        return listOf(FunctionCenterDataManagementAction.RESTORE_DEFAULT_SETTINGS)
    }
}
