package com.example.videobrowser.functioncenter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FunctionCenterRootActionCatalogTest {
    @Test
    fun rootPageActionsContainOnlyImplementedEntries() {
        val actions = FunctionCenterRootActionCatalog.actions(
            hasPage = true,
            hasSite = true,
            isPrivateBrowsing = false
        )
        val names = actions.map { it.name }

        assertEquals(
            listOf(
                "SHARE_PAGE",
                "BOOKMARKS",
                "HISTORY",
                "FILE_OPERATIONS",
                "REFRESH",
                "ADD_BOOKMARK",
                "MORE"
            ),
            names
        )
        assertFalse(names.contains("SMART_SUMMARY"))
        assertFalse(names.contains("LISTEN_MODE"))
    }
}
