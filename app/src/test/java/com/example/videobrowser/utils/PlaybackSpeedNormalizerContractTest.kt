package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSpeedNormalizerContractTest {
    @Test
    fun `playback speed normalization has a single implementation`() {
        val normalizer = projectFile(
            "src/main/java/com/example/videobrowser/utils/PlaybackSpeedNormalizer.kt"
        ).readText()
        val gestureMath = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureMath.kt"
        ).readText()
        val sessionState = projectFile(
            "src/main/java/com/example/videobrowser/video/PlaybackSessionState.kt"
        ).readText()
        val historyRepository = projectFile(
            "src/main/java/com/example/videobrowser/video/PlaybackHistoryRepository.kt"
        ).readText()
        val fullscreenController = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoController.kt"
        ).readText()
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val nativePlaybackSpeedController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerPlaybackSpeedController.kt"
        ).readText()
        val webViewVideoProtocol = projectFile(
            "src/main/java/com/example/videobrowser/video/WebViewVideoProtocol.kt"
        ).readText()
        val settingsValueNormalizer = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsValueNormalizer.kt"
        ).readText()
        val displayFormatter = projectFile(
            "src/main/java/com/example/videobrowser/utils/PlaybackSpeedDisplayFormatter.kt"
        ).readText()
        val playbackHistoryDisplayText = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/PlaybackHistoryDisplayText.kt"
        ).readText()

        assertTrue(normalizer.contains("object PlaybackSpeedNormalizer"))
        assertTrue(normalizer.contains("fun normalize(speed: Float"))
        assertTrue(gestureMath.contains("PlaybackSpeedNormalizer.normalize(speed, defaultSpeed)"))
        assertTrue(sessionState.contains("PlaybackSpeedNormalizer.normalize(speed)"))
        assertTrue(historyRepository.contains("PlaybackSpeedNormalizer.normalize(progress.speed)"))
        assertTrue(fullscreenController.contains("PlaybackSpeedNormalizer.normalize("))
        assertTrue(nativePlaybackSpeedController.contains("PlaybackSpeedNormalizer::normalize"))
        assertTrue(webViewVideoProtocol.contains("PlaybackSpeedNormalizer.normalize(speed, DEFAULT_PLAYBACK_SPEED)"))
        assertTrue(settingsValueNormalizer.contains("PlaybackSpeedNormalizer.normalize("))
        assertTrue(displayFormatter.contains("PlaybackSpeedNormalizer.normalize(speed)"))
        assertTrue(playbackHistoryDisplayText.contains("PlaybackSpeedDisplayFormatter.format(record.speed)"))
        assertFalse(gestureMath.contains("!speed.isNaN() && !speed.isInfinite() && speed > 0f"))
        assertFalse(sessionState.contains("private fun normalizeSpeed"))
        assertFalse(historyRepository.contains("private fun normalizeSpeed"))
        assertFalse(playerActivity.contains("private fun normalizePlaybackSpeed"))
        assertFalse(playerActivity.contains("PlaybackSpeedNormalizer.normalize("))
        assertFalse(fullscreenController.contains("!speed.isNaN() && !speed.isInfinite() && speed > 0f"))
        assertFalse(webViewVideoProtocol.contains("!speed.isNaN() && !speed.isInfinite() && speed > 0f"))
        assertFalse(settingsValueNormalizer.contains("!speed.isNaN() && !speed.isInfinite() && speed > 0f"))
        assertFalse(playbackHistoryDisplayText.contains("PlaybackSpeedNormalizer.normalize(speed)"))
        assertFalse(playbackHistoryDisplayText.contains("!speed.isNaN() && !speed.isInfinite() && speed > 0f"))
    }

}
