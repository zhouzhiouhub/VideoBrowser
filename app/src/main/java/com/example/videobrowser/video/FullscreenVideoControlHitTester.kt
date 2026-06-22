package com.example.videobrowser.video

import android.view.View

internal class FullscreenVideoControlHitTester(
    private val exitButton: View,
    private val lockButton: View,
    private val controlsGroup: View,
    private val isLocked: () -> Boolean
) {
    fun isControlPoint(x: Float, y: Float): Boolean {
        return isPointInside(exitButton, x, y) ||
            isPointInside(lockButton, x, y) ||
            (!isLocked() && isPointInside(controlsGroup, x, y))
    }

    private fun isPointInside(view: View, x: Float, y: Float): Boolean {
        if (view.visibility != View.VISIBLE) return false
        return x >= view.left &&
            x <= view.right &&
            y >= view.top &&
            y <= view.bottom
    }
}
