package com.example.videobrowser.functioncenter

enum class FunctionCenterRootAction {
    TABS,
    SHARE_PAGE,
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
            FunctionCenterRootAction.SHARE_PAGE.takeIf { hasPage },
            FunctionCenterRootAction.PRINT_PAGE.takeIf { hasPage },
            FunctionCenterRootAction.REFRESH.takeIf { hasPage },
            FunctionCenterRootAction.DESKTOP_MODE.takeIf { hasPage && !isPrivateBrowsing },
            FunctionCenterRootAction.ADD_BOOKMARK.takeIf { hasPage },
            FunctionCenterRootAction.PICK_ELEMENT.takeIf { hasPage && hasSite && !isPrivateBrowsing },
            FunctionCenterRootAction.MORE.takeIf { hasPage && !isPrivateBrowsing && hasSite }
        )
    }
}
