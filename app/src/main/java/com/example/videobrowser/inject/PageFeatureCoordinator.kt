package com.example.videobrowser.inject

import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager

class PageFeatureCoordinator(
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val jsInjector: JsInjector,
    private val currentSiteHost: () -> String?,
    private val currentPageUrl: () -> String?
) {
    fun isAdBlockEnabled(): Boolean {
        return settingsManager.isAdBlockEnabled()
    }

    fun isCurrentSiteAdBlockDisabled(): Boolean {
        return settingsManager.isAdBlockDisabledForSite(currentSiteHost())
    }

    fun isJsInjectionEnabled(): Boolean {
        return settingsManager.isJsInjectionEnabled()
    }

    fun isCurrentSiteJsInjectionDisabled(): Boolean {
        return settingsManager.isJsInjectionDisabledForSite(currentSiteHost())
    }

    fun isPageCleanupEnabled(): Boolean {
        return settingsManager.isDomAdBlockEnabled()
    }

    fun isCurrentSitePageCleanupDisabled(): Boolean {
        return settingsManager.isDomAdBlockDisabledForSite(currentSiteHost())
    }

    fun isVideoEnhancementEnabled(): Boolean {
        return settingsManager.isVideoEnhancementEnabled()
    }

    fun isCurrentSiteVideoEnhancementDisabled(): Boolean {
        return settingsManager.isVideoEnhancementDisabledForSite(currentSiteHost())
    }

    fun injectPageFeatures() {
        jsInjector.inject(
            PageFeatureConfig(
                jsInjectionEnabled = isJsInjectionEnabled() && !isCurrentSiteJsInjectionDisabled(),
                cleanupEnabled = isPageCleanupEnabled() && !isCurrentSitePageCleanupDisabled(),
                videoEnabled = isVideoEnhancementEnabled() && !isCurrentSiteVideoEnhancementDisabled(),
                userCssSelectors = settingsManager.userElementHideSelectorsForSite(currentSiteHost())
            ),
            pageUrl = currentPageUrl() ?: browserManager().currentUrl()
        )
    }
}
