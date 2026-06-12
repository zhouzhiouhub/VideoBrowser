package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SitePermission
import com.example.videobrowser.settings.SitePermissionDecision

class CurrentSiteSettingsPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val currentSiteHost: () -> String?,
    private val isAdBlockEnabled: () -> Boolean,
    private val isSmartNoImageEnabled: () -> Boolean,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isPageCleanupEnabled: () -> Boolean,
    private val isVideoEnhancementEnabled: () -> Boolean,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val startElementPicker: () -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show(replaceCurrent: Boolean = false) {
        val siteHost = currentSiteHost()
        host.showBottomSheetPage(
            title = activity.getString(R.string.title_current_site),
            onBack = showRootPage,
            onClose = { host.close() },
            replaceCurrent = replaceCurrent
        ) { content ->
            if (siteHost != null) {
                host.addFunctionMessage(
                    content,
                    activity.getString(R.string.function_center_current_site, siteHost)
                )
            } else {
                host.addEmptyState(
                    content,
                    activity.getString(R.string.function_center_site_action_unavailable)
                )
            }
            addCurrentSiteActionSection(content, siteHost)
        }
    }

    private fun addCurrentSiteActionSection(parent: LinearLayout, siteHost: String?) {
        if (isPrivateBrowsingEnabled()) {
            host.addEmptyState(
                parent,
                activity.getString(R.string.function_center_site_action_unavailable)
            )
            return
        }
        val siteSummary = siteHost ?: activity.getString(R.string.function_center_site_action_unavailable)
        val hasSite = siteHost != null
        val isWhitelisted = siteHost?.let(settingsManager::isUserWhitelistedSite) ?: false

        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_site_actions)
        ) { section ->
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_current_site_ad_block),
                summary = currentSiteFeatureSummary(siteHost, isAdBlockEnabled()),
                checked = isAdBlockEnabled() &&
                    !(siteHost?.let(settingsManager::isAdBlockDisabledForSite) ?: false),
                enabled = hasSite && isAdBlockEnabled()
            ) { enabled ->
                val hostName = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setAdBlockDisabledForSite(hostName, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = activity.getString(R.string.setting_current_site_ad_block),
                    hostName = hostName
                )
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_current_site_smart_no_image),
                summary = currentSiteFeatureSummary(siteHost, isSmartNoImageEnabled()),
                checked = isSmartNoImageEnabled() &&
                    !(siteHost?.let(settingsManager::isSmartNoImageDisabledForSite) ?: false),
                enabled = hasSite && isSmartNoImageEnabled()
            ) { enabled ->
                val hostName = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setSmartNoImageDisabledForSite(hostName, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = activity.getString(R.string.setting_current_site_smart_no_image),
                    hostName = hostName
                )
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_current_site_js_injection),
                summary = currentSiteFeatureSummary(siteHost, isJsInjectionEnabled()),
                checked = isJsInjectionEnabled() &&
                    !(siteHost?.let(settingsManager::isJsInjectionDisabledForSite) ?: false),
                enabled = hasSite && isJsInjectionEnabled()
            ) { enabled ->
                val hostName = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setJsInjectionDisabledForSite(hostName, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = activity.getString(R.string.setting_current_site_js_injection),
                    hostName = hostName
                )
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_page_cleanup),
                summary = currentSiteFeatureSummary(siteHost, isPageCleanupEnabled()),
                checked = isPageCleanupEnabled() &&
                    !(siteHost?.let(settingsManager::isDomAdBlockDisabledForSite) ?: false),
                enabled = hasSite && isPageCleanupEnabled()
            ) { enabled ->
                val hostName = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setDomAdBlockDisabledForSite(hostName, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = activity.getString(R.string.setting_page_cleanup),
                    hostName = hostName
                )
                injectPageFeatures()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_video_enhancement),
                summary = currentSiteFeatureSummary(siteHost, isVideoEnhancementEnabled()),
                checked = isVideoEnhancementEnabled() &&
                    !(siteHost?.let(settingsManager::isVideoEnhancementDisabledForSite) ?: false),
                enabled = hasSite && isVideoEnhancementEnabled()
            ) { enabled ->
                val hostName = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setVideoEnhancementDisabledForSite(hostName, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = activity.getString(R.string.setting_video_enhancement),
                    hostName = hostName
                )
                injectPageFeatures()
            }

            host.addDivider(section)

            addSitePermissionRow(section, siteHost, hasSite, SitePermission.CAMERA)
            addSitePermissionRow(section, siteHost, hasSite, SitePermission.MICROPHONE)
            addSitePermissionRow(section, siteHost, hasSite, SitePermission.LOCATION)

            host.addDivider(section)

            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_add_site_rule),
                summary = siteSummary,
                enabled = hasSite
            ) {
                host.close()
                startElementPicker()
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(
                    if (isWhitelisted) R.string.action_leave_whitelist else R.string.action_join_whitelist
                ),
                summary = siteSummary,
                enabled = hasSite
            ) {
                toggleCurrentSiteWhitelist()
                show(replaceCurrent = true)
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_view_site_config),
                summary = siteSummary,
                enabled = hasSite
            ) {
                showCurrentSiteConfigPage()
            }
        }
    }

    private fun currentSiteFeatureSummary(siteHost: String?, globalEnabled: Boolean): String {
        return when {
            siteHost == null -> activity.getString(R.string.function_center_site_action_unavailable)
            !globalEnabled -> activity.getString(R.string.setting_disabled_in_browser_settings)
            else -> siteHost
        }
    }

    private fun showCurrentSiteFeatureToast(
        enabled: Boolean,
        featureName: String,
        hostName: String
    ) {
        Toast.makeText(
            activity,
            activity.getString(
                if (enabled) {
                    R.string.toast_current_site_feature_enabled
                } else {
                    R.string.toast_current_site_feature_disabled
                },
                featureName,
                hostName
            ),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showCurrentSiteConfigPage() {
        val siteHost = currentSiteHost()
        host.showBottomSheetPage(
            title = activity.getString(R.string.title_site_config),
            onBack = { show(replaceCurrent = true) },
            onClose = { host.close() }
        ) { content ->
            if (siteHost == null) {
                host.addEmptyState(
                    content,
                    activity.getString(R.string.function_center_site_action_unavailable)
                )
                return@showBottomSheetPage
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_site_actions)
            ) { section ->
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.function_center_site_host),
                    summary = siteHost
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_current_site_ad_block),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isAdBlockEnabled(),
                        siteDisabled = settingsManager.isAdBlockDisabledForSite(siteHost)
                    )
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_current_site_smart_no_image),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isSmartNoImageEnabled(),
                        siteDisabled = settingsManager.isSmartNoImageDisabledForSite(siteHost)
                    )
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_current_site_js_injection),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isJsInjectionEnabled(),
                        siteDisabled = settingsManager.isJsInjectionDisabledForSite(siteHost)
                    )
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_page_cleanup),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isPageCleanupEnabled(),
                        siteDisabled = settingsManager.isDomAdBlockDisabledForSite(siteHost)
                    )
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_video_enhancement),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isVideoEnhancementEnabled(),
                        siteDisabled = settingsManager.isVideoEnhancementDisabledForSite(siteHost)
                    )
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.action_join_whitelist),
                    summary = if (settingsManager.isUserWhitelistedSite(siteHost)) {
                        activity.getString(R.string.site_config_whitelisted)
                    } else {
                        activity.getString(R.string.site_config_not_whitelisted)
                    }
                )
                host.addInfoRow(
                    parent = section,
                    title = sitePermissionTitle(SitePermission.CAMERA),
                    summary = currentSitePermissionSummary(siteHost, SitePermission.CAMERA)
                )
                host.addInfoRow(
                    parent = section,
                    title = sitePermissionTitle(SitePermission.MICROPHONE),
                    summary = currentSitePermissionSummary(siteHost, SitePermission.MICROPHONE)
                )
                host.addInfoRow(
                    parent = section,
                    title = sitePermissionTitle(SitePermission.LOCATION),
                    summary = currentSitePermissionSummary(siteHost, SitePermission.LOCATION)
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.action_add_site_rule),
                    summary = activity.getString(
                        R.string.site_config_rule_count,
                        settingsManager.userElementHideSelectorsForSite(siteHost).size
                    )
                )
            }
        }
    }

    private fun addSitePermissionRow(
        section: LinearLayout,
        siteHost: String?,
        hasSite: Boolean,
        permission: SitePermission
    ) {
        host.addActionRow(
            parent = section,
            title = sitePermissionTitle(permission),
            summary = currentSitePermissionSummary(siteHost, permission),
            enabled = hasSite
        ) {
            showSitePermissionDialog(permission)
        }
    }

    private fun showSitePermissionDialog(permission: SitePermission) {
        val hostName = currentSiteHost() ?: return
        val decisions = SitePermissionDecision.entries
        val labels = decisions
            .map { decision -> sitePermissionDecisionText(decision) }
            .toTypedArray()
        val checkedIndex = decisions.indexOf(settingsManager.sitePermissionDecision(hostName, permission))

        AlertDialog.Builder(activity)
            .setTitle(sitePermissionTitle(permission))
            .setSingleChoiceItems(labels, checkedIndex) { dialog, index ->
                val decision = decisions[index]
                settingsManager.setSitePermissionDecision(hostName, permission, decision)
                Toast.makeText(
                    activity,
                    activity.getString(
                        R.string.toast_site_permission_updated,
                        sitePermissionTitle(permission),
                        hostName
                    ),
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
                show(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun currentSitePermissionSummary(siteHost: String?, permission: SitePermission): String {
        return siteHost
            ?.let { hostName -> settingsManager.sitePermissionDecision(hostName, permission) }
            ?.let(::sitePermissionDecisionText)
            ?: activity.getString(R.string.function_center_site_action_unavailable)
    }

    private fun sitePermissionTitle(permission: SitePermission): String {
        return activity.getString(
            when (permission) {
                SitePermission.CAMERA -> R.string.setting_site_permission_camera
                SitePermission.MICROPHONE -> R.string.setting_site_permission_microphone
                SitePermission.LOCATION -> R.string.setting_site_permission_location
            }
        )
    }

    private fun sitePermissionDecisionText(decision: SitePermissionDecision): String {
        return activity.getString(
            when (decision) {
                SitePermissionDecision.ASK -> R.string.site_permission_ask
                SitePermissionDecision.ALLOW -> R.string.site_permission_allowed
                SitePermissionDecision.BLOCK -> R.string.site_permission_blocked
            }
        )
    }

    private fun currentSiteFeatureStatus(globalEnabled: Boolean, siteDisabled: Boolean): String {
        return when {
            !globalEnabled -> activity.getString(R.string.site_config_disabled_by_global)
            siteDisabled -> activity.getString(R.string.site_config_disabled)
            else -> activity.getString(R.string.site_config_enabled)
        }
    }

    private fun toggleCurrentSiteWhitelist() {
        val hostName = currentSiteHost() ?: run {
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        val shouldWhitelist = !settingsManager.isUserWhitelistedSite(hostName)
        settingsManager.setUserWhitelistedSite(hostName, shouldWhitelist)
        Toast.makeText(
            activity,
            if (shouldWhitelist) {
                activity.getString(R.string.toast_user_whitelist_added, hostName)
            } else {
                activity.getString(R.string.toast_user_whitelist_removed, hostName)
            },
            Toast.LENGTH_SHORT
        ).show()
        browserManager().reload()
    }
}
