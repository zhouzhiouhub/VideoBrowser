package com.example.videobrowser.functioncenter

enum class FunctionCenterProfileAction {
    HISTORY,
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
            FunctionCenterProfileAction.BOOKMARKS,
            FunctionCenterProfileAction.DOWNLOADS,
            FunctionCenterProfileAction.FILE_OPERATIONS,
            FunctionCenterProfileAction.USER_MANUAL_RULES.takeIf { !isPrivateBrowsing },
            FunctionCenterProfileAction.ABOUT
        )
    }
}
