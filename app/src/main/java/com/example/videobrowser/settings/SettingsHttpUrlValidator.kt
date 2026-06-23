package com.example.videobrowser.settings

import com.example.videobrowser.utils.WebUrlNormalizer

internal object SettingsHttpUrlValidator {
    fun isHttpUrl(url: String): Boolean {
        return WebUrlNormalizer.isHttpOrHttpsUrl(url)
    }
}
