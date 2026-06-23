package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager

internal class CurrentSiteFeatureSection(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val currentSiteHost: () -> String?,
    private val isAdBlockEnabled: () -> Boolean,
    private val isSmartNoImageEnabled: () -> Boolean,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isPageCleanupEnabled: () -> Boolean,
    private val isVideoEnhancementEnabled: () -> Boolean,
    private val injectPageFeatures: () -> Unit
) {
    private val activity = host.activity

    fun addRows(section: LinearLayout, siteHost: String?, hasSite: Boolean) {
        addReloadingSwitchRow(
            section = section,
            siteHost = siteHost,
            hasSite = hasSite,
            titleResId = R.string.setting_current_site_ad_block,
            globalEnabled = isAdBlockEnabled(),
            isSiteDisabled = settingsManager::isAdBlockDisabledForSite,
            setSiteDisabled = settingsManager::setAdBlockDisabledForSite
        )
        addReloadingSwitchRow(
            section = section,
            siteHost = siteHost,
            hasSite = hasSite,
            titleResId = R.string.setting_current_site_smart_no_image,
            globalEnabled = isSmartNoImageEnabled(),
            isSiteDisabled = settingsManager::isSmartNoImageDisabledForSite,
            setSiteDisabled = settingsManager::setSmartNoImageDisabledForSite
        )
        addReloadingSwitchRow(
            section = section,
            siteHost = siteHost,
            hasSite = hasSite,
            titleResId = R.string.setting_current_site_js_injection,
            globalEnabled = isJsInjectionEnabled(),
            isSiteDisabled = settingsManager::isJsInjectionDisabledForSite,
            setSiteDisabled = settingsManager::setJsInjectionDisabledForSite
        )
        addInjectedFeatureSwitchRow(
            section = section,
            siteHost = siteHost,
            hasSite = hasSite,
            titleResId = R.string.setting_page_cleanup,
            globalEnabled = isPageCleanupEnabled(),
            isSiteDisabled = settingsManager::isDomAdBlockDisabledForSite,
            setSiteDisabled = settingsManager::setDomAdBlockDisabledForSite
        )
        addInjectedFeatureSwitchRow(
            section = section,
            siteHost = siteHost,
            hasSite = hasSite,
            titleResId = R.string.setting_video_enhancement,
            globalEnabled = isVideoEnhancementEnabled(),
            isSiteDisabled = settingsManager::isVideoEnhancementDisabledForSite,
            setSiteDisabled = settingsManager::setVideoEnhancementDisabledForSite
        )
    }

    fun status(globalEnabled: Boolean, siteDisabled: Boolean): String {
        return when {
            !globalEnabled -> activity.getString(R.string.site_config_disabled_by_global)
            siteDisabled -> activity.getString(R.string.site_config_disabled)
            else -> activity.getString(R.string.site_config_enabled)
        }
    }

    private fun addReloadingSwitchRow(
        section: LinearLayout,
        siteHost: String?,
        hasSite: Boolean,
        titleResId: Int,
        globalEnabled: Boolean,
        isSiteDisabled: (String?) -> Boolean,
        setSiteDisabled: (String?, Boolean) -> Boolean
    ) {
        addFeatureSwitchRow(
            section = section,
            siteHost = siteHost,
            hasSite = hasSite,
            titleResId = titleResId,
            globalEnabled = globalEnabled,
            isSiteDisabled = isSiteDisabled,
            setSiteDisabled = setSiteDisabled,
            onChanged = { browserManager().reload() }
        )
    }

    private fun addInjectedFeatureSwitchRow(
        section: LinearLayout,
        siteHost: String?,
        hasSite: Boolean,
        titleResId: Int,
        globalEnabled: Boolean,
        isSiteDisabled: (String?) -> Boolean,
        setSiteDisabled: (String?, Boolean) -> Boolean
    ) {
        addFeatureSwitchRow(
            section = section,
            siteHost = siteHost,
            hasSite = hasSite,
            titleResId = titleResId,
            globalEnabled = globalEnabled,
            isSiteDisabled = isSiteDisabled,
            setSiteDisabled = setSiteDisabled,
            onChanged = injectPageFeatures
        )
    }

    private fun addFeatureSwitchRow(
        section: LinearLayout,
        siteHost: String?,
        hasSite: Boolean,
        titleResId: Int,
        globalEnabled: Boolean,
        isSiteDisabled: (String?) -> Boolean,
        setSiteDisabled: (String?, Boolean) -> Boolean,
        onChanged: () -> Unit
    ) {
        val title = activity.getString(titleResId)
        host.contentFactory.addSwitchRow(
            parent = section,
            title = title,
            summary = summary(siteHost, globalEnabled),
            checked = globalEnabled && !isSiteDisabled(siteHost),
            enabled = hasSite && globalEnabled
        ) { enabled ->
            val hostName = currentSiteHost() ?: return@addSwitchRow
            setSiteDisabled(hostName, !enabled)
            FeatureToggleToast.showForSite(activity, title, hostName, enabled)
            onChanged()
        }
    }

    private fun summary(siteHost: String?, globalEnabled: Boolean): String {
        return when {
            siteHost == null -> activity.getString(R.string.function_center_site_action_unavailable)
            !globalEnabled -> activity.getString(R.string.setting_disabled_in_browser_settings)
            else -> siteHost
        }
    }
}
