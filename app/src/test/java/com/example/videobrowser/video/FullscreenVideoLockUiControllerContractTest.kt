package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoLockUiControllerContractTest {
    @Test
    fun gestureOverlayDelegatesLockUiStateToController() {
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val lockController = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoLockUiController.kt"
        ).readText()

        assertTrue(overlay.contains("FullscreenVideoLockUiController("))
        assertTrue(overlay.contains("lockUiController.setLocked(false, announce = false)"))
        assertTrue(overlay.contains("lockUiController.toggle(announce = true)"))
        assertTrue(overlay.contains("private val locked: Boolean"))
        assertTrue(overlay.contains("private fun clearGestureStateWhenLocked()"))
        assertFalse(overlay.contains("private var locked"))
        assertFalse(overlay.contains("private fun setLocked"))
        assertFalse(overlay.contains("private fun updateLockUi"))

        assertTrue(lockController.contains("var locked: Boolean = false"))
        assertTrue(lockController.contains("fun setLocked(value: Boolean, announce: Boolean)"))
        assertTrue(lockController.contains("fun update()"))
        assertTrue(lockController.contains("lockButton.contentDescription = controlLabel"))
        assertTrue(lockController.contains("ViewCompat.setTooltipText(lockButton, controlLabel)"))
        assertTrue(lockController.contains("controlsGroup.visibility = if (locked) View.GONE else View.VISIBLE"))
        assertTrue(lockController.contains("dismissSpeedPopup()"))
        assertTrue(lockController.contains("clearGestureStateWhenLocked()"))
    }

}
