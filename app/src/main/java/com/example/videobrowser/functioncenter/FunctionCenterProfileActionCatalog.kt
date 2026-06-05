package com.example.videobrowser.functioncenter

enum class FunctionCenterProfileAction {
    HISTORY,
    BOOKMARKS,
    DOWNLOADS,
    FILE_OPERATIONS
}

object FunctionCenterProfileActionCatalog {
    fun shortcuts(): List<FunctionCenterProfileAction> {
        return listOf(
            FunctionCenterProfileAction.HISTORY,
            FunctionCenterProfileAction.BOOKMARKS,
            FunctionCenterProfileAction.DOWNLOADS,
            FunctionCenterProfileAction.FILE_OPERATIONS
        )
    }
}
