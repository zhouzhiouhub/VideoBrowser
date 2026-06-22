package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlayerWindowControllerContractTest {
    @Test
    fun `player activity delegates native window controls`() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val windowController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerWindowController.kt"
        ).readText()
        val orientationController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerOrientationController.kt"
        ).readText()
        val gestureOverlayBinder = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerGestureOverlayBinder.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlayerWindowController(this)"))
        assertTrue(playerActivity.contains("NativePlayerOrientationController("))
        assertTrue(playerActivity.contains("nativePlayerOrientationController.setLandscape(isLandscape = true)"))
        assertTrue(playerActivity.contains("toggleOrientation = nativePlayerOrientationController::toggle"))
        assertTrue(playerActivity.contains("isLandscape = nativePlayerOrientationController::isLandscape"))
        assertTrue(gestureOverlayBinder.contains("onToggleOrientation = toggleOrientation"))
        assertTrue(gestureOverlayBinder.contains("setLandscape(isLandscape())"))
        assertTrue(playerActivity.contains("nativePlayerWindowController.hideSystemBars()"))
        assertTrue(orientationController.contains("private var landscape = true"))
        assertTrue(orientationController.contains("windowController.applyOrientation(landscape)"))
        assertTrue(orientationController.contains("gestureOverlay()?.setLandscape(landscape)"))
        assertFalse(playerActivity.contains("WindowInsetsControllerCompat"))
        assertFalse(playerActivity.contains("private var isLandscape = true"))
        assertFalse(playerActivity.contains("private fun togglePlayerOrientation"))
        assertFalse(playerActivity.contains("private fun applyRequestedOrientation"))
        assertTrue(windowController.contains("WindowCompat.setDecorFitsSystemWindows"))
        assertTrue(windowController.contains("WindowInsetsCompat.Type.systemBars()"))
        assertTrue(windowController.contains("BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE"))
    }

}
