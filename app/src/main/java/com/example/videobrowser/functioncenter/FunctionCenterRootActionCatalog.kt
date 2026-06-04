package com.example.videobrowser.functioncenter

enum class FunctionCenterRootAction {
    SHARE_PAGE,
    BOOKMARKS,
    HISTORY,
    FILE_OPERATIONS,
    REFRESH,
    ADD_BOOKMARK,
    MORE
}

object FunctionCenterRootActionCatalog {
    fun actions(
        hasPage: Boolean,
        hasSite: Boolean,
        isPrivateBrowsing: Boolean
    ): List<FunctionCenterRootAction> {
        return listOfNotNull(
            FunctionCenterRootAction.SHARE_PAGE,
            FunctionCenterRootAction.BOOKMARKS,
            FunctionCenterRootAction.HISTORY,
            FunctionCenterRootAction.FILE_OPERATIONS,
            FunctionCenterRootAction.REFRESH,
            FunctionCenterRootAction.ADD_BOOKMARK,
            FunctionCenterRootAction.MORE.takeIf { !isPrivateBrowsing && hasSite }
        )
    }
}
