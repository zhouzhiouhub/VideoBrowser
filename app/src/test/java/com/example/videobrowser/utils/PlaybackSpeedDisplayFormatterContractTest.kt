package com.example.videobrowser.utils

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSpeedDisplayFormatterContractTest {
    @Test
    fun `video feedback and playback history share speed labels`() {
        val gestureFormatter = projectFile(
            "src/main/java/com/example/videobrowser/video/VideoGestureFeedbackFormatter.kt"
        ).readText()
        val historyDisplayText = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/PlaybackHistoryDisplayText.kt"
        ).readText()

        assertTrue(gestureFormatter.contains("PlaybackSpeedDisplayFormatter.format(speed)"))
        assertTrue(historyDisplayText.contains("PlaybackSpeedDisplayFormatter.format(record.speed)"))
        assertFalse(historyDisplayText.contains("private fun formatSpeed"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
