package com.example.videobrowser.video

import android.app.Activity
import android.os.SystemClock
import android.util.Log
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.ChromeClient
import com.example.videobrowser.settings.SettingsManager
import java.util.Locale

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

        browserManager().evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.wakeControls==='function'){" +
                "window.VideoBrowserEnhancer.wakeControls();" +
                "}})();"
        )
    }

    fun updatePlaybackTimeline(positionMs: Double, durationMs: Double) {
        videoPositionMs = positionMs
            .takeIf { it.isFinite() && it >= 0.0 }
            ?.toLong()
        videoDurationMs = durationMs
            .takeIf { it.isFinite() && it > 0.0 }
            ?.toLong()
    }

    private fun enterFullscreen() {
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
            browserManager().evaluateJavascript(EXIT_VIDEO_FULLSCREEN_SCRIPT)
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
        val seconds = String.format(Locale.US, "%.3f", offsetMs / 1000.0)
        videoPositionMs = boundedVideoPosition(offsetMs)
        browserManager().evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.seekBy==='function'){" +
                "window.VideoBrowserEnhancer.seekBy($seconds);" +
                "}})();"
        )
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
        val seconds = String.format(Locale.US, "%.3f", boundedPositionMs / 1000.0)
        browserManager().evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.seekTo==='function'){" +
                "window.VideoBrowserEnhancer.seekTo($seconds);" +
                "}})();"
        )
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
        browserManager().evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.reportPlaybackTimeline==='function'){" +
                "window.VideoBrowserEnhancer.reportPlaybackTimeline();" +
                "}})();"
        )
    }

    private fun resetTimeline() {
        videoPositionMs = null
        videoDurationMs = null
    }

    private fun togglePlayback(): Boolean? {
        Log.d(VIDEO_LOG_TAG, "event=web-toggle-playback")
        browserManager().evaluateJavascript(
            "(function(){var enhancer=window.VideoBrowserEnhancer;" +
                "if(!enhancer)return;" +
                "if(typeof enhancer.togglePlayPause==='function'){" +
                "enhancer.togglePlayPause();" +
                "}" +
                "if(typeof enhancer.wakeControls==='function'){" +
                "enhancer.wakeControls();" +
                "}})();"
        )
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
        val speedValue = String.format(Locale.US, "%.2f", normalizedSpeed)
        browserManager().evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.setPlaybackSpeed==='function'){" +
                "window.VideoBrowserEnhancer.setPlaybackSpeed($speedValue);" +
                "}})();"
        )
    }

    private fun startDirectionalLongPress(direction: Int) {
        val normalizedDirection = if (direction < 0) -1 else 1
        browserManager().evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.startDirectionalPlayback==='function'){" +
                "window.VideoBrowserEnhancer.startDirectionalPlayback($normalizedDirection);" +
                "}})();"
        )
    }

    private fun stopDirectionalLongPress() {
        browserManager().evaluateJavascript(
            "(function(){if(window.VideoBrowserEnhancer&&" +
                "typeof window.VideoBrowserEnhancer.stopDirectionalPlayback==='function'){" +
                "window.VideoBrowserEnhancer.stopDirectionalPlayback();" +
                "}})();"
        )
        setPlaybackSpeed(playbackSpeed)
    }

    private fun defaultVideoSpeed(): Float {
        return settingsManager()?.defaultVideoSpeed() ?: SettingsManager.DEFAULT_VIDEO_SPEED
    }

    private companion object {
        private const val VIDEO_LOG_TAG = "VideoBrowserVideo"
        private const val FULLSCREEN_CONTROLS_WAKE_THROTTLE_MS = 250L
        private const val EXIT_VIDEO_FULLSCREEN_SCRIPT =
            "if(window.VideoBrowserEnhancer){window.VideoBrowserEnhancer.exitFullscreen();}"
    }
}
