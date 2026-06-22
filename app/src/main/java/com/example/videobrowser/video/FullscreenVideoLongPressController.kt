package com.example.videobrowser.video

import android.os.Handler

internal class FullscreenVideoLongPressController(
    private val feedbackHandler: Handler,
    private val touchSession: FullscreenVideoTouchSessionState,
    private val isLocked: () -> Boolean,
    private val clearSideTapState: () -> Unit,
    private val requestDirectionalLongPressStart: (Int) -> Unit,
    private val requestDirectionalLongPressEnd: () -> Unit,
    private val showFeedback: (String, Boolean) -> Unit,
    private val hideFeedback: () -> Unit
) {
    private val longPressRunnable = Runnable {
        trigger()
    }

    fun scheduleIfSideZone(delayMs: Long) {
        if (touchSession.downZone.isSide()) {
            feedbackHandler.postDelayed(longPressRunnable, delayMs)
        }
    }

    fun cancelScheduled() {
        feedbackHandler.removeCallbacks(longPressRunnable)
    }

    fun trigger() {
        if (isLocked() ||
            touchSession.longPressActive ||
            touchSession.activeGesture != FullscreenVideoActiveGesture.NONE ||
            !touchSession.downZone.isSide()
        ) {
            return
        }
        touchSession.startLongPress()
        clearSideTapState()
        val direction = if (touchSession.downZone == VideoGestureScreenZone.LEFT) -1 else 1
        requestDirectionalLongPressStart(direction)
        showFeedback(
            VideoGestureFeedbackFormatter.formatSpeed(VideoSpeedOptions.longPressSpeed),
            false
        )
    }

    fun stopActive() {
        if (!touchSession.longPressActive) return
        touchSession.stopLongPress()
        requestDirectionalLongPressEnd()
        hideFeedback()
    }
}
