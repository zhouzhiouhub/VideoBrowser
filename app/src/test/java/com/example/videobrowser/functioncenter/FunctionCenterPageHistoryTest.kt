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
    @Test
    fun backReturnsToTheMostRecentOpeningPage() {
        val history = FunctionCenterPageHistory<String>()

        history.push("root")
        history.push("settings")

        assertEquals("settings", history.pop())
        assertEquals("root", history.pop())
        assertNull(history.pop())
    }

    @Test
    fun clearDropsOpeningPages() {
        val history = FunctionCenterPageHistory<String>()

        history.push("root")
        history.clear()

        assertNull(history.pop())
    }
}
