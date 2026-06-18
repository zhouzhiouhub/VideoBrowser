package com.example.videobrowser.video

import androidx.media3.common.Player

object PlaybackRepeatModeMedia3Converter {
    fun toPlayerRepeatMode(mode: PlaybackRepeatMode): Int {
        return when (mode) {
            PlaybackRepeatMode.NONE -> Player.REPEAT_MODE_OFF
            PlaybackRepeatMode.ONE -> Player.REPEAT_MODE_ONE
            PlaybackRepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }
}
