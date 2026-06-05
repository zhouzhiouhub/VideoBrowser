package com.example.videobrowser.functioncenter

enum class FunctionCenterProfileAction {
    HISTORY,
    BOOKMARKS,
    DOWNLOADS,
    FILE_OPERATIONS,
    BROWSER_SETTINGS
}

object FunctionCenterProfileActionCatalog {
    fun shortcuts(): List<FunctionCenterProfileAction> {
        return listOf(
            FunctionCenterProfileAction.HISTORY,
            FunctionCenterProfileAction.BOOKMARKS,
            FunctionCenterProfileAction.DOWNLOADS,
            FunctionCenterProfileAction.FILE_OPERATIONS,
            FunctionCenterProfileAction.BROWSER_SETTINGS
        )
    }
}
