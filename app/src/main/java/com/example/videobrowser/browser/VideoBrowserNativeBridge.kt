package com.example.videobrowser.browser

import android.webkit.JavascriptInterface

class VideoBrowserNativeBridge(
    private val postToUi: ((() -> Unit) -> Unit),
    private val enterFullscreen: () -> Unit,
    private val exitFullscreen: () -> Unit,
    private val updatePlaybackTimeline: (Double, Double) -> Unit,
    private val requestElementBlock: (String, String) -> Unit,
    private val blockSelectedElement: (String) -> Unit,
    private val cancelElementPicker: () -> Unit
) {
    @JavascriptInterface
    fun enterFullscreen() {
        postToUi { enterFullscreen.invoke() }
    }

    @JavascriptInterface
    fun exitFullscreen() {
        postToUi { exitFullscreen.invoke() }
    }

    @JavascriptInterface
    fun updatePlaybackTimeline(positionMs: Double, durationMs: Double) {
        postToUi { updatePlaybackTimeline.invoke(positionMs, durationMs) }
    }

    @JavascriptInterface
    fun requestElementBlock(selector: String, description: String) {
        postToUi { requestElementBlock.invoke(selector, description) }
    }

    @JavascriptInterface
    fun blockSelectedElement(selector: String) {
        postToUi { blockSelectedElement.invoke(selector) }
    }

    @JavascriptInterface
    fun cancelElementPicker() {
        postToUi { cancelElementPicker.invoke() }
    }
}
