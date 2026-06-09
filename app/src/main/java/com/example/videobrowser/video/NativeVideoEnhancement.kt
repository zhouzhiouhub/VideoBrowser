package com.example.videobrowser.video

import androidx.media3.common.Effect
import androidx.media3.effect.Contrast

object NativeVideoEnhancement {
    fun defaultEffects(): List<Effect> {
        return listOf(Contrast(DEFAULT_CONTRAST))
    }

    private const val DEFAULT_CONTRAST = 0.08f
}
