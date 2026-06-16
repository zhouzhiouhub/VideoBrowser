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
    @Test
    fun homePage_hidesNavigationToolsThatAreOnlyUsefulAfterOpeningPage() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = true)

        assertFalse(visibility.showBack)
        assertFalse(visibility.showPageTools)
        assertTrue(visibility.showRefresh)
        assertTrue(visibility.showWenxin)
        assertTrue(visibility.showProfile)
    }

    @Test
    fun browsingPage_showsOnlyFunctionalNavigationToolsAgain() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = false)

        assertTrue(visibility.showBack)
        assertTrue(visibility.showPageTools)
        assertTrue(visibility.showRefresh)
        assertTrue(visibility.showWenxin)
        assertTrue(visibility.showProfile)
    }

    @Test
    fun bottomBarVisibilityDoesNotExposeTabsHomeEntry() {
        val fieldNames = BottomBarButtonVisibility::class.java.declaredFields.map { it.name }

        assertFalse(fieldNames.contains("showTabsHome"))
    }
}
