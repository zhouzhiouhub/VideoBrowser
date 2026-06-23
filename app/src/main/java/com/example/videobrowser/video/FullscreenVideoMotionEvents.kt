package com.example.videobrowser.video

import android.view.MotionEvent

internal object FullscreenVideoMotionEvents {
    fun isFinishedAction(event: MotionEvent): Boolean {
        return event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_CANCEL
    }

    fun isWakeControlsAction(event: MotionEvent): Boolean {
        return event.actionMasked == MotionEvent.ACTION_DOWN ||
            event.actionMasked == MotionEvent.ACTION_MOVE ||
            event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_HOVER_ENTER ||
            event.actionMasked == MotionEvent.ACTION_HOVER_MOVE
    }
}
