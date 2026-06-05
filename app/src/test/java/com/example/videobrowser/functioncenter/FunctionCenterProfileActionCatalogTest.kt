package com.example.videobrowser.functioncenter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FunctionCenterProfileActionCatalogTest {
    @Test
    fun profileShortcutsExcludeBrowserSettingsBecauseExpandedSettingsAreAlreadyShown() {
        val actions = FunctionCenterProfileActionCatalog.shortcuts()
            .map { action -> action.name }

        assertEquals(
            listOf(
                "HISTORY",
                "BOOKMARKS",
                "DOWNLOADS",
                "FILE_OPERATIONS"
            ),
            actions
        )
        assertFalse(actions.contains("BROWSER_SETTINGS"))
    }
}
