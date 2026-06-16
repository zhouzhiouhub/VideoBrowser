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
    /**
     * 测试函数 `dataManagementKeepsSingleResetEntryInsteadOfSeparateClearBrowsingData`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `data Management Keeps Single Reset Entry Instead Of Separate Clear Browsing Data` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `privateBrowsingOnlyShowsCacheAndRestoreDefaultSettings`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `private Browsing Only Shows Cache And Restore Default Settings` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun privateBrowsingOnlyShowsCacheAndRestoreDefaultSettings() {
        val actions = FunctionCenterDataManagementActionCatalog.actions(
            isPrivateBrowsing = true
        ).map { action -> action.name }

        assertEquals(listOf("CACHE", "RESTORE_DEFAULT_SETTINGS"), actions)
    }

    /**
     * 测试函数 `profileDataManagementOnlyShowsRestoreDefaultSettings`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `profile Data Management Only Shows Restore Default Settings` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun profileDataManagementOnlyShowsRestoreDefaultSettings() {
        val actions = FunctionCenterDataManagementActionCatalog.profileActions()
            .map { action -> action.name }

        assertEquals(listOf("RESTORE_DEFAULT_SETTINGS"), actions)
    }
}
