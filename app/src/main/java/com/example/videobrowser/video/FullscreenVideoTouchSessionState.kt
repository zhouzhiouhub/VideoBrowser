package com.example.videobrowser.video

internal class FullscreenVideoTouchSessionState {
    var touchStartedOnControl = false
        private set
    var touchStartedInBottomPassthrough = false
        private set
    var playbackControlsVisibleOnTouchStart = true
        private set
    var activeGesture = FullscreenVideoActiveGesture.NONE
        private set
    var tapCandidate = false
        private set
    var longPressActive = false
        private set
    var downZone = VideoGestureScreenZone.CENTER
        private set
    var downX = 0f
        private set
    var downY = 0f
        private set
    var downTime = 0L
        private set
    var initialBrightness = FullscreenVideoGestureMath.DEFAULT_BRIGHTNESS
        private set
    var initialVolume = 0
        private set

    fun beginDispatchDown(
        playbackControlsVisible: Boolean,
        startedOnControl: Boolean,
        startedInBottomPassthrough: Boolean
    ) {
        playbackControlsVisibleOnTouchStart = playbackControlsVisible
        touchStartedOnControl = startedOnControl
        touchStartedInBottomPassthrough = startedInBottomPassthrough
    }

    fun clearControlStart() {
        touchStartedOnControl = false
    }

    fun clearBottomPassthroughStart() {
        touchStartedInBottomPassthrough = false
    }

    fun beginGestureDown(
        x: Float,
        y: Float,
        eventTime: Long,
        zone: VideoGestureScreenZone,
        brightness: Float,
        volume: Int
    ) {
        downX = x
        downY = y
        downTime = eventTime
        downZone = zone
        initialBrightness = brightness
        initialVolume = volume
        activeGesture = FullscreenVideoActiveGesture.NONE
        tapCandidate = true
        longPressActive = false
    }

    fun cancelTapCandidate() {
        tapCandidate = false
    }

    fun setActiveGesture(gesture: FullscreenVideoActiveGesture) {
        activeGesture = gesture
    }

    fun startLongPress() {
        longPressActive = true
        tapCandidate = false
    }

    fun stopLongPress() {
        longPressActive = false
    }

    fun reset() {
        activeGesture = FullscreenVideoActiveGesture.NONE
        tapCandidate = false
        touchStartedOnControl = false
        touchStartedInBottomPassthrough = false
        playbackControlsVisibleOnTouchStart = true
        downZone = VideoGestureScreenZone.CENTER
    }
}

internal enum class FullscreenVideoActiveGesture {
    NONE,
    HORIZONTAL_SEEK,
    BRIGHTNESS,
    VOLUME
}
