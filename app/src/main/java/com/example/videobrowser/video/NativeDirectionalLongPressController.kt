package com.example.videobrowser.video

import android.os.Handler
import androidx.media3.exoplayer.ExoPlayer
import com.example.videobrowser.utils.PlaybackSpeedNormalizer

internal class NativeDirectionalLongPressController(
    private val scheduler: PlaybackScanScheduler,
    private val seekBy: (Long) -> Unit,
    private val speedNormalizer: (Float) -> Float = PlaybackSpeedNormalizer::normalize
) {
    private var active = false
    private var direction = 0
    private var restoreSpeed = DEFAULT_PLAYBACK_SPEED
    private var restorePlayWhenReady = true
    private var playbackTarget: DirectionalLongPressPlaybackTarget? = null

    private val reverseScanRunnable = object : Runnable {
        override fun run() {
            if (!active || direction >= 0) {
                return
            }
            seekBy(-REVERSE_SCAN_STEP_MS)
            scheduler.postDelayed(this, REVERSE_SCAN_INTERVAL_MS)
        }
    }

    fun start(
        direction: Int,
        selectedPlaybackSpeed: Float,
        playbackTarget: DirectionalLongPressPlaybackTarget
    ) {
        stop()

        active = true
        this.direction = if (direction < 0) -1 else 1
        this.playbackTarget = playbackTarget
        restoreSpeed = selectedPlaybackSpeed
        restorePlayWhenReady = playbackTarget.playWhenReady

        if (this.direction > 0) {
            playbackTarget.setPlaybackSpeed(VideoSpeedOptions.longPressSpeed)
            playbackTarget.play()
        } else {
            playbackTarget.pause()
            seekBy(-REVERSE_SCAN_STEP_MS)
            scheduler.postDelayed(reverseScanRunnable, REVERSE_SCAN_INTERVAL_MS)
        }
    }

    fun stop(): Float? {
        if (!active) {
            return null
        }

        active = false
        direction = 0
        scheduler.removeCallbacks(reverseScanRunnable)

        val restoredSpeed = speedNormalizer(restoreSpeed)
        playbackTarget?.let { target ->
            target.setPlaybackSpeed(restoredSpeed)
            if (restorePlayWhenReady) {
                target.play()
            } else {
                target.pause()
            }
        }
        playbackTarget = null
        return restoredSpeed
    }

    private companion object {
        private const val DEFAULT_PLAYBACK_SPEED = 1f
        private const val REVERSE_SCAN_STEP_MS = 500L
        private const val REVERSE_SCAN_INTERVAL_MS = 250L
    }
}

internal interface DirectionalLongPressPlaybackTarget {
    val playWhenReady: Boolean

    fun setPlaybackSpeed(speed: Float)

    fun play()

    fun pause()
}

internal class ExoPlayerDirectionalLongPressTarget(
    private val player: ExoPlayer
) : DirectionalLongPressPlaybackTarget {
    override val playWhenReady: Boolean
        get() = player.playWhenReady

    override fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }
}

internal interface PlaybackScanScheduler {
    fun postDelayed(runnable: Runnable, delayMs: Long)

    fun removeCallbacks(runnable: Runnable)
}

internal class HandlerPlaybackScanScheduler(
    private val handler: Handler
) : PlaybackScanScheduler {
    override fun postDelayed(runnable: Runnable, delayMs: Long) {
        handler.postDelayed(runnable, delayMs)
    }

    override fun removeCallbacks(runnable: Runnable) {
        handler.removeCallbacks(runnable)
    }
}
