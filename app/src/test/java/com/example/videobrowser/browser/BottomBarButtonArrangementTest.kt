package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Bottom Bar Button Arrangement Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class BottomBarButtonArrangementTest {
    /**
     * 测试函数 `homePage_evenlySpacesVisibleActionsAcrossTheFullBar`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `home Page evenly Spaces Visible Actions Across The Full Bar` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun homePage_evenlySpacesVisibleActionsAcrossTheFullBar() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = true)

        assertEquals(BottomBarButtonArrangement.VisibleActionsEvenlySpaced, BottomBarButtonArrangement.forVisibility(visibility))
    }

    /**
     * 测试函数 `browsingPage_evenlySpacesVisibleActionsAcrossTheFullBar`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browsing Page evenly Spaces Visible Actions Across The Full Bar` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browsingPage_evenlySpacesVisibleActionsAcrossTheFullBar() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = false)

        assertEquals(BottomBarButtonArrangement.VisibleActionsEvenlySpaced, BottomBarButtonArrangement.forVisibility(visibility))
    }
}
