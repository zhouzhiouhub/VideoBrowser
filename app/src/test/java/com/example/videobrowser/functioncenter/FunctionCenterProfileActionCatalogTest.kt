package com.example.videobrowser.functioncenter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FunctionCenterProfileActionCatalogTest {
    @Test
    fun profileShortcutsShowManualRulesAsTopGridAction() {
        val actions = FunctionCenterProfileActionCatalog.shortcuts(
            isPrivateBrowsing = false
        )
            .map { action -> action.name }

        assertEquals(
            listOf(
                "HISTORY",
                "BOOKMARKS",
                "DOWNLOADS",
                "FILE_OPERATIONS",
                "USER_MANUAL_RULES"
            ),
            actions
        )
        assertFalse(actions.contains("BROWSER_SETTINGS"))
    }

    @Test
    fun profileShortcutsHideManualRulesInPrivateBrowsing() {
        val actions = FunctionCenterProfileActionCatalog.shortcuts(
            isPrivateBrowsing = true
        ).map { action -> action.name }

        assertEquals(
            listOf(
                "HISTORY",
                "BOOKMARKS",
                "DOWNLOADS",
                "FILE_OPERATIONS"
            ),
            actions
        )
        assertFalse(actions.contains("USER_MANUAL_RULES"))
    }
}
