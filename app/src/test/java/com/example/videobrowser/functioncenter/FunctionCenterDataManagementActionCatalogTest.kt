package com.example.videobrowser.functioncenter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FunctionCenterDataManagementActionCatalogTest {
    @Test
    fun dataManagementKeepsSingleResetEntryInsteadOfSeparateClearBrowsingData() {
        val actions = FunctionCenterDataManagementActionCatalog.actions(
            isPrivateBrowsing = false
        ).map { action -> action.name }

        assertEquals(
            listOf(
                "AD_BLOCK_LOG",
                "USER_WHITELIST",
                "USER_MANUAL_RULES",
                "RESTORE_DEFAULT_SETTINGS"
            ),
            actions
        )
        assertFalse(actions.contains("CLEAR_BROWSER_DATA"))
    }

    @Test
    fun privateBrowsingOnlyShowsRestoreDefaultSettings() {
        val actions = FunctionCenterDataManagementActionCatalog.actions(
            isPrivateBrowsing = true
        ).map { action -> action.name }

        assertEquals(listOf("RESTORE_DEFAULT_SETTINGS"), actions)
    }
}
