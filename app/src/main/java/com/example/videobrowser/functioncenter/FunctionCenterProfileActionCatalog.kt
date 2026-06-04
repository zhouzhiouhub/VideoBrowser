package com.example.videobrowser.functioncenter

enum class FunctionCenterProfileAction {
    HISTORY,
    BOOKMARKS,
    FILE_OPERATIONS,
    BROWSER_SETTINGS
}

object FunctionCenterProfileActionCatalog {
    fun shortcuts(): List<FunctionCenterProfileAction> {
        return listOf(
            FunctionCenterProfileAction.HISTORY,
            FunctionCenterProfileAction.BOOKMARKS,
            FunctionCenterProfileAction.FILE_OPERATIONS,
            FunctionCenterProfileAction.BROWSER_SETTINGS
        )
    }
}
