package com.example.videobrowser.video

internal class NativePlayerOrientationController(
    private val windowController: NativePlayerWindowController,
    private val gestureOverlay: () -> FullscreenVideoGestureOverlay?
) {
    private var landscape = true

    fun isLandscape(): Boolean {
        return landscape
    }

    fun setLandscape(isLandscape: Boolean) {
        landscape = isLandscape
        apply()
    }

    fun toggle(): Boolean {
        setLandscape(!landscape)
        return landscape
    }

    fun apply() {
        windowController.applyOrientation(landscape)
        gestureOverlay()?.setLandscape(landscape)
    }
}
