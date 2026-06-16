package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Function Center Root Action Catalog Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctionCenterRootActionCatalogTest {
    @Test
    fun rootActionsIncludeTabsAndCurrentPageActionsWithinTwoRows() {
        val actions = FunctionCenterRootActionCatalog.actions(
            hasPage = true,
            hasSite = true,
            isPrivateBrowsing = false
        )
        val names = actions.map { it.name }

        assertEquals(
            listOf(
                "TABS",
                "HOME",
                "SHARE_PAGE",
                "SAVE_PAGE_ARCHIVE",
                "PRINT_PAGE",
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
    fun rootActionsKeepTabsAndHidePageActionsWithoutPage() {
        val noPageActions = FunctionCenterRootActionCatalog.actions(
            hasPage = false,
            hasSite = true,
            isPrivateBrowsing = false
        ).map { it.name }

        assertEquals(listOf("TABS"), noPageActions)
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
