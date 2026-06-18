package com.example.videobrowser.utils

import java.io.File
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
        assertTrue(playbackHistoryDisplayText.contains("DurationLabelFormatter.formatMillis(durationMs)"))
        assertFalse(gestureFormatter.contains("val totalSeconds = (timeMs / 1000L)"))
        assertFalse(playbackHistoryDisplayText.contains("TimeUnit.MILLISECONDS.toSeconds"))
        assertFalse(playbackHistoryDisplayText.contains("SECONDS_PER_MINUTE"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
