package com.example.videobrowser.settings

internal object SettingsValueNormalizer {
    fun videoSpeed(speed: Float): Float {
        return if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            SettingsManager.DEFAULT_VIDEO_SPEED
        }
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
