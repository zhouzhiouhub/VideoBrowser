package com.example.videobrowser.functioncenter

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
