package com.example.videobrowser.settings

import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.WebSchemePolicy

internal object SettingsHttpUrlValidator {
    fun isHttpUrl(url: String): Boolean {
        val uri = SafeUriParser.parse(url) ?: return false
        return WebSchemePolicy.isHttpOrHttpsScheme(uri.scheme) && !uri.host.isNullOrBlank()
    }
}
