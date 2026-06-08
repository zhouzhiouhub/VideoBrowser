package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoBrowserNativeBridgeTest {
    @Test
    fun logVideoEventSanitizesAndPostsMessageToLogger() {
        val messages = mutableListOf<String>()
        val bridge = VideoBrowserNativeBridge(
            postToUi = { action -> action() },
            enterFullscreen = {},
            exitFullscreen = {},
            updatePlaybackTimeline = { _, _ -> },
            requestElementBlock = { _, _ -> },
            blockSelectedElement = {},
            cancelElementPicker = {},
            logVideoEvent = { message -> messages += message }
        )

        bridge.logVideoEvent("controls\nremoved\rfor\tvideo")

        assertEquals(listOf("controls removed for video"), messages)
    }
}
