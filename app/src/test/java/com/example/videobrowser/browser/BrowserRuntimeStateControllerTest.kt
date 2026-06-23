package com.example.videobrowser.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserRuntimeStateControllerTest {
    @Test
    fun isHomePageVisibleDefaultsToHomeBeforeSessionsAreInitialized() {
        var currentSessionRequested = false
        val controller = BrowserRuntimeStateController(
            areBrowserSessionsInitialized = { false },
            currentSessionController = {
                currentSessionRequested = true
                error("Current session should not be read before session controllers exist.")
            },
            fullscreenVideoController = { null }
        )

        assertTrue(controller.isHomePageVisible())
        assertFalse(currentSessionRequested)
    }
}
