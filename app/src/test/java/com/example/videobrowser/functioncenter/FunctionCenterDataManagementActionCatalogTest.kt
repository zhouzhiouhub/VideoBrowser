package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Function Center Data Management Action Catalog Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
                "SITE_PERMISSIONS",
                "RULE_SUBSCRIPTIONS",
                "BOOKMARKS",
                "HISTORY",
                "DOWNLOADS",
                "COOKIES",
                "CACHE",
                "SITE_DATA",
                "RESTORE_DEFAULT_SETTINGS"
            ),
            actions
        )
        assertFalse(actions.contains("CLEAR_BROWSER_DATA"))
    }

    @Test
    fun privateBrowsingOnlyShowsCacheAndRestoreDefaultSettings() {
        val actions = FunctionCenterDataManagementActionCatalog.actions(
            isPrivateBrowsing = true
        ).map { action -> action.name }

        assertEquals(listOf("CACHE", "RESTORE_DEFAULT_SETTINGS"), actions)
    }

    @Test
    fun profileDataManagementOnlyShowsRestoreDefaultSettings() {
        val actions = FunctionCenterDataManagementActionCatalog.profileActions()
            .map { action -> action.name }

        assertEquals(listOf("RESTORE_DEFAULT_SETTINGS"), actions)
    }
}
