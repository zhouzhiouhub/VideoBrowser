package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager

class BrowserSettingsPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val isAdBlockEnabled: () -> Boolean,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isPageCleanupEnabled: () -> Boolean,
    private val isVideoEnhancementEnabled: () -> Boolean,
    private val setPrivateBrowsingEnabled: (Boolean) -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val showBookmarks: () -> Unit,
    private val showHistory: () -> Unit,
    private val showFileOperationsPage: () -> Unit,
    private val showAdBlockLog: () -> Unit,
    private val showUserWhitelistManager: () -> Unit,
    private val showUserManualRulesManager: () -> Unit,
    private val clearBrowserData: () -> Unit,
    private val showRestoreDefaultSettingsPage: () -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show() {
        host.showPage(
            title = activity.getString(R.string.title_browser_settings),
            onBack = showRootPage
        ) { content ->
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
            if (!isPrivateBrowsingEnabled()) {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_show_ad_block_log),
                    summary = activity.getString(R.string.action_show_ad_block_log_summary)
                ) {
                    showAdBlockLog()
                }
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_manage_user_whitelist),
                    summary = activity.getString(R.string.action_manage_user_whitelist_summary)
                ) {
                    showUserWhitelistManager()
                }
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_manage_user_manual_rules),
                    summary = activity.getString(R.string.action_manage_user_manual_rules_summary)
                ) {
                    showUserManualRulesManager()
                }
                host.addDivider(section)
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_clear_browser_data),
                summary = activity.getString(R.string.action_clear_browser_data_summary)
            ) {
                showClearBrowserDataDialog()
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_restore_default_settings),
                summary = activity.getString(R.string.action_restore_default_settings_summary)
            ) {
                showRestoreDefaultSettingsPage()
            }
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
        host.addActionRow(
            parent = section,
            title = activity.getString(R.string.action_show_ad_block_log),
            summary = activity.getString(R.string.action_show_ad_block_log_summary)
        ) {
            showAdBlockLog()
        }
        host.addActionRow(
            parent = section,
            title = activity.getString(R.string.action_manage_user_whitelist),
            summary = activity.getString(R.string.action_manage_user_whitelist_summary)
        ) {
            showUserWhitelistManager()
        }
        host.addActionRow(
            parent = section,
            title = activity.getString(R.string.action_manage_user_manual_rules),
            summary = activity.getString(R.string.action_manage_user_manual_rules_summary)
        ) {
            showUserManualRulesManager()
        }
        host.addDivider(section)
        host.addActionRow(
            parent = section,
            title = activity.getString(R.string.action_clear_browser_data),
            summary = activity.getString(R.string.action_clear_browser_data_summary)
        ) {
            showClearBrowserDataDialog()
        }
        host.addActionRow(
            parent = section,
            title = activity.getString(R.string.action_restore_default_settings),
            summary = activity.getString(R.string.action_restore_default_settings_summary)
        ) {
            showRestoreDefaultSettingsPage()
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
        }
    }

    private fun showClearBrowserDataDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear_browser_data)
            .setMessage(R.string.dialog_clear_browser_data_message)
            .setPositiveButton(R.string.action_clear) { _, _ -> clearBrowserData() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
