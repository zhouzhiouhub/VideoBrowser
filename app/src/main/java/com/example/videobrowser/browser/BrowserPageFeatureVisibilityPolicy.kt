package com.example.videobrowser.browser

import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.HostNameNormalizer

/**
 * Decides whether a page should stay hidden until page feature injection has
 * applied app-owned cleanup such as search-shell replacement or DOM ad hiding.
 */
class BrowserPageFeatureVisibilityPolicy(
    private val settingsManager: SettingsManager,
    private val isBuiltInSearchResultPage: (String?) -> Boolean
) {
    fun shouldHideUntilPageFeaturesInjected(url: String?): Boolean {
        val host = HostNameNormalizer.fromUrl(url) ?: return false
        if (!isJavascriptInjectionEnabledFor(host)) {
            return false
        }
        return isBuiltInSearchResultPage(url) ||
            isDomCleanupEnabledFor(host) ||
            settingsManager.userElementHideSelectorsForSite(host).isNotEmpty()
    }

    private fun isJavascriptInjectionEnabledFor(host: String): Boolean {
        return settingsManager.isJsInjectionEnabled() &&
            !settingsManager.isJsInjectionDisabledForSite(host)
    }

    private fun isDomCleanupEnabledFor(host: String): Boolean {
        return settingsManager.isDomAdBlockEnabled() &&
            !settingsManager.isDomAdBlockDisabledForSite(host)
    }
}
