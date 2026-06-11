package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewVideoProtocolTest {
    @Test
    fun timelineNormalizesBridgeNumbers() {
        val timeline = WebViewVideoTimeline.fromBridge(
            positionMs = 12_345.8,
            durationMs = 60_000.0
        )
        val invalidTimeline = WebViewVideoTimeline.fromBridge(
            positionMs = Double.NaN,
            durationMs = -1.0
        )

        assertEquals(12_345L, timeline.positionMs)
        assertEquals(60_000L, timeline.durationMs)
        assertNull(invalidTimeline.positionMs)
        assertNull(invalidTimeline.durationMs)
    }

    @Test
    fun seekCommandsRenderSafeEnhancerCallsInSeconds() {
        val seekBy = WebViewVideoCommand.SeekBy(offsetMs = 1_500L).toJavascript()
        val seekTo = WebViewVideoCommand.SeekTo(positionMs = 65_250L).toJavascript()

        assertTrue(seekBy.contains("var enhancer=window.VideoBrowserEnhancer;"))
        assertTrue(seekBy.contains("typeof enhancer.seekBy==='function'"))
        assertTrue(seekBy.contains("enhancer.seekBy(1.500);"))
        assertTrue(seekTo.contains("typeof enhancer.seekTo==='function'"))
        assertTrue(seekTo.contains("enhancer.seekTo(65.250);"))
    }

    @Test
    fun playbackCommandsKeepFallbackGuardsAndNormalizeArguments() {
        val speed = WebViewVideoCommand.SetPlaybackSpeed(speed = 1.5f).toJavascript()
        val invalidSpeed = WebViewVideoCommand.SetPlaybackSpeed(speed = Float.NaN).toJavascript()
        val startReverse = WebViewVideoCommand.StartDirectionalPlayback(direction = -7).toJavascript()

        assertTrue(speed.contains("typeof enhancer.setPlaybackSpeed==='function'"))
        assertTrue(speed.contains("enhancer.setPlaybackSpeed(1.50);"))
        assertTrue(invalidSpeed.contains("enhancer.setPlaybackSpeed(1.00);"))
        assertTrue(startReverse.contains("enhancer.startDirectionalPlayback(-1);"))
        assertFalse(startReverse.contains("-7"))
    }

    @Test
    fun togglePlayPauseAlsoWakesControlsWhenAvailable() {
        val script = WebViewVideoCommand.TogglePlayPause.toJavascript()

        assertTrue(script.contains("enhancer.togglePlayPause();"))
        assertTrue(script.contains("enhancer.wakeControls();"))
        assertTrue(script.indexOf("enhancer.togglePlayPause();") < script.indexOf("enhancer.wakeControls();"))
    }
}
