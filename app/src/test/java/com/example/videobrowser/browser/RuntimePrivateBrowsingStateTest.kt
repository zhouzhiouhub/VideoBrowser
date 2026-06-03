package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimePrivateBrowsingStateTest {
    @Test
    fun startsInStandardMode() {
        val state = RuntimePrivateBrowsingState()

        assertEquals(BrowserMode.STANDARD, state.mode)
        assertFalse(state.isPrivate)
    }

    @Test
    fun enterAndExitPrivateMode_areRuntimeOnly() {
        var cleanupCalls = 0
        val state = RuntimePrivateBrowsingState(onPrivateCleanup = { cleanupCalls++ })

        assertTrue(state.enterPrivate())
        assertEquals(BrowserMode.PRIVATE, state.mode)
        assertTrue(state.isPrivate)

        assertTrue(state.exitPrivate())
        assertEquals(BrowserMode.STANDARD, state.mode)
        assertFalse(state.isPrivate)
        assertEquals(1, cleanupCalls)
    }

    @Test
    fun repeatedTransitions_doNotRepeatCleanup() {
        var cleanupCalls = 0
        val state = RuntimePrivateBrowsingState(onPrivateCleanup = { cleanupCalls++ })

        assertFalse(state.exitPrivate())
        assertTrue(state.enterPrivate())
        assertFalse(state.enterPrivate())
        assertTrue(state.exitPrivate())
        assertFalse(state.exitPrivate())

        assertEquals(BrowserMode.STANDARD, state.mode)
        assertEquals(1, cleanupCalls)
    }
}
