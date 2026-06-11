package com.example.videobrowser.functioncenter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FunctionCenterRootActionCatalogTest {
    @Test
    fun rootPageActionsPrioritizeCurrentPageActionsBeforeGlobalEntries() {
        val actions = FunctionCenterRootActionCatalog.actions(
            hasPage = true,
            hasSite = true,
            isPrivateBrowsing = false
        )
        val names = actions.map { it.name }

        assertEquals(
            listOf(
                "SHARE_PAGE",
                "REFRESH",
                "ADD_BOOKMARK",
                "PICK_ELEMENT",
                "MORE",
                "BOOKMARKS",
                "HISTORY",
                "PLAYBACK_HISTORY",
                "DOWNLOADS",
                "FILE_OPERATIONS"
            ),
            names
        )
        assertFalse(names.contains("SMART_SUMMARY"))
        assertFalse(names.contains("LISTEN_MODE"))
    }

    @Test
    fun rootPageActionsHideElementPickerWithoutPageOrInPrivateBrowsing() {
        val noPageActions = FunctionCenterRootActionCatalog.actions(
            hasPage = false,
            hasSite = true,
            isPrivateBrowsing = false
        ).map { it.name }
        val privateActions = FunctionCenterRootActionCatalog.actions(
            hasPage = true,
            hasSite = true,
            isPrivateBrowsing = true
        ).map { it.name }

        assertFalse(noPageActions.contains("PICK_ELEMENT"))
        assertFalse(privateActions.contains("PICK_ELEMENT"))
    }
}
