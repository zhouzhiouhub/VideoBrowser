package com.example.videobrowser.functioncenter

enum class FunctionCenterDataManagementAction {
    AD_BLOCK_LOG,
    USER_WHITELIST,
    USER_MANUAL_RULES,
    RESTORE_DEFAULT_SETTINGS
}

object FunctionCenterDataManagementActionCatalog {
    fun actions(isPrivateBrowsing: Boolean): List<FunctionCenterDataManagementAction> {
        return listOfNotNull(
            FunctionCenterDataManagementAction.AD_BLOCK_LOG.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.USER_WHITELIST.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.USER_MANUAL_RULES.takeIf { !isPrivateBrowsing },
            FunctionCenterDataManagementAction.RESTORE_DEFAULT_SETTINGS
        )
    }
}
