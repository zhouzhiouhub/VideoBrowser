package com.example.videobrowser.functioncenter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctionCenterRootActionCatalogTest {
    @Test
    fun rootPageActionsShowOnlyCurrentPageActionsWithinTwoRows() {
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
                "DESKTOP_MODE",
                "ADD_BOOKMARK",
                "PICK_ELEMENT",
                "MORE"
            ),
            names
        )
        assertTrue(FunctionCenterActionGridLayout.rows(actions.size).size <= 2)
        assertFalse(names.contains("BOOKMARKS"))
        assertFalse(names.contains("HISTORY"))
        assertFalse(names.contains("PLAYBACK_HISTORY"))
        assertFalse(names.contains("DOWNLOADS"))
        assertFalse(names.contains("FILE_OPERATIONS"))
        assertFalse(names.contains("SMART_SUMMARY"))
        assertFalse(names.contains("LISTEN_MODE"))
    }

    @Test
    fun rootPageActionsHidePageActionsWithoutPage() {
        val noPageActions = FunctionCenterRootActionCatalog.actions(
            hasPage = false,
            hasSite = true,
            isPrivateBrowsing = false
        ).map { it.name }

        assertEquals(emptyList<String>(), noPageActions)
    }

    @Test
    fun rootPageActionsHideDesktopModeInPrivateBrowsing() {
        val privateActions = FunctionCenterRootActionCatalog.actions(
            hasPage = true,
            hasSite = true,
            isPrivateBrowsing = true
        ).map { it.name }

        assertFalse(privateActions.contains("DESKTOP_MODE"))
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
