package com.example.videobrowser.video

internal class NativePlaybackCommandDispatcher(
    private val transportController: NativePlayerTransportController,
    private val playbackSpeedController: NativePlayerPlaybackSpeedController,
    private val queueController: NativePlayerQueueController,
    private val playbackQueue: () -> PlaybackQueue,
    private val setPlaybackQueue: (PlaybackQueue) -> Unit,
    private val repeatModeController: NativePlayerRepeatModeController,
    private val videoZoomController: NativePlayerVideoZoomController,
    private val trackSelectionDialogController: NativeTrackSelectionDialogController,
    private val playbackQueueDialogController: NativePlaybackQueueDialogController,
    private val updateQueueControls: () -> Unit
) {
    fun handle(command: PlaybackCommand): Any? {
        return when (command) {
            PlaybackCommand.Play -> transportController.play()
            PlaybackCommand.Pause -> transportController.pause()
            PlaybackCommand.TogglePlayPause -> transportController.togglePlayPause()
            is PlaybackCommand.SeekBy -> {
                transportController.seekBy(command.offsetMs)
                Unit
            }
            is PlaybackCommand.SeekTo -> {
                transportController.seekTo(command.positionMs)
                Unit
            }
            is PlaybackCommand.SetSpeed -> {
                playbackSpeedController.setSpeed(command.speed)
                Unit
            }
            PlaybackCommand.Previous -> {
                queueController.playPreviousMedia()
                Unit
            }
            PlaybackCommand.Next -> {
                queueController.playNextMedia()
                Unit
            }
            PlaybackCommand.ToggleRepeat -> {
                setPlaybackQueue(repeatModeController.cycle(playbackQueue()))
                updateQueueControls()
                repeatModeController.currentMode()
            }
            is PlaybackCommand.SelectQueueItem -> {
                queueController.playMediaAt(command.index)
                Unit
            }
            PlaybackCommand.ShowQueue -> {
                playbackQueueDialogController.showMenu()
                Unit
            }
            PlaybackCommand.ToggleShuffle -> queueController.toggleShuffleMode()
            PlaybackCommand.CycleZoom -> videoZoomController.cycle()
            PlaybackCommand.ShowTrackSelection -> {
                trackSelectionDialogController.showMenu()
                Unit
            }
            is PlaybackCommand.SelectTrack -> {
                trackSelectionDialogController.showDialog(command.trackType)
                Unit
            }
        }
    }
}
