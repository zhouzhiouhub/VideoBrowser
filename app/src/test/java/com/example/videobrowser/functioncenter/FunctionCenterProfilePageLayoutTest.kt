package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Function Center Profile Page Layout Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FunctionCenterProfilePageLayoutTest {
    @Test
    fun profilePageStartsWithShortcutsInsteadOfAHeaderCard() {
        val blocks = FunctionCenterProfilePageLayout.blocks()

        assertEquals(
            listOf(
                FunctionCenterProfilePageBlock.SHORTCUTS,
                FunctionCenterProfilePageBlock.FEATURES
            ),
            blocks
        )
        assertFalse(blocks.contains(FunctionCenterProfilePageBlock.PROFILE_HEADER))
    }
}
