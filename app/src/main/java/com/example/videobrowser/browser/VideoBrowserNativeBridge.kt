package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 VideoBrowserNativeBridge 可以拆开理解为“Video Browser Native Bridge”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.webkit.JavascriptInterface

class VideoBrowserNativeBridge(
    private val postToUi: ((() -> Unit) -> Unit),
    private val enterFullscreen: () -> Unit,
    private val exitFullscreen: () -> Unit,
    private val updatePlaybackTimeline: (Double, Double) -> Unit,
    private val requestElementBlock: (String, String) -> Unit,
    private val blockSelectedElement: (String) -> Unit,
    private val cancelElementPicker: () -> Unit,
    logVideoEvent: (String) -> Unit = {}
) {
    private val videoEventLogger = logVideoEvent

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
        if (!positionMs.isFinite() || !durationMs.isFinite()) {
            return
        }
        if (positionMs < 0.0 || durationMs < 0.0) {
            return
        }
        val sanitizedPositionMs = positionMs.coerceAtMost(MAX_PLAYBACK_TIMELINE_MS)
        val sanitizedDurationMs = durationMs.coerceAtMost(MAX_PLAYBACK_TIMELINE_MS)
        postToUi { updatePlaybackTimeline.invoke(sanitizedPositionMs, sanitizedDurationMs) }
    }

    @JavascriptInterface
    fun requestElementBlock(selector: String, description: String) {
        val sanitizedSelector = sanitizeSelector(selector) ?: return
        val sanitizedDescription = sanitizeBridgeText(description, MAX_ELEMENT_DESCRIPTION_LENGTH)
        postToUi { requestElementBlock.invoke(sanitizedSelector, sanitizedDescription) }
    }

    @JavascriptInterface
    fun blockSelectedElement(selector: String) {
        val sanitizedSelector = sanitizeSelector(selector) ?: return
        postToUi { blockSelectedElement.invoke(sanitizedSelector) }
    }

    @JavascriptInterface
    fun cancelElementPicker() {
        postToUi { cancelElementPicker.invoke() }
    }

    @JavascriptInterface
    fun logVideoEvent(message: String) {
        val sanitizedMessage = sanitizeBridgeText(message, MAX_VIDEO_LOG_LENGTH)
        if (sanitizedMessage.isBlank()) {
            return
        }
        postToUi { videoEventLogger.invoke(sanitizedMessage) }
    }

    private fun sanitizeSelector(selector: String): String? {
        return selector
            .trim()
            .filterNot { char -> char.isISOControl() }
            .take(MAX_ELEMENT_SELECTOR_LENGTH)
            .trim()
            .takeIf { sanitized -> sanitized.isNotBlank() }
    }

    private fun sanitizeBridgeText(text: String, maxLength: Int): String {
        return text
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(maxLength)
    }

    private companion object {
        private const val MAX_PLAYBACK_TIMELINE_MS = 86_400_000.0
        private const val MAX_ELEMENT_SELECTOR_LENGTH = 500
        private const val MAX_ELEMENT_DESCRIPTION_LENGTH = 500
        private const val MAX_VIDEO_LOG_LENGTH = 600
    }
}
