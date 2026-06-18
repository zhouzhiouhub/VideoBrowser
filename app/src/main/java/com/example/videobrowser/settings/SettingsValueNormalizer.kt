package com.example.videobrowser.settings

import com.example.videobrowser.utils.PlaybackSpeedNormalizer

internal object SettingsValueNormalizer {
    fun videoSpeed(speed: Float): Float {
        return PlaybackSpeedNormalizer.normalize(speed, SettingsManager.DEFAULT_VIDEO_SPEED)
    }

    fun textZoomPercent(percent: Int): Int {
        return percent.takeIf { value -> value in SettingsManager.TEXT_ZOOM_OPTIONS }
            ?: SettingsManager.DEFAULT_TEXT_ZOOM_PERCENT
    }

    fun homeUrl(value: String?, defaultValue: String): String {
        return homeUrlOrNull(value) ?: defaultValue
    }

    fun homeUrlOrNull(value: String?): String? {
        return value
            ?.trim()
            ?.takeIf(SettingsHttpUrlValidator::isHttpUrl)
    }
}
