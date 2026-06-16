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
