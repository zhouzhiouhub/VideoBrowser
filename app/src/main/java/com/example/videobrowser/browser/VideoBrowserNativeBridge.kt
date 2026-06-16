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

    /**
     * 函数 `enterFullscreen`：封装 `enter Fullscreen` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @JavascriptInterface
    fun enterFullscreen() {
        postToUi { enterFullscreen.invoke() }
    }

    /**
     * 函数 `exitFullscreen`：封装 `exit Fullscreen` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @JavascriptInterface
    fun exitFullscreen() {
        postToUi { exitFullscreen.invoke() }
    }

    /**
     * 函数 `updatePlaybackTimeline`：根据最新状态刷新 `update Playback Timeline` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param positionMs 参数类型为 `Double`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param durationMs 参数类型为 `Double`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
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

    /**
     * 函数 `requestElementBlock`：处理 `request Element Block` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     * @param description 参数类型为 `String`，表示函数执行 `description` 相关逻辑时需要读取或处理的输入。
     */
    @JavascriptInterface
    fun requestElementBlock(selector: String, description: String) {
        val sanitizedSelector = sanitizeSelector(selector) ?: return
        val sanitizedDescription = sanitizeBridgeText(description, MAX_ELEMENT_DESCRIPTION_LENGTH)
        postToUi { requestElementBlock.invoke(sanitizedSelector, sanitizedDescription) }
    }

    /**
     * 函数 `blockSelectedElement`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     */
    @JavascriptInterface
    fun blockSelectedElement(selector: String) {
        val sanitizedSelector = sanitizeSelector(selector) ?: return
        postToUi { blockSelectedElement.invoke(sanitizedSelector) }
    }

    /**
     * 函数 `cancelElementPicker`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @JavascriptInterface
    fun cancelElementPicker() {
        postToUi { cancelElementPicker.invoke() }
    }

    /**
     * 函数 `logVideoEvent`：封装 `log Video Event` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param message 参数类型为 `String`，表示函数执行 `message` 相关逻辑时需要读取或处理的输入。
     */
    @JavascriptInterface
    fun logVideoEvent(message: String) {
        val sanitizedMessage = sanitizeBridgeText(message, MAX_VIDEO_LOG_LENGTH)
        if (sanitizedMessage.isBlank()) {
            return
        }
        postToUi { videoEventLogger.invoke(sanitizedMessage) }
    }

    /**
     * 函数 `sanitizeSelector`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun sanitizeSelector(selector: String): String? {
        return selector
            .trim()
            .filterNot { char -> char.isISOControl() }
            .take(MAX_ELEMENT_SELECTOR_LENGTH)
            .trim()
            .takeIf { sanitized -> sanitized.isNotBlank() }
    }

    /**
     * 函数 `sanitizeBridgeText`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @param maxLength 参数类型为 `Int`，表示函数执行 `maxLength` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
