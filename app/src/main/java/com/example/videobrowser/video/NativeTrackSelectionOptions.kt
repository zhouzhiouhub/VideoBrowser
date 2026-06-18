package com.example.videobrowser.video

import androidx.media3.common.C
import com.example.videobrowser.R

internal data class NativeTrackSelectionOption(
    val playbackTrackType: PlaybackTrackType,
    val media3TrackType: Int,
    val titleResId: Int,
    val showDisableOption: Boolean
)

internal object NativeTrackSelectionOptions {
    fun menuOptions(): List<NativeTrackSelectionOption> {
        return listOf(
            optionFor(PlaybackTrackType.AUDIO),
            optionFor(PlaybackTrackType.SUBTITLE)
        )
    }

    fun optionFor(trackType: PlaybackTrackType): NativeTrackSelectionOption {
        return when (trackType) {
            PlaybackTrackType.AUDIO -> {
                NativeTrackSelectionOption(
                    playbackTrackType = trackType,
                    media3TrackType = C.TRACK_TYPE_AUDIO,
                    titleResId = R.string.video_track_audio,
                    showDisableOption = false
                )
            }

            PlaybackTrackType.SUBTITLE -> {
                NativeTrackSelectionOption(
                    playbackTrackType = trackType,
                    media3TrackType = C.TRACK_TYPE_TEXT,
                    titleResId = R.string.video_track_subtitles,
                    showDisableOption = true
                )
            }
        }
    }
}
