package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Function Center Action Grid Layout Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class FunctionCenterActionGridLayoutTest {
    @Test
    fun sevenActionsUseFiveEqualSlotsPerRowWithTrailingEmptySlots() {
        val slots = FunctionCenterActionGridLayout.rows(actionCount = 7)

        assertEquals(
            listOf(
                listOf(0, 1, 2, 3, 4),
                listOf(5, 6, null, null, null)
            ),
            slots
        )
    }

    @Test
    fun oneActionKeepsTheActionInTheFirstSlot() {
        val slots = FunctionCenterActionGridLayout.rows(actionCount = 1)

        assertEquals(listOf(listOf(0, null, null, null, null)), slots)
    }
}
