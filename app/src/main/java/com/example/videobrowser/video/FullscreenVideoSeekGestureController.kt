package com.example.videobrowser.video

internal class FullscreenVideoSeekGestureController(
    private val seekPreviewStart: () -> FullscreenVideoGestureOverlay.SeekPosition?,
    private val seekBy: (Long) -> Unit,
    private val seekTo: (Long) -> Unit,
    private val currentFeedbackText: () -> String,
    private val showFeedback: (String, Boolean) -> Unit,
    private val hideFeedback: () -> Unit
) {
    private var startPositionMs: Long? = null
    private var durationMs: Long? = null
    private var pendingOffsetMs = 0L
    private var pendingTargetMs: Long? = null

    fun begin(deltaX: Float, viewWidth: Int) {
        val position = seekPreviewStart()
        startPositionMs = position?.positionMs?.takeIf { it >= 0L }
        durationMs = position?.durationMs?.takeIf { it > 0L }
        pendingOffsetMs = 0L
        pendingTargetMs = startPositionMs
        update(deltaX, viewWidth)
    }

    fun update(deltaX: Float, viewWidth: Int) {
        val currentDuration = durationMs
        val offsetMs = VideoSeekDragCalculator.offsetForDrag(deltaX, viewWidth, currentDuration)
        pendingOffsetMs = offsetMs

        val target = startPositionMs?.let {
            VideoSeekDragCalculator.targetForDrag(
                startPositionMs = it,
                durationMs = currentDuration,
                deltaX = deltaX,
                viewWidth = viewWidth
            )
        }
        pendingTargetMs = target

        showFeedback(
            VideoGestureFeedbackFormatter.formatSeekPreview(offsetMs, target, currentDuration),
            false
        )
    }

    fun finish(commit: Boolean) {
        val feedbackText = currentFeedbackText()
        if (commit && pendingOffsetMs != 0L) {
            pendingTargetMs?.let(seekTo) ?: seekBy(pendingOffsetMs)
        }

        if (commit && feedbackText.isNotBlank()) {
            showFeedback(feedbackText, true)
        } else {
            hideFeedback()
        }

        reset()
    }

    private fun reset() {
        startPositionMs = null
        durationMs = null
        pendingOffsetMs = 0L
        pendingTargetMs = null
    }
}
