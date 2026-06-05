package com.example.videobrowser.functioncenter

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
