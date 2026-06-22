package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoControlHitTesterContractTest {
    @Test
    fun gestureOverlayDelegatesControlHitTestingToHitTester() {
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val hitTester = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoControlHitTester.kt"
        ).readText()

        assertTrue(overlay.contains("FullscreenVideoControlHitTester("))
        assertTrue(overlay.contains("controlHitTester.isControlPoint(event.x, event.y)"))
        assertFalse(overlay.contains("private fun isControlPoint("))
        assertFalse(overlay.contains("private fun isPointInside("))

        assertTrue(hitTester.contains("internal class FullscreenVideoControlHitTester"))
        assertTrue(hitTester.contains("fun isControlPoint(x: Float, y: Float): Boolean"))
        assertTrue(hitTester.contains("isPointInside(exitButton, x, y)"))
        assertTrue(hitTester.contains("isPointInside(lockButton, x, y)"))
        assertTrue(hitTester.contains("!isLocked() && isPointInside(controlsGroup, x, y)"))
        assertTrue(hitTester.contains("view.visibility != View.VISIBLE"))
    }

}
