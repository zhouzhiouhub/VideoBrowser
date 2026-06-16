package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Function Center Action Grid Layout Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class FunctionCenterActionGridLayoutTest {
    /**
     * 测试函数 `sevenActionsUseFiveEqualSlotsPerRowWithTrailingEmptySlots`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `seven Actions Use Five Equal Slots Per Row With Trailing Empty Slots` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `oneActionKeepsTheActionInTheFirstSlot`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `one Action Keeps The Action In The First Slot` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun oneActionKeepsTheActionInTheFirstSlot() {
        val slots = FunctionCenterActionGridLayout.rows(actionCount = 1)

        assertEquals(listOf(listOf(0, null, null, null, null)), slots)
    }
}
