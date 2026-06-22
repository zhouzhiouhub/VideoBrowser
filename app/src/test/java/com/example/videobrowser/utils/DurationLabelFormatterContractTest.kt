package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DurationLabelFormatterContractTest {
    @Test
    fun `video feedback and playback history share duration labels`() {
        val gestureFormatter = projectFile(
            "src/main/java/com/example/videobrowser/video/VideoGestureFeedbackFormatter.kt"
        ).readText()
        val playbackHistoryDisplayText = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/PlaybackHistoryDisplayText.kt"
        ).readText()

        assertTrue(gestureFormatter.contains("DurationLabelFormatter.formatMillis("))
        assertTrue(playbackHistoryDisplayText.contains("DurationLabelFormatter.formatMillis("))
        assertFalse(gestureFormatter.contains("val totalSeconds = (timeMs / 1000L)"))
        assertFalse(playbackHistoryDisplayText.contains("TimeUnit.MILLISECONDS.toSeconds"))
        assertFalse(playbackHistoryDisplayText.contains("SECONDS_PER_MINUTE"))
    }

}
