package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Function Center Page History Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FunctionCenterPageHistoryTest {
    /**
     * 测试函数 `backReturnsToTheMostRecentOpeningPage`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `back Returns To The Most Recent Opening Page` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun backReturnsToTheMostRecentOpeningPage() {
        val history = FunctionCenterPageHistory<String>()

        history.push("root")
        history.push("settings")

        assertEquals("settings", history.pop())
        assertEquals("root", history.pop())
        assertNull(history.pop())
    }

    /**
     * 测试函数 `clearDropsOpeningPages`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear Drops Opening Pages` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun clearDropsOpeningPages() {
        val history = FunctionCenterPageHistory<String>()

        history.push("root")
        history.clear()

        assertNull(history.pop())
    }
}
