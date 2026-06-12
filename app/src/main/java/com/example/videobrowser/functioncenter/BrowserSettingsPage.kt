package com.example.videobrowser.functioncenter

import android.text.InputType
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.search.SearchProviders
import com.example.videobrowser.settings.SettingsManager

class BrowserSettingsPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val isAdBlockEnabled: () -> Boolean,
    private val isSmartNoImageEnabled: () -> Boolean,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isPageCleanupEnabled: () -> Boolean,
    private val isVideoEnhancementEnabled: () -> Boolean,
    private val setPrivateBrowsingEnabled: (Boolean) -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val showBookmarks: () -> Unit,
    private val showBookmarkManager: () -> Unit,
    private val showHistory: () -> Unit,
    private val showHistoryManager: () -> Unit,
    private val showDownloadManager: () -> Unit,
    private val showFileOperationsPage: () -> Unit,
    private val showAdBlockLog: () -> Unit,
    private val showUserWhitelistManager: () -> Unit,
    private val showUserManualRulesManager: () -> Unit,
    private val showRuleSubscriptionsManager: () -> Unit,
    private val showCookieManager: () -> Unit,
    private val showCacheManager: () -> Unit,
    private val showSiteDataManager: () -> Unit,
    private val showRestoreDefaultSettingsPage: () -> Unit,
    private val currentSearchProviderName: () -> String,
    private val selectSearchProvider: (String) -> Boolean,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show() {
        host.showPage(
            title = activity.getString(R.string.title_browser_settings),
            onBack = showRootPage
        ) { content ->
            addBrowserBasicsSection(content)
            addGlobalEnhancementSection(content)
            addToolboxSection(content)
        }
    }

    fun addExpandedBrowserSettings(parent: LinearLayout) {
        addGlobalEnhancementSection(parent)
    }

    fun addExpandedDataManagement(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_data)
        ) { section ->
            addDataManagementActions(section, includeSavedPages = true)
        }
    }

    fun addProfileDataManagement(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_data)
        ) { section ->
            FunctionCenterDataManagementActionCatalog.profileActions()
                .forEach { action -> addDataManagementActionRow(section, action) }
        }
    }

    private fun addToolboxSection(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_toolbox)
        ) { section ->
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_show_bookmarks),
                summary = activity.getString(R.string.action_show_bookmarks_summary)
            ) {
                showBookmarks()
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_show_history),
                summary = activity.getString(R.string.action_show_history_summary)
            ) {
                showHistory()
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_file_operations),
                summary = activity.getString(R.string.action_file_operations_summary)
            ) {
                showFileOperationsPage()
            }
            if (isPrivateBrowsingEnabled()) {
                return@addFunctionSection
            }
            addDataManagementRows(section)
        }
    }

    private fun addDataManagementRows(section: LinearLayout) {
        addDataManagementActions(section, includeSavedPages = false)
    }

    private fun addBrowserBasicsSection(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_browser_basics)
        ) { section ->
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.setting_home_page),
                summary = settingsManager.homeUrl()
            ) {
                showHomeUrlDialog()
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.setting_search_engine),
                summary = currentSearchProviderName()
            ) {
                showSearchEngineDialog()
            }
        }
    }

    private fun showHomeUrlDialog() {
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
            hint = activity.getString(R.string.hint_home_page_url)
            setText(settingsManager.homeUrl())
            setSelection(text?.length ?: 0)
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.setting_home_page)
            .setView(input)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val homeUrl = input.text?.toString().orEmpty()
                if (!settingsManager.isValidHomeUrl(homeUrl)) {
                    Toast.makeText(activity, R.string.toast_home_page_invalid, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                settingsManager.setHomeUrl(homeUrl)
                Toast.makeText(activity, R.string.toast_home_page_updated, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                show()
            }
        }
        dialog.show()
    }

    private fun showSearchEngineDialog() {
        val providers = SearchProviders.defaults
        val selectedIndex = providers
            .indexOfFirst { provider -> provider.id == settingsManager.searchEngineId() }
            .takeIf { index -> index >= 0 }
            ?: 0
        AlertDialog.Builder(activity)
            .setTitle(R.string.setting_search_engine)
            .setSingleChoiceItems(
                providers.map { provider -> provider.name }.toTypedArray(),
                selectedIndex
            ) { dialog, index ->
                val provider = providers[index]
                val toastResId = if (selectSearchProvider(provider.id)) {
                    R.string.toast_search_engine_updated
                } else {
                    R.string.toast_search_engine_invalid
                }
                Toast.makeText(activity, toastResId, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun addDataManagementActions(section: LinearLayout, includeSavedPages: Boolean) {
        FunctionCenterDataManagementActionCatalog.actions(isPrivateBrowsingEnabled())
            .filter { action ->
                includeSavedPages ||
                    action != FunctionCenterDataManagementAction.BOOKMARKS &&
                    action != FunctionCenterDataManagementAction.HISTORY
            }
            .forEachIndexed { index, action ->
                if (index > 0 && action == FunctionCenterDataManagementAction.RESTORE_DEFAULT_SETTINGS) {
                    host.addDivider(section)
                }
                addDataManagementActionRow(section, action)
            }
    }

    private fun addDataManagementActionRow(
        section: LinearLayout,
        action: FunctionCenterDataManagementAction
    ) {
        when (action) {
            FunctionCenterDataManagementAction.AD_BLOCK_LOG -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_show_ad_block_log),
                    summary = activity.getString(R.string.action_show_ad_block_log_summary)
                ) {
                    showAdBlockLog()
                }
            }

            FunctionCenterDataManagementAction.USER_WHITELIST -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_manage_user_whitelist),
                    summary = activity.getString(R.string.action_manage_user_whitelist_summary)
                ) {
                    showUserWhitelistManager()
                }
            }

            FunctionCenterDataManagementAction.USER_MANUAL_RULES -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_manage_user_manual_rules),
                    summary = activity.getString(R.string.action_manage_user_manual_rules_summary)
                ) {
                    showUserManualRulesManager()
                }
            }

            FunctionCenterDataManagementAction.RULE_SUBSCRIPTIONS -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_manage_rule_subscriptions),
                    summary = activity.getString(R.string.action_manage_rule_subscriptions_summary)
                ) {
                    showRuleSubscriptionsManager()
                }
            }

            FunctionCenterDataManagementAction.BOOKMARKS -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.title_bookmarks),
                    summary = activity.getString(R.string.action_manage_bookmarks_summary)
                ) {
                    showBookmarkManager()
                }
            }

            FunctionCenterDataManagementAction.HISTORY -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.title_history),
                    summary = activity.getString(R.string.action_manage_history_summary)
                ) {
                    showHistoryManager()
                }
            }

            FunctionCenterDataManagementAction.DOWNLOADS -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.title_downloads),
                    summary = activity.getString(R.string.action_manage_download_records_summary)
                ) {
                    showDownloadManager()
                }
            }

            FunctionCenterDataManagementAction.COOKIES -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_manage_cookies),
                    summary = activity.getString(R.string.action_manage_cookies_summary)
                ) {
                    showCookieManager()
                }
            }

            FunctionCenterDataManagementAction.CACHE -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_manage_cache),
                    summary = activity.getString(R.string.action_manage_cache_summary)
                ) {
                    showCacheManager()
                }
            }

            FunctionCenterDataManagementAction.SITE_DATA -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_manage_site_data),
                    summary = activity.getString(R.string.action_manage_site_data_summary)
                ) {
                    showSiteDataManager()
                }
            }

            FunctionCenterDataManagementAction.RESTORE_DEFAULT_SETTINGS -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_restore_default_settings),
                    summary = activity.getString(R.string.action_restore_default_settings_summary)
                ) {
                    showRestoreDefaultSettingsPage()
                }
            }
        }
    }

    private fun addGlobalEnhancementSection(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_settings)
        ) { section ->
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_private_browsing),
                summary = activity.getString(R.string.setting_private_browsing_summary),
                checked = isPrivateBrowsingEnabled()
            ) { enabled ->
                setPrivateBrowsingEnabled(enabled)
            }

            if (isPrivateBrowsingEnabled()) {
                return@addFunctionSection
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_ad_block),
                summary = activity.getString(R.string.setting_ad_block_summary),
                checked = isAdBlockEnabled()
            ) { enabled ->
                settingsManager.setAdBlockEnabled(enabled)
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_smart_no_image),
                summary = activity.getString(R.string.setting_smart_no_image_summary),
                checked = isSmartNoImageEnabled()
            ) { enabled ->
                settingsManager.setSmartNoImageEnabled(enabled)
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_third_party_cookies),
                summary = activity.getString(R.string.setting_third_party_cookies_summary),
                checked = settingsManager.areThirdPartyCookiesEnabled()
            ) { enabled ->
                settingsManager.setThirdPartyCookiesEnabled(enabled)
                browserManager().setThirdPartyCookiesEnabled(enabled)
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_js_injection),
                summary = activity.getString(R.string.setting_js_injection_summary),
                checked = isJsInjectionEnabled()
            ) { enabled ->
                settingsManager.setJsInjectionEnabled(enabled)
                browserManager().reload()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_page_cleanup),
                summary = activity.getString(R.string.setting_page_cleanup_summary),
                checked = isPageCleanupEnabled()
            ) { enabled ->
                settingsManager.setDomAdBlockEnabled(enabled)
                injectPageFeatures()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_video_enhancement),
                summary = activity.getString(R.string.setting_video_enhancement_summary),
                checked = isVideoEnhancementEnabled()
            ) { enabled ->
                settingsManager.setVideoEnhancementEnabled(enabled)
                injectPageFeatures()
            }

            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_always_start_videos_from_beginning),
                summary = activity.getString(
                    R.string.setting_always_start_videos_from_beginning_summary
                ),
                checked = settingsManager.alwaysStartVideosFromBeginning()
            ) { enabled ->
                settingsManager.setAlwaysStartVideosFromBeginning(enabled)
            }
        }
    }

}
