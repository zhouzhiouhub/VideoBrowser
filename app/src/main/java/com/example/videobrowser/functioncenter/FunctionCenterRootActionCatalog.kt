package com.example.videobrowser.functioncenter

enum class FunctionCenterRootAction {
    SHARE_PAGE,
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
            FunctionCenterRootAction.SHARE_PAGE,
            FunctionCenterRootAction.REFRESH,
            FunctionCenterRootAction.DESKTOP_MODE.takeIf { hasPage && !isPrivateBrowsing },
            FunctionCenterRootAction.ADD_BOOKMARK,
            FunctionCenterRootAction.PICK_ELEMENT.takeIf { hasPage && hasSite && !isPrivateBrowsing },
            FunctionCenterRootAction.BOOKMARKS,
            FunctionCenterRootAction.HISTORY,
            FunctionCenterRootAction.PLAYBACK_HISTORY,
            FunctionCenterRootAction.DOWNLOADS,
            FunctionCenterRootAction.FILE_OPERATIONS,
            FunctionCenterRootAction.MORE.takeIf { !isPrivateBrowsing && hasSite }
        )
    }
}
