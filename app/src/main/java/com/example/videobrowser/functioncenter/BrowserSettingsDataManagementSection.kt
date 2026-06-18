package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import com.example.videobrowser.R

/**
 * 浏览器设置页的数据管理 section。
 *
 * BrowserSettingsPage 负责页面入口和全局设置，本类只负责把数据管理 action 渲染成行并绑定跳转。
 */
internal class BrowserSettingsDataManagementSection(
    private val host: FunctionCenterPageHost,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val showBookmarkManager: () -> Unit,
    private val showHistoryManager: () -> Unit,
    private val showDownloadManager: () -> Unit,
    private val showAdBlockLog: () -> Unit,
    private val showUserWhitelistManager: () -> Unit,
    private val showUserManualRulesManager: () -> Unit,
    private val showSitePermissionsManager: () -> Unit,
    private val showRuleSubscriptionsManager: () -> Unit,
    private val showCookieManager: () -> Unit,
    private val showCacheManager: () -> Unit,
    private val showSiteDataManager: () -> Unit,
    private val showRestoreDefaultSettingsPage: () -> Unit
) {
    private val activity = host.activity

    fun addActions(section: LinearLayout, includeSavedPages: Boolean) {
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
                addActionRow(section, action)
            }
    }

    fun addProfileActions(section: LinearLayout) {
        FunctionCenterDataManagementActionCatalog.profileActions()
            .forEach { action -> addActionRow(section, action) }
    }

    private fun addActionRow(
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

            FunctionCenterDataManagementAction.SITE_PERMISSIONS -> {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_manage_site_permissions),
                    summary = activity.getString(R.string.action_manage_site_permissions_summary)
                ) {
                    showSitePermissionsManager()
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
}
