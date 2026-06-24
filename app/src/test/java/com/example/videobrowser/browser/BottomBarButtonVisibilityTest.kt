package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Bottom Bar Button Visibility Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomBarButtonVisibilityTest {
    /**
     * 测试函数 `homePage_keepsBottomNavigationVisibleWithBackDisabled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证首页保留底部菜单但禁用后退。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun homePage_keepsBottomNavigationVisibleWithBackDisabled() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = true)

        assertTrue(visibility.showBack)
        assertFalse(visibility.enableBack)
        assertTrue(visibility.showPageTools)
        assertTrue(visibility.showRefresh)
        assertTrue(visibility.showWenxin)
        assertTrue(visibility.showProfile)
    }

    /**
     * 测试函数 `browsingPage_showsOnlyFunctionalNavigationToolsAgain`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browsing Page shows Only Functional Navigation Tools Again` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browsingPage_showsOnlyFunctionalNavigationToolsAgain() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = false)

        assertTrue(visibility.showBack)
        assertTrue(visibility.enableBack)
        assertTrue(visibility.showPageTools)
        assertTrue(visibility.showRefresh)
        assertTrue(visibility.showWenxin)
        assertTrue(visibility.showProfile)
    }

    /**
     * 测试函数 `bottomBarVisibilityDoesNotExposeTabsHomeEntry`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `bottom Bar Visibility Does Not Expose Tabs Home Entry` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun bottomBarVisibilityDoesNotExposeTabsHomeEntry() {
        val fieldNames = BottomBarButtonVisibility::class.java.declaredFields.map { it.name }

        assertFalse(fieldNames.contains("showTabsHome"))
    }
}
