package com.example.videobrowser.browser

import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.HostNameNormalizer

/**
 * Decides whether a page should stay hidden until page feature injection has
 * applied user-owned element hiding rules.
 */
class BrowserPageFeatureVisibilityPolicy(
    private val settingsManager: SettingsManager
) {
    fun shouldHideUntilPageFeaturesInjected(url: String?): Boolean {
        val host = HostNameNormalizer.fromUrl(url) ?: return false
        if (!isJavascriptInjectionEnabledFor(host)) {
            return false
        }
        return settingsManager.userElementHideSelectorsForSite(host).isNotEmpty()
    }

    private fun isJavascriptInjectionEnabledFor(host: String): Boolean {
        return settingsManager.isJsInjectionEnabled() &&
            !settingsManager.isJsInjectionDisabledForSite(host)
    }
}
