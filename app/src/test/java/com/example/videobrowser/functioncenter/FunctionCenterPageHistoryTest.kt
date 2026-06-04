package com.example.videobrowser.functioncenter

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
