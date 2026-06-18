package com.example.videobrowser.video

import androidx.media3.common.C
import com.example.videobrowser.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeTrackSelectionOptionsTest {
    @Test
    fun menuOptionsExposeAudioThenSubtitles() {
        assertEquals(
            listOf(PlaybackTrackType.AUDIO, PlaybackTrackType.SUBTITLE),
            NativeTrackSelectionOptions.menuOptions().map { option -> option.playbackTrackType }
        )
    }

    @Test
    fun audioOptionUsesMedia3AudioTrackWithoutDisableOption() {
        val option = NativeTrackSelectionOptions.optionFor(PlaybackTrackType.AUDIO)

        assertEquals(C.TRACK_TYPE_AUDIO, option.media3TrackType)
        assertEquals(R.string.video_track_audio, option.titleResId)
        assertFalse(option.showDisableOption)
    }

    @Test
    fun subtitleOptionUsesMedia3TextTrackWithDisableOption() {
        val option = NativeTrackSelectionOptions.optionFor(PlaybackTrackType.SUBTITLE)

        assertEquals(C.TRACK_TYPE_TEXT, option.media3TrackType)
        assertEquals(R.string.video_track_subtitles, option.titleResId)
        assertTrue(option.showDisableOption)
    }
}
