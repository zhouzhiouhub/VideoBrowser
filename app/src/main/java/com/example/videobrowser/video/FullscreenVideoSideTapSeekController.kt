package com.example.videobrowser.video

import android.os.Handler

internal class FullscreenVideoSideTapSeekController(
    private val feedbackHandler: Handler,
    private val seekBy: (Long) -> Unit,
    private val showFeedback: (String) -> Unit
) {
    private var pendingTapZone = VideoGestureScreenZone.NONE
    private var pendingTapTime = 0L
    private var seekAccumulatorDirection = 0
    private var seekAccumulatorCount = 0

    private val clearPendingTapRunnable = Runnable {
        clearPendingTapState()
    }

    private val clearSeekAccumulatorRunnable = Runnable {
        clearSeekAccumulatorState()
    }

    fun registerTap(zone: VideoGestureScreenZone, eventTime: Long) {
        if (!zone.isSide()) {
            clearPendingTap()
            return
        }

        if (pendingTapZone == zone && eventTime - pendingTapTime <= DOUBLE_TAP_TIMEOUT_MS) {
            clearPendingTap()
            handleDoubleTap(zone)
            return
        }

        pendingTapZone = zone
        pendingTapTime = eventTime
        feedbackHandler.removeCallbacks(clearPendingTapRunnable)
        feedbackHandler.postDelayed(clearPendingTapRunnable, DOUBLE_TAP_TIMEOUT_MS)
    }

    fun clearPendingTap() {
        clearPendingTapState()
        feedbackHandler.removeCallbacks(clearPendingTapRunnable)
    }

    fun clearSeekAccumulator() {
        clearSeekAccumulatorState()
        feedbackHandler.removeCallbacks(clearSeekAccumulatorRunnable)
    }

    fun clearAll() {
        clearPendingTap()
        clearSeekAccumulator()
    }

    private fun handleDoubleTap(zone: VideoGestureScreenZone) {
        val direction = if (zone == VideoGestureScreenZone.LEFT) -1 else 1
        seekBy(direction * SEEK_STEP_MS)
        if (seekAccumulatorDirection != direction) {
            seekAccumulatorDirection = direction
            seekAccumulatorCount = 0
        }
        seekAccumulatorCount += 1
        val seconds = direction * seekAccumulatorCount * SEEK_STEP_SECONDS
        showFeedback(VideoGestureFeedbackFormatter.formatSeekSeconds(seconds))
        feedbackHandler.removeCallbacks(clearSeekAccumulatorRunnable)
        feedbackHandler.postDelayed(clearSeekAccumulatorRunnable, SEEK_ACCUMULATE_RESET_MS)
    }

    private fun clearPendingTapState() {
        pendingTapZone = VideoGestureScreenZone.NONE
        pendingTapTime = 0L
    }

    private fun clearSeekAccumulatorState() {
        seekAccumulatorDirection = 0
        seekAccumulatorCount = 0
    }

    private companion object {
        private const val SEEK_STEP_MS = 10_000L
        private const val SEEK_STEP_SECONDS = 10
        private const val DOUBLE_TAP_TIMEOUT_MS = 280L
        private const val SEEK_ACCUMULATE_RESET_MS = 850L
    }
}
