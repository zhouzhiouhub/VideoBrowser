package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 FullscreenVideoController 可以拆开理解为“Fullscreen Video Controller”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import android.app.Activity
import android.os.SystemClock
import android.util.Log
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.ChromeClient
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.PlaybackSpeedNormalizer

/**
 * 网页全屏视频控制器。
 *
 * 它服务的是 WebView 页面里的 video 元素，不是原生 PlayerActivity。
 * 手势层接收用户操作后，这个控制器会发送 WebViewVideoCommand，让网页里的脚本执行播放、暂停、倍速和进度控制。
 */
class FullscreenVideoController(
    private val activity: Activity,
    private val rootView: ViewGroup,
    private val browserManager: () -> BrowserManager,
    private val settingsManager: () -> SettingsManager?,
    private val chromeClient: () -> ChromeClient?,
    private val dp: (Int) -> Int
) {
    private lateinit var gestureOverlay: FullscreenVideoGestureOverlay
    private var playbackSpeed = SettingsManager.DEFAULT_VIDEO_SPEED
    private var videoPositionMs: Long? = null
    private var videoDurationMs: Long? = null
    private var lastControlsWakeAt = 0L

    var isFullscreenUiActive = false
        private set

    /**
     * 函数 `attachOverlay`：封装 `attach Overlay` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun attachOverlay() {
        // 覆盖层加到主界面根布局上，只有网页视频进入全屏时才显示。
        gestureOverlay = FullscreenVideoGestureOverlay(activity).apply {
            elevation = dp(28).toFloat()
            onSeekBy = ::seekBy
            onSeekTo = ::seekTo
            onSeekPreviewStart = ::currentSeekPosition
            onTogglePlayPause = ::togglePlayback
            onPlaybackSpeedSelected = ::setPlaybackSpeed
            onDirectionalLongPressStart = ::startDirectionalLongPress
            onDirectionalLongPressEnd = ::stopDirectionalLongPress
            onUserInteraction = ::wakeControls
            onToggleOrientation = {
                chromeClient()?.toggleFullscreenOrientation() ?: true
            }
            onExitFullscreen = ::exitFullscreen
        }

        rootView.addView(
            gestureOverlay,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        )
    }

    /**
     * 函数 `handleFullscreenChanged`：处理 `handle Fullscreen Changed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param fullscreen 参数类型为 `Boolean`，表示函数执行 `fullscreen` 相关逻辑时需要读取或处理的输入。
     */
    fun handleFullscreenChanged(fullscreen: Boolean) {
        val wasFullscreen = isFullscreenUiActive
        isFullscreenUiActive = fullscreen
        Log.d(
            VIDEO_LOG_TAG,
            "event=web-fullscreen-changed fullscreen=$fullscreen wasFullscreen=$wasFullscreen"
        )
        if (!::gestureOverlay.isInitialized) {
            return
        }

        when {
            fullscreen && !wasFullscreen -> enterFullscreen()
            fullscreen -> refreshFullscreenOverlay()
            wasFullscreen -> hideFullscreenOverlay()
        }
    }

    /**
     * 函数 `wakeControls`：封装 `wake Controls` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun wakeControls() {
        if (!isFullscreenUiActive) {
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (now - lastControlsWakeAt < FULLSCREEN_CONTROLS_WAKE_THROTTLE_MS) {
            Log.d(VIDEO_LOG_TAG, "event=web-wake-controls throttled=true")
            return
        }
        lastControlsWakeAt = now
        Log.d(VIDEO_LOG_TAG, "event=web-wake-controls throttled=false")

        evaluateWebVideoCommand(WebViewVideoCommand.WakeControls)
    }

    /**
     * 函数 `updatePlaybackTimeline`：根据最新状态刷新 `update Playback Timeline` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param positionMs 参数类型为 `Double`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param durationMs 参数类型为 `Double`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    fun updatePlaybackTimeline(positionMs: Double, durationMs: Double) {
        val timeline = WebViewVideoTimeline.fromBridge(positionMs, durationMs)
        videoPositionMs = timeline.positionMs
        videoDurationMs = timeline.durationMs
    }

    /**
     * 函数 `enterFullscreen`：封装 `enter Fullscreen` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun enterFullscreen() {
        // 进入全屏时重置时间轴缓存，并把默认倍速同步到网页播放器。
        val defaultSpeed = defaultVideoSpeed()
        Log.d(VIDEO_LOG_TAG, "event=web-enter-fullscreen defaultSpeed=$defaultSpeed")
        resetTimeline()
        lastControlsWakeAt = 0L
        playbackSpeed = defaultSpeed
        gestureOverlay.setPlaybackSpeed(defaultSpeed)
        gestureOverlay.setLandscape(chromeClient()?.isFullscreenLandscape() ?: false)
        gestureOverlay.showOverlay()
        setPlaybackSpeed(defaultSpeed)
        wakeControls()
        requestTimeline()
    }

    /**
     * 函数 `refreshFullscreenOverlay`：根据最新状态刷新 `refresh Fullscreen Overlay` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun refreshFullscreenOverlay() {
        Log.d(
            VIDEO_LOG_TAG,
            "event=web-refresh-fullscreen landscape=${chromeClient()?.isFullscreenLandscape()}"
        )
        gestureOverlay.setLandscape(chromeClient()?.isFullscreenLandscape() ?: false)
        gestureOverlay.bringToFront()
        wakeControls()
        requestTimeline()
    }

    /**
     * 函数 `exitFullscreen`：封装 `exit Fullscreen` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun exitFullscreen() {
        Log.d(
            VIDEO_LOG_TAG,
            "event=web-exit-fullscreen customView=${chromeClient()?.isShowingCustomView()} " +
                "pageFullscreen=${chromeClient()?.isFullscreenModeActive()}"
        )
        if (chromeClient()?.isShowingCustomView() == true) {
            chromeClient()?.hideCustomView()
            return
        }
        if (chromeClient()?.isFullscreenModeActive() == true) {
            evaluateWebVideoCommand(WebViewVideoCommand.ExitFullscreen)
            chromeClient()?.exitPageFullscreen()
        }
    }

    /**
     * 函数 `hideFullscreenOverlay`：控制 `hide Fullscreen Overlay` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun hideFullscreenOverlay() {
        val defaultSpeed = defaultVideoSpeed()
        Log.d(VIDEO_LOG_TAG, "event=web-hide-fullscreen-overlay defaultSpeed=$defaultSpeed")
        resetTimeline()
        lastControlsWakeAt = 0L
        playbackSpeed = defaultSpeed
        setPlaybackSpeed(defaultSpeed)
        gestureOverlay.hideOverlay()
    }

    /**
     * 函数 `seekBy`：封装 `seek By` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param offsetMs 参数类型为 `Long`，表示函数执行 `offsetMs` 相关逻辑时需要读取或处理的输入。
     */
    private fun seekBy(offsetMs: Long) {
        Log.d(VIDEO_LOG_TAG, "event=web-seek-by offsetMs=$offsetMs positionMs=$videoPositionMs")
        videoPositionMs = boundedVideoPosition(offsetMs)
        evaluateWebVideoCommand(WebViewVideoCommand.SeekBy(offsetMs))
    }

    /**
     * 函数 `seekTo`：封装 `seek To` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param positionMs 参数类型为 `Long`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    private fun seekTo(positionMs: Long) {
        val duration = videoDurationMs
        Log.d(VIDEO_LOG_TAG, "event=web-seek-to requestedMs=$positionMs durationMs=$duration")
        val boundedPositionMs = PlaybackSeekBounds.clampPosition(positionMs, duration)
        videoPositionMs = boundedPositionMs
        evaluateWebVideoCommand(WebViewVideoCommand.SeekTo(boundedPositionMs))
    }

    /**
     * 函数 `currentSeekPosition`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun currentSeekPosition(): FullscreenVideoGestureOverlay.SeekPosition {
        requestTimeline()
        return FullscreenVideoGestureOverlay.SeekPosition(
            positionMs = videoPositionMs,
            durationMs = videoDurationMs
        )
    }

    /**
     * 函数 `boundedVideoPosition`：封装 `bounded Video Position` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param offsetMs 参数类型为 `Long`，表示函数执行 `offsetMs` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun boundedVideoPosition(offsetMs: Long): Long? {
        return PlaybackSeekBounds.offsetPosition(
            currentPositionMs = videoPositionMs,
            offsetMs = offsetMs,
            durationMs = videoDurationMs
        )
    }

    /**
     * 函数 `requestTimeline`：处理 `request Timeline` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun requestTimeline() {
        evaluateWebVideoCommand(WebViewVideoCommand.RequestTimeline)
    }

    /**
     * 函数 `resetTimeline`：封装 `reset Timeline` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun resetTimeline() {
        videoPositionMs = null
        videoDurationMs = null
    }

    /**
     * 函数 `togglePlayback`：封装 `toggle Playback` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun togglePlayback(): Boolean? {
        Log.d(VIDEO_LOG_TAG, "event=web-toggle-playback")
        evaluateWebVideoCommand(WebViewVideoCommand.TogglePlayPause)
        return null
    }

    /**
     * 函数 `setPlaybackSpeed`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param speed 参数类型为 `Float`，表示函数执行 `speed` 相关逻辑时需要读取或处理的输入。
     */
    private fun setPlaybackSpeed(speed: Float) {
        val normalizedSpeed = PlaybackSpeedNormalizer.normalize(
            speed,
            SettingsManager.DEFAULT_VIDEO_SPEED
        )
        playbackSpeed = normalizedSpeed
        settingsManager()?.setDefaultVideoSpeed(normalizedSpeed)
        if (::gestureOverlay.isInitialized) {
            gestureOverlay.setPlaybackSpeed(playbackSpeed)
        }
        evaluateWebVideoCommand(WebViewVideoCommand.SetPlaybackSpeed(normalizedSpeed))
    }

    /**
     * 函数 `startDirectionalLongPress`：启动或加载 `start Directional Long Press` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param direction 参数类型为 `Int`，表示函数执行 `direction` 相关逻辑时需要读取或处理的输入。
     */
    private fun startDirectionalLongPress(direction: Int) {
        evaluateWebVideoCommand(WebViewVideoCommand.StartDirectionalPlayback(direction))
    }

    /**
     * 函数 `stopDirectionalLongPress`：封装 `stop Directional Long Press` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun stopDirectionalLongPress() {
        evaluateWebVideoCommand(WebViewVideoCommand.StopDirectionalPlayback)
        setPlaybackSpeed(playbackSpeed)
    }

    /**
     * 函数 `evaluateWebVideoCommand`：封装 `evaluate Web Video Command` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param command 参数类型为 `WebViewVideoCommand`，表示函数执行 `command` 相关逻辑时需要读取或处理的输入。
     */
    private fun evaluateWebVideoCommand(command: WebViewVideoCommand) {
        // WebViewVideoCommand 负责生成安全的 JavaScript 字符串，避免在这里手写脚本片段。
        browserManager().evaluateJavascript(command.toJavascript())
    }

    /**
     * 函数 `defaultVideoSpeed`：封装 `default Video Speed` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun defaultVideoSpeed(): Float {
        return settingsManager()?.defaultVideoSpeed() ?: SettingsManager.DEFAULT_VIDEO_SPEED
    }

    private companion object {
        private const val VIDEO_LOG_TAG = "VideoBrowserVideo"
        private const val FULLSCREEN_CONTROLS_WAKE_THROTTLE_MS = 250L
    }
}
