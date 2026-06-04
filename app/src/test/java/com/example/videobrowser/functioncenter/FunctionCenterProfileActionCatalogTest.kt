package com.example.videobrowser.functioncenter

import org.junit.Assert.assertEquals
import org.junit.Test

class FunctionCenterProfileActionCatalogTest {
    @Test
    fun profileShortcutsEndWithBrowserSettingsInsteadOfDataRows() {
        val actions = FunctionCenterProfileActionCatalog.shortcuts()
            .map { action -> action.name }

        assertEquals(
            listOf(
                "HISTORY",
                "BOOKMARKS",
                "FILE_OPERATIONS",
                "BROWSER_SETTINGS"
            ),
            actions
        )
    }
}
