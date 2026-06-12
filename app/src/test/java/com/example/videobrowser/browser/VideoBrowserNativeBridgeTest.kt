package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoBrowserNativeBridgeTest {
    @Test
    fun updatePlaybackTimelineIgnoresInvalidValues() {
        val timelineUpdates = mutableListOf<Pair<Double, Double>>()
        val bridge = bridge(
            updatePlaybackTimeline = { positionMs, durationMs ->
                timelineUpdates += positionMs to durationMs
            }
        )

        bridge.updatePlaybackTimeline(Double.NaN, 1000.0)
        bridge.updatePlaybackTimeline(1000.0, Double.POSITIVE_INFINITY)
        bridge.updatePlaybackTimeline(-1.0, 1000.0)
        bridge.updatePlaybackTimeline(1000.0, -1.0)

        assertEquals(emptyList<Pair<Double, Double>>(), timelineUpdates)
    }

    @Test
    fun updatePlaybackTimelineClampsLargeValues() {
        val timelineUpdates = mutableListOf<Pair<Double, Double>>()
        val bridge = bridge(
            updatePlaybackTimeline = { positionMs, durationMs ->
                timelineUpdates += positionMs to durationMs
            }
        )

        bridge.updatePlaybackTimeline(100_000_000.0, 200_000_000.0)

        assertEquals(listOf(86_400_000.0 to 86_400_000.0), timelineUpdates)
    }

    @Test
    fun elementBlockCallbacksSanitizeSelectorsAndDescriptions() {
        val requests = mutableListOf<Pair<String, String>>()
        val blockedSelectors = mutableListOf<String>()
        val bridge = bridge(
            requestElementBlock = { selector, description -> requests += selector to description },
            blockSelectedElement = { selector -> blockedSelectors += selector }
        )

        bridge.requestElementBlock("  div > .ad\n  ", "banner\nsponsor")
        bridge.blockSelectedElement("  ${"a".repeat(700)}  ")

        assertEquals(listOf("div > .ad" to "banner sponsor"), requests)
        assertEquals(listOf("a".repeat(500)), blockedSelectors)
    }

    @Test
    fun elementBlockCallbacksIgnoreBlankSelectors() {
        val requests = mutableListOf<Pair<String, String>>()
        val blockedSelectors = mutableListOf<String>()
        val bridge = bridge(
            requestElementBlock = { selector, description -> requests += selector to description },
            blockSelectedElement = { selector -> blockedSelectors += selector }
        )

        bridge.requestElementBlock("\n\t", "description")
        bridge.blockSelectedElement("   ")

        assertEquals(emptyList<Pair<String, String>>(), requests)
        assertEquals(emptyList<String>(), blockedSelectors)
    }

    @Test
    fun logVideoEventSanitizesAndPostsMessageToLogger() {
        val messages = mutableListOf<String>()
        val bridge = bridge(
            logVideoEvent = { message -> messages += message }
        )

        bridge.logVideoEvent("controls\nremoved\rfor\tvideo")

        assertEquals(listOf("controls removed for video"), messages)
    }

    private fun bridge(
        updatePlaybackTimeline: (Double, Double) -> Unit = { _, _ -> },
        requestElementBlock: (String, String) -> Unit = { _, _ -> },
        blockSelectedElement: (String) -> Unit = {},
        logVideoEvent: (String) -> Unit = {}
    ): VideoBrowserNativeBridge {
        return VideoBrowserNativeBridge(
            postToUi = { action -> action() },
            enterFullscreen = {},
            exitFullscreen = {},
            updatePlaybackTimeline = updatePlaybackTimeline,
            requestElementBlock = requestElementBlock,
            blockSelectedElement = blockSelectedElement,
            cancelElementPicker = {},
            logVideoEvent = logVideoEvent
        )
    }
}
