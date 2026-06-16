package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Bottom Bar Button Arrangement Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class BottomBarButtonArrangementTest {
    @Test
    fun homePage_evenlySpacesVisibleActionsAcrossTheFullBar() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = true)

        assertEquals(BottomBarButtonArrangement.VisibleActionsEvenlySpaced, BottomBarButtonArrangement.forVisibility(visibility))
    }

    @Test
    fun browsingPage_evenlySpacesVisibleActionsAcrossTheFullBar() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = false)

        assertEquals(BottomBarButtonArrangement.VisibleActionsEvenlySpaced, BottomBarButtonArrangement.forVisibility(visibility))
    }
}
