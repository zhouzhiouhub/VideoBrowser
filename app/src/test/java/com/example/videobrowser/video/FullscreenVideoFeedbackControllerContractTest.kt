package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoFeedbackControllerContractTest {
    @Test
    fun `gesture overlay delegates feedback view lifecycle to controller`() {
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoFeedbackController.kt"
        ).readText()

        assertTrue(overlay.contains("FullscreenVideoFeedbackController("))
        assertTrue(overlay.contains("feedbackController.view"))
        assertTrue(overlay.contains("feedbackController.show(text, autoHide)"))
        assertTrue(overlay.contains("feedbackController.currentText()"))
        assertTrue(overlay.contains("feedbackController.hide()"))

        assertTrue(controller.contains("val view: TextView = TextView(context).apply"))
        assertTrue(controller.contains("ellipsize = TextUtils.TruncateAt.END"))
        assertTrue(controller.contains("background = BrowserDrawableFactory.roundedBackground("))
        assertTrue(controller.contains("private val hideRunnable = Runnable"))
        assertTrue(controller.contains("feedbackHandler.postDelayed(hideRunnable, FEEDBACK_DURATION_MS)"))
        assertTrue(controller.contains("fun currentText(): String"))

        assertFalse(overlay.contains("private val feedbackView"))
        assertFalse(overlay.contains("hideFeedbackRunnable"))
        assertFalse(overlay.contains("private const val FEEDBACK_DURATION_MS"))
        assertFalse(overlay.contains("feedbackHandler.postDelayed(hideFeedbackRunnable"))
        assertFalse(overlay.contains("feedbackView.text = text"))
        assertFalse(overlay.contains("feedbackView.visibility = View.GONE"))
        assertFalse(overlay.contains("ellipsize = TextUtils.TruncateAt.END"))
    }

}
