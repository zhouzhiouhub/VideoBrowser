package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalPlaybackQueueWiringContractTest {
    @Test
    fun localDirectoryOpenBuildsAndPassesSiblingPlaybackQueue() {
        val localFiles = projectFile(
            "src/main/java/com/example/videobrowser/localfiles/LocalFilesController.kt"
        ).readText()
        val pageActions = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val navigator = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserExternalNavigator.kt"
        ).readText()

        assertTrue(localFiles.contains("LocalPlaybackQueueBuilder.fromDocuments"))
        assertTrue(localFiles.contains("playbackQueue"))
        assertTrue(pageActions.contains("playbackQueue: PlaybackQueue? = null"))
        assertTrue(navigator.contains("playbackQueue: PlaybackQueue? = null"))
        assertTrue(navigator.contains("playbackQueue = playbackQueue"))
    }

    @Test
    fun playerActivityRestoresQueueFromIntentAndExposesQueueControls() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()

        assertTrue(playerActivity.contains("playbackQueueFromIntent()"))
        assertTrue(playerActivity.contains("EXTRA_PLAYBACK_QUEUE"))
        assertTrue(playerActivity.contains("PlaybackCommand.Previous"))
        assertTrue(playerActivity.contains("PlaybackCommand.Next"))
        assertTrue(playerActivity.contains("PlaybackCommand.ToggleRepeat"))
        assertTrue(playerActivity.contains("Player.REPEAT_MODE_ONE"))
        assertTrue(playerActivity.contains("STATE_REPEAT_MODE"))
        assertTrue(playerActivity.contains("outState.putString(STATE_REPEAT_MODE, sessionState.repeatMode.name)"))
        assertTrue(overlay.contains("var onPreviousMediaRequested: (() -> Unit)? = null"))
        assertTrue(overlay.contains("var onNextMediaRequested: (() -> Unit)? = null"))
        assertTrue(overlay.contains("var onRepeatModeRequested: (() -> PlaybackRepeatMode)? = null"))
        assertTrue(overlay.contains("setQueueControlsVisible"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
