package com.example.videobrowser.video

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout

internal class NativePlayerGestureOverlayBinder(
    private val activity: Activity,
    private val playerRoot: FrameLayout,
    private val seekPreviewStart: () -> FullscreenVideoGestureOverlay.SeekPosition?,
    private val handlePlaybackCommand: (PlaybackCommand) -> Any?,
    private val startDirectionalLongPress: (Int) -> Unit,
    private val stopDirectionalLongPress: () -> Unit,
    private val toggleOrientation: () -> Boolean,
    private val wakePlayerControls: () -> Unit,
    private val arePlayerControlsVisible: () -> Boolean,
    private val exitFullscreen: () -> Unit,
    private val currentPlaybackSpeed: () -> Float,
    private val isLandscape: () -> Boolean,
    private val hasMultipleQueueItems: () -> Boolean,
    private val currentRepeatMode: () -> PlaybackRepeatMode,
    private val currentVideoZoomMode: () -> VideoZoomMode
) {
    fun attach(): FullscreenVideoGestureOverlay {
        val overlay = FullscreenVideoGestureOverlay(activity).apply {
            onSeekBy = { offsetMs -> handlePlaybackCommand(PlaybackCommand.SeekBy(offsetMs)) }
            onSeekTo = { positionMs -> handlePlaybackCommand(PlaybackCommand.SeekTo(positionMs)) }
            onSeekPreviewStart = seekPreviewStart
            onTogglePlayPause = {
                handlePlaybackCommand(PlaybackCommand.TogglePlayPause) as? Boolean
            }
            onPlaybackSpeedSelected = { speed ->
                handlePlaybackCommand(PlaybackCommand.SetSpeed(speed))
            }
            onDirectionalLongPressStart = startDirectionalLongPress
            onDirectionalLongPressEnd = stopDirectionalLongPress
            onToggleOrientation = toggleOrientation
            onUserInteraction = wakePlayerControls
            arePlaybackControlsVisible = arePlayerControlsVisible
            onExitFullscreen = exitFullscreen
            onTrackSelectionRequested = {
                handlePlaybackCommand(PlaybackCommand.ShowTrackSelection)
            }
            onPlaybackQueueRequested = { handlePlaybackCommand(PlaybackCommand.ShowQueue) }
            onVideoZoomRequested = {
                handlePlaybackCommand(PlaybackCommand.CycleZoom) as? VideoZoomMode
                    ?: currentVideoZoomMode()
            }
            onPreviousMediaRequested = { handlePlaybackCommand(PlaybackCommand.Previous) }
            onNextMediaRequested = { handlePlaybackCommand(PlaybackCommand.Next) }
            onRepeatModeRequested = {
                handlePlaybackCommand(PlaybackCommand.ToggleRepeat) as? PlaybackRepeatMode
                    ?: currentRepeatMode()
            }
            setPlaybackSpeed(currentPlaybackSpeed())
            setLandscape(isLandscape())
            setQueueControlsVisible(hasMultipleQueueItems())
            setRepeatMode(currentRepeatMode())
            setVideoZoomMode(currentVideoZoomMode())
        }
        playerRoot.addView(
            overlay,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        return overlay
    }
}
