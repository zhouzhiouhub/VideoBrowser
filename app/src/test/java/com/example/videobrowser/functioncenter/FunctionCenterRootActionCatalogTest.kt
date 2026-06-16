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
    /**
     * 测试函数 `rootActionsIncludeTabsAndCurrentPageActionsWithinTwoRows`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `root Actions Include Tabs And Current Page Actions Within Two Rows` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `rootActionsKeepTabsAndHidePageActionsWithoutPage`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `root Actions Keep Tabs And Hide Page Actions Without Page` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun rootActionsKeepTabsAndHidePageActionsWithoutPage() {
        val noPageActions = FunctionCenterRootActionCatalog.actions(
            hasPage = false,
            hasSite = true,
            isPrivateBrowsing = false
        ).map { it.name }

        assertEquals(listOf("TABS"), noPageActions)
    }

    /**
     * 测试函数 `rootPageActionsHideDesktopModeInPrivateBrowsing`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `root Page Actions Hide Desktop Mode In Private Browsing` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun rootPageActionsHideDesktopModeInPrivateBrowsing() {
        val privateActions = FunctionCenterRootActionCatalog.actions(
            hasPage = true,
            hasSite = true,
            isPrivateBrowsing = true
        ).map { it.name }

        assertFalse(privateActions.contains("DESKTOP_MODE"))
    }

    /**
     * 测试函数 `rootPageActionsHideElementPickerWithoutPageOrInPrivateBrowsing`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `root Page Actions Hide Element Picker Without Page Or In Private Browsing` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
