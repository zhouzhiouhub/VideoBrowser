package com.example.videobrowser.video

import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.TrackSelectionDialogBuilder
import com.example.videobrowser.R
import com.example.videobrowser.utils.ActionListDialog
import com.example.videobrowser.utils.DialogAction
import com.example.videobrowser.utils.ShortToast

internal class NativeTrackSelectionDialogController(
    private val activity: AppCompatActivity,
    private val playerProvider: () -> ExoPlayer?,
    private val wakePlayerControls: () -> Unit,
    private val onTrackTypeSelected: (PlaybackTrackType) -> Unit
) {
    fun showMenu() {
        if (playerProvider() == null) {
            showUnavailableToast()
            return
        }
        wakePlayerControls()
        val options = NativeTrackSelectionOptions.menuOptions()
        val actions = options.map { option ->
            DialogAction(activity.getString(option.titleResId)) {
                onTrackTypeSelected(option.playbackTrackType)
            }
        }
        ActionListDialog.show(
            activity = activity,
            titleRes = R.string.video_control_tracks,
            actions = actions
        )
    }

    @OptIn(UnstableApi::class)
    fun showDialog(trackType: PlaybackTrackType) {
        val exoPlayer = playerProvider() ?: run {
            showUnavailableToast()
            return
        }
        val option = NativeTrackSelectionOptions.optionFor(trackType)
        val hasTracks = exoPlayer.currentTracks.groups.any { group ->
            group.type == option.media3TrackType
        }
        if (!hasTracks) {
            showUnavailableToast()
            return
        }
        TrackSelectionDialogBuilder(
            activity,
            activity.getString(option.titleResId),
            exoPlayer,
            option.media3TrackType
        )
            .setShowDisableOption(option.showDisableOption)
            .setAllowAdaptiveSelections(false)
            .setAllowMultipleOverrides(false)
            .build()
            .show()
    }

    private fun showUnavailableToast() {
        ShortToast.show(activity, R.string.toast_video_tracks_unavailable)
    }
}
