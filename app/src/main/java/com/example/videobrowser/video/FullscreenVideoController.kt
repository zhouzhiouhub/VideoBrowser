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

    fun updatePlaybackTimeline(positionMs: Double, durationMs: Double) {
        val timeline = WebViewVideoTimeline.fromBridge(positionMs, durationMs)
        videoPositionMs = timeline.positionMs
        videoDurationMs = timeline.durationMs
    }

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

    private fun hideFullscreenOverlay() {
        val defaultSpeed = defaultVideoSpeed()
        Log.d(VIDEO_LOG_TAG, "event=web-hide-fullscreen-overlay defaultSpeed=$defaultSpeed")
        resetTimeline()
        lastControlsWakeAt = 0L
        playbackSpeed = defaultSpeed
        setPlaybackSpeed(defaultSpeed)
        gestureOverlay.hideOverlay()
    }

    private fun seekBy(offsetMs: Long) {
        Log.d(VIDEO_LOG_TAG, "event=web-seek-by offsetMs=$offsetMs positionMs=$videoPositionMs")
        videoPositionMs = boundedVideoPosition(offsetMs)
        evaluateWebVideoCommand(WebViewVideoCommand.SeekBy(offsetMs))
    }

    private fun seekTo(positionMs: Long) {
        val duration = videoDurationMs
        Log.d(VIDEO_LOG_TAG, "event=web-seek-to requestedMs=$positionMs durationMs=$duration")
        val boundedPositionMs = if (duration != null && duration > 0L) {
            positionMs.coerceIn(0L, duration)
        } else {
            positionMs.coerceAtLeast(0L)
        }
        videoPositionMs = boundedPositionMs
        evaluateWebVideoCommand(WebViewVideoCommand.SeekTo(boundedPositionMs))
    }

    private fun currentSeekPosition(): FullscreenVideoGestureOverlay.SeekPosition {
        requestTimeline()
        return FullscreenVideoGestureOverlay.SeekPosition(
            positionMs = videoPositionMs,
            durationMs = videoDurationMs
        )
    }

    private fun boundedVideoPosition(offsetMs: Long): Long? {
        val current = videoPositionMs ?: return null
        val target = current + offsetMs
        val duration = videoDurationMs
        return if (duration != null && duration > 0L) {
            target.coerceIn(0L, duration)
        } else {
            target.coerceAtLeast(0L)
        }
    }

    private fun requestTimeline() {
        evaluateWebVideoCommand(WebViewVideoCommand.RequestTimeline)
    }

    private fun resetTimeline() {
        videoPositionMs = null
        videoDurationMs = null
    }

    private fun togglePlayback(): Boolean? {
        Log.d(VIDEO_LOG_TAG, "event=web-toggle-playback")
        evaluateWebVideoCommand(WebViewVideoCommand.TogglePlayPause)
        return null
    }

    private fun setPlaybackSpeed(speed: Float) {
        val normalizedSpeed = if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            SettingsManager.DEFAULT_VIDEO_SPEED
        }
        playbackSpeed = normalizedSpeed
        settingsManager()?.setDefaultVideoSpeed(normalizedSpeed)
        if (::gestureOverlay.isInitialized) {
            gestureOverlay.setPlaybackSpeed(playbackSpeed)
        }
        evaluateWebVideoCommand(WebViewVideoCommand.SetPlaybackSpeed(normalizedSpeed))
    }

    private fun startDirectionalLongPress(direction: Int) {
        evaluateWebVideoCommand(WebViewVideoCommand.StartDirectionalPlayback(direction))
    }

    private fun stopDirectionalLongPress() {
        evaluateWebVideoCommand(WebViewVideoCommand.StopDirectionalPlayback)
        setPlaybackSpeed(playbackSpeed)
    }

    private fun evaluateWebVideoCommand(command: WebViewVideoCommand) {
        // WebViewVideoCommand 负责生成安全的 JavaScript 字符串，避免在这里手写脚本片段。
        browserManager().evaluateJavascript(command.toJavascript())
    }

    private fun defaultVideoSpeed(): Float {
        return settingsManager()?.defaultVideoSpeed() ?: SettingsManager.DEFAULT_VIDEO_SPEED
    }

    private companion object {
        private const val VIDEO_LOG_TAG = "VideoBrowserVideo"
        private const val FULLSCREEN_CONTROLS_WAKE_THROTTLE_MS = 250L
    }
}
