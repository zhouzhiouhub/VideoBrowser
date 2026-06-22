package com.example.videobrowser.video

import android.view.MotionEvent
import kotlin.math.abs

internal class FullscreenVideoGestureEventHandler(
    private val touchSession: FullscreenVideoTouchSessionState,
    private val systemGestureController: FullscreenVideoSystemGestureController,
    private val sideTapSeekController: FullscreenVideoSideTapSeekController,
    private val seekGestureController: FullscreenVideoSeekGestureController,
    private val longPressController: FullscreenVideoLongPressController,
    private val touchSlop: Int,
    private val swipeStartDistance: () -> Int,
    private val viewWidth: () -> Int,
    private val viewHeight: () -> Int,
    private val screenZoneFor: (Float) -> VideoGestureScreenZone,
    private val handleTap: (Float, Long) -> Unit,
    private val resetTouchState: () -> Unit
) {
    fun handle(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchSession.beginGestureDown(
                    x = event.x,
                    y = event.y,
                    eventTime = event.eventTime,
                    zone = screenZoneFor(event.x),
                    brightness = systemGestureController.currentWindowBrightness(),
                    volume = systemGestureController.currentStreamVolume()
                )
                longPressController.scheduleIfSideZone(LONG_PRESS_TIMEOUT_MS)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - touchSession.downX
                val deltaY = event.y - touchSession.downY
                if (!touchSession.longPressActive &&
                    (abs(deltaX) > touchSlop || abs(deltaY) > touchSlop)
                ) {
                    touchSession.cancelTapCandidate()
                    longPressController.cancelScheduled()
                }
                if (!touchSession.longPressActive &&
                    touchSession.activeGesture == FullscreenVideoActiveGesture.NONE &&
                    abs(deltaX) >= swipeStartDistance() &&
                    abs(deltaX) > abs(deltaY)
                ) {
                    beginHorizontalSeek(deltaX)
                } else if (!touchSession.longPressActive &&
                    touchSession.activeGesture == FullscreenVideoActiveGesture.NONE &&
                    touchSession.downZone.isSide() &&
                    abs(deltaY) >= swipeStartDistance() &&
                    abs(deltaY) > abs(deltaX) * VERTICAL_GESTURE_RATIO
                ) {
                    touchSession.setActiveGesture(
                        if (touchSession.downZone == VideoGestureScreenZone.LEFT) {
                            FullscreenVideoActiveGesture.BRIGHTNESS
                        } else {
                            FullscreenVideoActiveGesture.VOLUME
                        }
                    )
                }
                when (touchSession.activeGesture) {
                    FullscreenVideoActiveGesture.HORIZONTAL_SEEK -> updateHorizontalSeek(deltaX)
                    FullscreenVideoActiveGesture.BRIGHTNESS -> systemGestureController.updateBrightness(
                        deltaY = deltaY,
                        viewHeight = viewHeight(),
                        initialBrightness = touchSession.initialBrightness
                    )
                    FullscreenVideoActiveGesture.VOLUME -> systemGestureController.updateVolume(
                        deltaY = deltaY,
                        viewHeight = viewHeight(),
                        initialVolume = touchSession.initialVolume
                    )
                    FullscreenVideoActiveGesture.NONE -> Unit
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                longPressController.cancelScheduled()
                if (touchSession.longPressActive) {
                    longPressController.stopActive()
                    resetTouchState()
                    return true
                }
                if (touchSession.activeGesture == FullscreenVideoActiveGesture.HORIZONTAL_SEEK) {
                    finishHorizontalSeek(commit = true)
                    resetTouchState()
                    return true
                }
                if (touchSession.activeGesture == FullscreenVideoActiveGesture.NONE &&
                    touchSession.tapCandidate &&
                    event.eventTime - touchSession.downTime <= TAP_MAX_DURATION_MS
                ) {
                    handleTap(event.x, event.eventTime)
                }
                resetTouchState()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                longPressController.cancelScheduled()
                longPressController.stopActive()
                if (touchSession.activeGesture == FullscreenVideoActiveGesture.HORIZONTAL_SEEK) {
                    finishHorizontalSeek(commit = false)
                }
                resetTouchState()
                return true
            }
        }
        return true
    }

    fun cancelActiveHorizontalSeek() {
        if (touchSession.activeGesture == FullscreenVideoActiveGesture.HORIZONTAL_SEEK) {
            finishHorizontalSeek(commit = false)
        }
    }

    private fun beginHorizontalSeek(deltaX: Float) {
        touchSession.setActiveGesture(FullscreenVideoActiveGesture.HORIZONTAL_SEEK)
        sideTapSeekController.clearAll()
        longPressController.cancelScheduled()
        seekGestureController.begin(deltaX, viewWidth())
    }

    private fun updateHorizontalSeek(deltaX: Float) {
        seekGestureController.update(deltaX, viewWidth())
    }

    private fun finishHorizontalSeek(commit: Boolean) {
        seekGestureController.finish(commit)
    }

    private companion object {
        private const val TAP_MAX_DURATION_MS = 260L
        private const val LONG_PRESS_TIMEOUT_MS = 520L
        private const val VERTICAL_GESTURE_RATIO = 1.15f
    }
}
