package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoSpeedPopupControllerContractTest {
    @Test
    fun gestureOverlayDelegatesSpeedPopupUiToController() {
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val popupController = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoSpeedPopupController.kt"
        ).readText()

        assertTrue(overlay.contains("FullscreenVideoSpeedPopupController("))
        assertTrue(overlay.contains("speedPopupController.show()"))
        assertTrue(overlay.contains("speedPopupController.dismiss()"))
        assertFalse(overlay.contains("PopupWindow("))
        assertFalse(overlay.contains("private var speedPopup"))
        assertFalse(overlay.contains("ColorDrawable(Color.TRANSPARENT)"))

        assertTrue(popupController.contains("private var speedPopup: PopupWindow? = null"))
        assertTrue(popupController.contains("fun show()"))
        assertTrue(popupController.contains("fun dismiss()"))
        assertTrue(popupController.contains("VideoGestureFeedbackFormatter.formatSpeed(speed)"))
        assertTrue(popupController.contains("onPlaybackSpeedSelected(speed)"))
        assertTrue(popupController.contains("showFeedback(VideoGestureFeedbackFormatter.formatSpeed(speed))"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
