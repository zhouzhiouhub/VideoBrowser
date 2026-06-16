package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Function Center Profile Action Catalog Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
                "PLAYBACK_HISTORY",
                "BOOKMARKS",
                "DOWNLOADS",
                "FILE_OPERATIONS",
                "USER_MANUAL_RULES",
                "ABOUT"
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
                "PLAYBACK_HISTORY",
                "BOOKMARKS",
                "DOWNLOADS",
                "FILE_OPERATIONS",
                "ABOUT"
            ),
            actions
        )
        assertFalse(actions.contains("USER_MANUAL_RULES"))
    }
}
