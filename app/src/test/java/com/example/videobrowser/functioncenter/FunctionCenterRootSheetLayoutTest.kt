package com.example.videobrowser.functioncenter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FunctionCenterRootSheetLayoutTest {
    @Test
    fun pageToolsBottomSheetStopsAtHistoryPreview() {
        val blocks = FunctionCenterRootSheetLayout.blocks()

        assertEquals(
            listOf(
                FunctionCenterRootSheetBlock.ACTION_GRID,
                FunctionCenterRootSheetBlock.HISTORY_PREVIEW
            ),
            blocks
        )
        assertFalse(blocks.contains(FunctionCenterRootSheetBlock.EXPANDED_BROWSER_SETTINGS))
        assertFalse(blocks.contains(FunctionCenterRootSheetBlock.EXPANDED_DATA_MANAGEMENT))
    }
}
