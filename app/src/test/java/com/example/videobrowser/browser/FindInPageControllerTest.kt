package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FindInPageControllerTest {
    @Test
    fun searchTrimsQueryAndStartsWebViewFind() {
        val queries = mutableListOf<String>()
        val controller = FindInPageController(
            findAll = { query -> queries += query },
            findNext = {},
            clearMatches = {}
        )

        assertTrue(controller.search("  video  "))

        assertEquals(listOf("video"), queries)
        assertEquals("video", controller.currentQuery)
    }

    @Test
    fun searchRejectsBlankQuery() {
        var findAllCalled = false
        val controller = FindInPageController(
            findAll = { findAllCalled = true },
            findNext = {},
            clearMatches = {}
        )

        assertFalse(controller.search("   "))

        assertFalse(findAllCalled)
        assertEquals(null, controller.currentQuery)
    }

    @Test
    fun findNextRequiresAnActiveQuery() {
        val directions = mutableListOf<Boolean>()
        val controller = FindInPageController(
            findAll = {},
            findNext = { forward -> directions += forward },
            clearMatches = {}
        )

        assertFalse(controller.findNext())
        controller.search("clip")

        assertTrue(controller.findNext())
        assertTrue(controller.findNext(forward = false))
        assertEquals(listOf(true, false), directions)
    }

    @Test
    fun clearRemovesCurrentQueryAndClearsWebViewMatches() {
        var clearCount = 0
        val controller = FindInPageController(
            findAll = {},
            findNext = {},
            clearMatches = { clearCount++ }
        )
        controller.search("clip")

        controller.clear()

        assertEquals(null, controller.currentQuery)
        assertEquals(1, clearCount)
    }
}
