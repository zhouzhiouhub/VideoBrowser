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
    /**
     * 测试函数 `profileShortcutsShowManualRulesAsTopGridAction`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `profile Shortcuts Show Manual Rules As Top Grid Action` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
                "DOWNLOADS",
                "FILE_OPERATIONS",
                "SEARCH_ENGINE",
                "USER_MANUAL_RULES",
                "ABOUT"
            ),
            actions
        )
        assertFalse(actions.contains("BOOKMARKS"))
        assertFalse(actions.contains("BROWSER_SETTINGS"))
    }

    /**
     * 测试函数 `profileShortcutsHideManualRulesInPrivateBrowsing`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `profile Shortcuts Hide Manual Rules In Private Browsing` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun profileShortcutsHideManualRulesInPrivateBrowsing() {
        val actions = FunctionCenterProfileActionCatalog.shortcuts(
            isPrivateBrowsing = true
        ).map { action -> action.name }

        assertEquals(
            listOf(
                "HISTORY",
                "PLAYBACK_HISTORY",
                "DOWNLOADS",
                "FILE_OPERATIONS",
                "SEARCH_ENGINE",
                "ABOUT"
            ),
            actions
        )
        assertFalse(actions.contains("BOOKMARKS"))
        assertFalse(actions.contains("USER_MANUAL_RULES"))
    }
}
