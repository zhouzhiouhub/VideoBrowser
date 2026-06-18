package com.example.videobrowser.video

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.TrackSelectionDialogBuilder
import com.example.videobrowser.R

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
        AlertDialog.Builder(activity)
            .setTitle(R.string.video_control_tracks)
            .setItems(
                options.map { option -> activity.getString(option.titleResId) }
                    .toTypedArray()
            ) { _, which ->
                onTrackTypeSelected(options[which].playbackTrackType)
            }
            .show()
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
        Toast.makeText(activity, R.string.toast_video_tracks_unavailable, Toast.LENGTH_SHORT).show()
    }
}
