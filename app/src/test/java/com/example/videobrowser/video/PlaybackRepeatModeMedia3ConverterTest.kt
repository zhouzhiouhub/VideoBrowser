package com.example.videobrowser.video

import androidx.media3.common.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackRepeatModeMedia3ConverterTest {
    @Test
    fun `repeat mode converts to Media3 player constants`() {
        assertEquals(
            Player.REPEAT_MODE_OFF,
            PlaybackRepeatModeMedia3Converter.toPlayerRepeatMode(PlaybackRepeatMode.NONE)
        )
        assertEquals(
            Player.REPEAT_MODE_ONE,
            PlaybackRepeatModeMedia3Converter.toPlayerRepeatMode(PlaybackRepeatMode.ONE)
        )
        assertEquals(
            Player.REPEAT_MODE_ALL,
            PlaybackRepeatModeMedia3Converter.toPlayerRepeatMode(PlaybackRepeatMode.ALL)
        )
    }
}
