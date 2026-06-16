package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Function Center Root Sheet Layout Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FunctionCenterRootSheetLayoutTest {
    /**
     * 测试函数 `pageToolsBottomSheetOnlyShowsCurrentPageActionGrid`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `page Tools Bottom Sheet Only Shows Current Page Action Grid` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun pageToolsBottomSheetOnlyShowsCurrentPageActionGrid() {
        val blocks = FunctionCenterRootSheetLayout.blocks()

        assertEquals(
            listOf(
                FunctionCenterRootSheetBlock.ACTION_GRID
            ),
            blocks
        )
        assertFalse(blocks.contains(FunctionCenterRootSheetBlock.HISTORY_PREVIEW))
        assertFalse(blocks.contains(FunctionCenterRootSheetBlock.EXPANDED_BROWSER_SETTINGS))
        assertFalse(blocks.contains(FunctionCenterRootSheetBlock.EXPANDED_DATA_MANAGEMENT))
    }
}
