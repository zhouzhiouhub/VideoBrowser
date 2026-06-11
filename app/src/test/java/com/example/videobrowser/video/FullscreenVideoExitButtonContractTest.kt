package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoExitButtonContractTest {
    @Test
    fun fullscreenOverlayProvidesTopStartExitButton() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()

        assertTrue(source.contains("var onExitFullscreen: (() -> Unit)? = null"))
        assertTrue(source.contains("private val exitButton"))
        assertTrue(source.contains("setupExitButton()"))
        assertTrue(source.contains("gravity = Gravity.TOP or Gravity.START"))
        assertTrue(source.contains("onExitFullscreen?.invoke()"))
    }

    @Test
    fun fullscreenExitButtonHasAccessibleLabel() {
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"video_control_exit_fullscreen\""))
        assertTrue(strings.contains(">退出全屏<"))
    }

    @Test
    fun fullscreenControllerWiresExitButtonToChromeClient() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoController.kt"
        ).readText()

        assertTrue(source.contains("onExitFullscreen = ::exitFullscreen"))
        assertTrue(source.contains("chromeClient()?.hideCustomView()"))
        assertTrue(source.contains("chromeClient()?.exitPageFullscreen()"))
    }

    @Test
    fun nativePlayerWiresExitButtonToFinishActivity() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("onExitFullscreen = ::finish"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
