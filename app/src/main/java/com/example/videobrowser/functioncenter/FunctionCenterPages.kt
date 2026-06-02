package com.example.videobrowser.functioncenter

import android.graphics.Color
import android.net.Uri
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.adblock.AdBlockLogAction
import com.example.videobrowser.adblock.AdBlockLogEntry
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.UrlUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FunctionCenterPages(
    private val activity: AppCompatActivity,
    private val functionCenter: FunctionCenterController,
    private val settingsManager: SettingsManager,
    private val browserManager: BrowserManager,
    private val savedPageRepository: SavedPageRepository,
    private val adBlockLogger: AdBlockLogger,
    private val currentSiteHost: () -> String?,
    private val currentActionableUrl: () -> String?,
    private val isDesktopModeEnabled: () -> Boolean,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val isAdBlockEnabled: () -> Boolean,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isPageCleanupEnabled: () -> Boolean,
    private val isVideoEnhancementEnabled: () -> Boolean,
    private val toggleCurrentBookmark: () -> Unit,
    private val copyCurrentUrl: () -> Unit,
    private val shareCurrentUrl: () -> Unit,
    private val openCurrentUrlExternally: () -> Unit,
    private val openCurrentUrlInNativePlayer: () -> Unit,
    private val downloadCurrentUrl: () -> Unit,
    private val clearBrowserData: () -> Unit,
    private val setPrivateBrowsingEnabled: (Boolean) -> Unit,
    private val restoreDefaultSettings: () -> Unit,
    private val showFileOperationsPage: () -> Unit,
    private val startElementPicker: () -> Unit,
    private val applyDesktopMode: (Boolean) -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val loadUrl: (String) -> Unit
) {
    fun showRootPage() {
        val onBack: () -> Unit = { close() }
        showSubPage(
            title = activity.getString(R.string.title_page_tools),
            onBack = onBack
        ) { content ->
            val siteHost = currentSiteHost()
            val pageUrl = currentActionableUrl()
            addCurrentPageActionSection(content, pageUrl, siteHost)
            addFunctionNavigationSection(content, siteHost)
        }
    }

    fun handleBack(): Boolean {
        return functionCenter.handleBack()
    }

    fun close(): Boolean {
        return functionCenter.close()
    }

    private fun showSubPage(
        title: String,
        onBack: () -> Unit = { showRootPage() },
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.showPage(title, onBack, buildContent)
    }

    private fun addFunctionNavigationSection(parent: LinearLayout, siteHost: String?) {
        addFunctionSection(parent, activity.getString(R.string.function_center_section_more)) { section ->
            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_site_settings),
                summary = siteHost ?: activity.getString(R.string.function_center_site_action_unavailable),
                enabled = siteHost != null
            ) {
                showCurrentSiteSettingsPage()
            }
            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_browser_settings),
                summary = activity.getString(R.string.action_browser_settings_summary)
            ) {
                showBrowserSettingsPage()
            }
        }
    }

    private fun addCurrentPageActionSection(
        parent: LinearLayout,
        pageUrl: String?,
        siteHost: String?
    ) {
        val pageSummary = pageUrl
            ?.let(UrlUtils::displayUrl)
            ?: activity.getString(R.string.function_center_page_action_unavailable)
        val hasPage = pageUrl != null
        val bookmarkTitle = if (pageUrl?.let(savedPageRepository::isBookmarked) == true) {
            activity.getString(R.string.action_remove_bookmark)
        } else {
            activity.getString(R.string.action_add_bookmark)
        }

        addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_page_actions)
        ) { section ->
            addActionRow(section, bookmarkTitle, pageSummary, enabled = hasPage) {
                toggleCurrentBookmark()
                showRootPage()
            }
            addActionRow(section, activity.getString(R.string.action_copy_link), pageSummary, enabled = hasPage) {
                copyCurrentUrl()
            }
            addActionRow(section, activity.getString(R.string.action_share_page), pageSummary, enabled = hasPage) {
                shareCurrentUrl()
            }
            addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_desktop_mode),
                summary = activity.getString(R.string.setting_desktop_mode_summary),
                checked = isDesktopModeEnabled(),
                enabled = hasPage
            ) { enabled ->
                settingsManager.setDesktopModeEnabled(enabled)
                applyDesktopMode(true)
            }
            addActionRow(section, activity.getString(R.string.action_open_external), pageSummary, enabled = hasPage) {
                openCurrentUrlExternally()
            }
            addActionRow(
                section,
                activity.getString(R.string.action_open_native_player),
                pageSummary,
                enabled = hasPage
            ) {
                openCurrentUrlInNativePlayer()
            }
            addActionRow(
                section,
                activity.getString(R.string.action_download_current_url),
                pageSummary,
                enabled = hasPage
            ) {
                downloadCurrentUrl()
            }
            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_pick_element),
                summary = siteHost ?: activity.getString(R.string.function_center_site_action_unavailable),
                enabled = siteHost != null
            ) {
                close()
                startElementPicker()
            }
        }
    }

    private fun showCurrentSiteSettingsPage() {
        val siteHost = currentSiteHost()
        showSubPage(activity.getString(R.string.title_current_site)) { content ->
            if (siteHost != null) {
                addFunctionMessage(
                    content,
                    activity.getString(R.string.function_center_current_site, siteHost)
                )
            } else {
                addEmptyState(
                    content,
                    activity.getString(R.string.function_center_site_action_unavailable)
                )
            }
            addCurrentSiteActionSection(content, siteHost)
        }
    }

    private fun addCurrentSiteActionSection(parent: LinearLayout, siteHost: String?) {
        val siteSummary = siteHost ?: activity.getString(R.string.function_center_site_action_unavailable)
        val hasSite = siteHost != null
        val isWhitelisted = siteHost?.let(settingsManager::isUserWhitelistedSite) ?: false

        addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_site_actions)
        ) { section ->
            addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_current_site_ad_block),
                summary = currentSiteFeatureSummary(siteHost, isAdBlockEnabled()),
                checked = isAdBlockEnabled() &&
                    !(siteHost?.let(settingsManager::isAdBlockDisabledForSite) ?: false),
                enabled = hasSite && isAdBlockEnabled()
            ) { enabled ->
                val host = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setAdBlockDisabledForSite(host, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = activity.getString(R.string.setting_current_site_ad_block),
                    host = host
                )
                browserManager.reload()
            }

            addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_current_site_js_injection),
                summary = currentSiteFeatureSummary(siteHost, isJsInjectionEnabled()),
                checked = isJsInjectionEnabled() &&
                    !(siteHost?.let(settingsManager::isJsInjectionDisabledForSite) ?: false),
                enabled = hasSite && isJsInjectionEnabled()
            ) { enabled ->
                val host = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setJsInjectionDisabledForSite(host, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = activity.getString(R.string.setting_current_site_js_injection),
                    host = host
                )
                browserManager.reload()
            }

            addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_page_cleanup),
                summary = currentSiteFeatureSummary(siteHost, isPageCleanupEnabled()),
                checked = isPageCleanupEnabled() &&
                    !(siteHost?.let(settingsManager::isDomAdBlockDisabledForSite) ?: false),
                enabled = hasSite && isPageCleanupEnabled()
            ) { enabled ->
                val host = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setDomAdBlockDisabledForSite(host, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = activity.getString(R.string.setting_page_cleanup),
                    host = host
                )
                injectPageFeatures()
            }

            addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_video_enhancement),
                summary = currentSiteFeatureSummary(siteHost, isVideoEnhancementEnabled()),
                checked = isVideoEnhancementEnabled() &&
                    !(siteHost?.let(settingsManager::isVideoEnhancementDisabledForSite) ?: false),
                enabled = hasSite && isVideoEnhancementEnabled()
            ) { enabled ->
                val host = currentSiteHost() ?: return@addSwitchRow
                settingsManager.setVideoEnhancementDisabledForSite(host, !enabled)
                showCurrentSiteFeatureToast(
                    enabled = enabled,
                    featureName = activity.getString(R.string.setting_video_enhancement),
                    host = host
                )
                injectPageFeatures()
            }

            addDivider(section)

            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_add_site_rule),
                summary = siteSummary,
                enabled = hasSite
            ) {
                close()
                startElementPicker()
            }
            addActionRow(
                parent = section,
                title = activity.getString(
                    if (isWhitelisted) R.string.action_leave_whitelist else R.string.action_join_whitelist
                ),
                summary = siteSummary,
                enabled = hasSite
            ) {
                toggleCurrentSiteWhitelist()
                showCurrentSiteSettingsPage()
            }
            addActionRow(
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

    private fun showCurrentSiteFeatureToast(enabled: Boolean, featureName: String, host: String) {
        Toast.makeText(
            activity,
            activity.getString(
                if (enabled) {
                    R.string.toast_current_site_feature_enabled
                } else {
                    R.string.toast_current_site_feature_disabled
                },
                featureName,
                host
            ),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showCurrentSiteConfigPage() {
        val siteHost = currentSiteHost()
        showSubPage(
            title = activity.getString(R.string.title_site_config),
            onBack = { showCurrentSiteSettingsPage() }
        ) { content ->
            if (siteHost == null) {
                addEmptyState(
                    content,
                    activity.getString(R.string.function_center_site_action_unavailable)
                )
                return@showSubPage
            }

            addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_site_actions)
            ) { section ->
                addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.function_center_site_host),
                    summary = siteHost
                )
                addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_current_site_ad_block),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isAdBlockEnabled(),
                        siteDisabled = settingsManager.isAdBlockDisabledForSite(siteHost)
                    )
                )
                addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_current_site_js_injection),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isJsInjectionEnabled(),
                        siteDisabled = settingsManager.isJsInjectionDisabledForSite(siteHost)
                    )
                )
                addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_page_cleanup),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isPageCleanupEnabled(),
                        siteDisabled = settingsManager.isDomAdBlockDisabledForSite(siteHost)
                    )
                )
                addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.setting_video_enhancement),
                    summary = currentSiteFeatureStatus(
                        globalEnabled = isVideoEnhancementEnabled(),
                        siteDisabled = settingsManager.isVideoEnhancementDisabledForSite(siteHost)
                    )
                )
                addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.action_join_whitelist),
                    summary = if (settingsManager.isUserWhitelistedSite(siteHost)) {
                        activity.getString(R.string.site_config_whitelisted)
                    } else {
                        activity.getString(R.string.site_config_not_whitelisted)
                    }
                )
                addInfoRow(
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

    private fun currentSiteFeatureStatus(globalEnabled: Boolean, siteDisabled: Boolean): String {
        return when {
            !globalEnabled -> activity.getString(R.string.site_config_disabled_by_global)
            siteDisabled -> activity.getString(R.string.site_config_disabled)
            else -> activity.getString(R.string.site_config_enabled)
        }
    }

    private fun showBrowserSettingsPage() {
        showSubPage(activity.getString(R.string.title_browser_settings)) { content ->
            addGlobalEnhancementSection(content)
            addToolboxSection(content)
        }
    }

    private fun addToolboxSection(parent: LinearLayout) {
        addFunctionSection(parent, activity.getString(R.string.function_center_section_toolbox)) { section ->
            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_show_bookmarks),
                summary = activity.getString(R.string.action_show_bookmarks_summary)
            ) {
                showSavedPageList(
                    collection = SavedPageCollection.BOOKMARKS,
                    title = activity.getString(R.string.title_bookmarks),
                    emptyMessage = activity.getString(R.string.toast_bookmarks_empty)
                )
            }
            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_show_history),
                summary = activity.getString(R.string.action_show_history_summary)
            ) {
                showSavedPageList(
                    collection = SavedPageCollection.HISTORY,
                    title = activity.getString(R.string.title_history),
                    emptyMessage = activity.getString(R.string.toast_history_empty)
                )
            }
            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_file_operations),
                summary = activity.getString(R.string.action_file_operations_summary)
            ) {
                showFileOperationsPage()
            }
            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_show_ad_block_log),
                summary = activity.getString(R.string.action_show_ad_block_log_summary)
            ) {
                showAdBlockLog()
            }
            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_manage_user_whitelist),
                summary = activity.getString(R.string.action_manage_user_whitelist_summary)
            ) {
                showUserWhitelistManager()
            }
            addDivider(section)
            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_clear_browser_data),
                summary = activity.getString(R.string.action_clear_browser_data_summary)
            ) {
                clearBrowserData()
            }
            addActionRow(
                parent = section,
                title = activity.getString(R.string.action_restore_default_settings),
                summary = activity.getString(R.string.action_restore_default_settings_summary)
            ) {
                showRestoreDefaultSettingsPage()
            }
        }
    }

    private fun addGlobalEnhancementSection(parent: LinearLayout) {
        addFunctionSection(parent, activity.getString(R.string.function_center_section_settings)) { section ->
            addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_private_browsing),
                summary = activity.getString(R.string.setting_private_browsing_summary),
                checked = isPrivateBrowsingEnabled()
            ) { enabled ->
                setPrivateBrowsingEnabled(enabled)
            }

            addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_ad_block),
                summary = activity.getString(R.string.setting_ad_block_summary),
                checked = isAdBlockEnabled()
            ) { enabled ->
                settingsManager.setAdBlockEnabled(enabled)
                browserManager.reload()
            }

            addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_js_injection),
                summary = activity.getString(R.string.setting_js_injection_summary),
                checked = isJsInjectionEnabled()
            ) { enabled ->
                settingsManager.setJsInjectionEnabled(enabled)
                browserManager.reload()
            }

            addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_page_cleanup),
                summary = activity.getString(R.string.setting_page_cleanup_summary),
                checked = isPageCleanupEnabled()
            ) { enabled ->
                settingsManager.setDomAdBlockEnabled(enabled)
                injectPageFeatures()
            }

            addSwitchRow(
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

    private fun showAdBlockLog() {
        val entries = adBlockLogger.entries()
        if (entries.isEmpty()) {
            Toast.makeText(activity, R.string.toast_ad_block_log_empty, Toast.LENGTH_SHORT).show()
            return
        }

        showSubPage(activity.getString(R.string.title_ad_block_log)) { content ->
            addFunctionSection(content, activity.getString(R.string.function_center_section_actions)) { section ->
                addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_ad_block_log_summary)
                ) {
                    adBlockLogger.clear()
                    Toast.makeText(
                        activity,
                        R.string.toast_ad_block_log_cleared,
                        Toast.LENGTH_SHORT
                    ).show()
                    showRootPage()
                }
            }

            addFunctionSection(content, activity.getString(R.string.function_center_section_records)) { section ->
                entries.forEach { entry ->
                    val host = adBlockLogHost(entry)
                    val source = entry.ruleSource ?: entry.reason.name.lowercase(Locale.US)
                    val rule = entry.ruleId ?: entry.rulePattern ?: entry.reason.name
                    val rowTitle = "${adBlockLogTime(entry)} ${adBlockLogActionLabel(entry)} · $host"
                    val rowSummary = "$source  $rule"
                    val whitelistHost = entry.host?.takeIf { value -> value.isNotBlank() }
                    if (
                        entry.action == AdBlockLogAction.BLOCK &&
                        whitelistHost != null &&
                        !settingsManager.isUserWhitelistedSite(whitelistHost)
                    ) {
                        addActionRow(section, rowTitle, rowSummary) {
                            showAddWhitelistFromLogPage(whitelistHost)
                        }
                    } else {
                        addInfoRow(section, rowTitle, rowSummary)
                    }
                }
            }
        }
    }

    private fun adBlockLogTime(entry: AdBlockLogEntry): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(Date(entry.timestampMillis))
    }

    private fun adBlockLogActionLabel(entry: AdBlockLogEntry): String {
        return when (entry.action) {
            AdBlockLogAction.BLOCK -> activity.getString(R.string.ad_block_log_action_blocked)
            AdBlockLogAction.ALLOW -> activity.getString(R.string.ad_block_log_action_allowed)
        }
    }

    private fun adBlockLogHost(entry: AdBlockLogEntry): String {
        return entry.host ?: Uri.parse(entry.url).host ?: entry.url
    }

    private fun showAddWhitelistFromLogPage(host: String) {
        if (settingsManager.isUserWhitelistedSite(host)) {
            Toast.makeText(
                activity,
                activity.getString(R.string.toast_user_whitelist_already_added, host),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        showSubPage(
            title = activity.getString(R.string.title_add_user_whitelist),
            onBack = { showAdBlockLog() }
        ) { content ->
            addFunctionMessage(
                content,
                activity.getString(R.string.dialog_add_user_whitelist_message, host)
            )
            addFunctionSection(content, activity.getString(R.string.function_center_section_actions)) { section ->
                addFunctionActionButton(section, activity.getString(R.string.action_add)) {
                    settingsManager.setUserWhitelistedSite(host, true)
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.toast_user_whitelist_added, host),
                        Toast.LENGTH_SHORT
                    ).show()
                    browserManager.reload()
                    showAdBlockLog()
                }
            }
        }
    }

    private fun showUserWhitelistManager() {
        val hosts = settingsManager.userWhitelistedSiteHosts().sorted()
        val currentHost = currentSiteHost()

        showSubPage(activity.getString(R.string.title_user_whitelist)) { content ->
            if (currentHost != null && !settingsManager.isUserWhitelistedSite(currentHost)) {
                addFunctionSection(content, activity.getString(R.string.function_center_section_actions)) { section ->
                    addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_add_current_site),
                        summary = currentHost
                    ) {
                        settingsManager.setUserWhitelistedSite(currentHost, true)
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.toast_user_whitelist_added, currentHost),
                            Toast.LENGTH_SHORT
                        ).show()
                        browserManager.reload()
                        showUserWhitelistManager()
                    }
                }
            }

            if (hosts.isEmpty()) {
                addEmptyState(content, activity.getString(R.string.dialog_user_whitelist_empty))
            } else {
                addFunctionSection(content, activity.getString(R.string.function_center_section_sites)) { section ->
                    hosts.forEach { host ->
                        addActionRow(
                            parent = section,
                            title = host,
                            summary = activity.getString(R.string.user_whitelist_host_summary)
                        ) {
                            showRemoveUserWhitelistHostPage(host)
                        }
                    }
                }
            }
        }
    }

    private fun toggleCurrentSiteWhitelist() {
        val host = currentSiteHost() ?: run {
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        val shouldWhitelist = !settingsManager.isUserWhitelistedSite(host)
        settingsManager.setUserWhitelistedSite(host, shouldWhitelist)
        Toast.makeText(
            activity,
            if (shouldWhitelist) {
                activity.getString(R.string.toast_user_whitelist_added, host)
            } else {
                activity.getString(R.string.toast_user_whitelist_removed, host)
            },
            Toast.LENGTH_SHORT
        ).show()
        browserManager.reload()
    }

    private fun showRemoveUserWhitelistHostPage(host: String) {
        showSubPage(
            title = activity.getString(R.string.title_remove_user_whitelist),
            onBack = { showUserWhitelistManager() }
        ) { content ->
            addFunctionMessage(
                content,
                activity.getString(R.string.dialog_remove_user_whitelist_message, host)
            )
            addFunctionSection(content, activity.getString(R.string.function_center_section_actions)) { section ->
                addFunctionActionButton(
                    parent = section,
                    title = activity.getString(R.string.action_remove),
                    backgroundColor = Color.parseColor("#D92D20")
                ) {
                    settingsManager.setUserWhitelistedSite(host, false)
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.toast_user_whitelist_removed, host),
                        Toast.LENGTH_SHORT
                    ).show()
                    browserManager.reload()
                    showUserWhitelistManager()
                }
            }
        }
    }

    private fun showSavedPageList(
        collection: SavedPageCollection,
        title: String,
        emptyMessage: String
    ) {
        val pages = savedPageRepository.pages(collection)
        if (pages.isEmpty()) {
            Toast.makeText(activity, emptyMessage, Toast.LENGTH_SHORT).show()
            return
        }

        showSubPage(title) { content ->
            addFunctionSection(content, activity.getString(R.string.function_center_section_actions)) { section ->
                addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_saved_pages_summary)
                ) {
                    savedPageRepository.clear(collection)
                    Toast.makeText(
                        activity,
                        R.string.toast_saved_pages_cleared,
                        Toast.LENGTH_SHORT
                    ).show()
                    showRootPage()
                }
            }

            addFunctionSection(content, activity.getString(R.string.function_center_section_records)) { section ->
                pages.forEach { page ->
                    addActionRow(
                        parent = section,
                        title = page.title.ifBlank { page.url },
                        summary = UrlUtils.displayUrl(page.url)
                    ) {
                        loadUrl(page.url)
                    }
                }
            }
        }
    }

    private fun showRestoreDefaultSettingsPage() {
        showSubPage(activity.getString(R.string.action_restore_default_settings)) { content ->
            addFunctionMessage(
                content,
                activity.getString(R.string.dialog_restore_default_settings_message)
            )
            addFunctionSection(content, activity.getString(R.string.function_center_section_actions)) { section ->
                addFunctionActionButton(section, activity.getString(R.string.action_restore)) {
                    restoreDefaultSettings()
                }
            }
        }
    }

    private fun addFunctionSection(
        parent: LinearLayout,
        title: String,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.addFunctionSection(parent, title, buildContent)
    }

    private fun addInfoRow(parent: LinearLayout, title: String, summary: String) {
        functionCenter.addInfoRow(parent, title, summary)
    }

    private fun addFunctionMessage(parent: LinearLayout, message: String) {
        functionCenter.addFunctionMessage(parent, message)
    }

    private fun addEmptyState(parent: LinearLayout, message: String) {
        functionCenter.addEmptyState(parent, message)
    }

    private fun addFunctionActionButton(
        parent: LinearLayout,
        title: String,
        backgroundColor: Int = ContextCompat.getColor(activity, R.color.browser_primary),
        onClick: () -> Unit
    ) {
        functionCenter.addFunctionActionButton(parent, title, backgroundColor, onClick)
    }

    private fun addSwitchRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        checked: Boolean,
        enabled: Boolean = true,
        onChanged: (Boolean) -> Unit
    ) {
        functionCenter.addSwitchRow(parent, title, summary, checked, enabled, onChanged)
    }

    private fun addActionRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        functionCenter.addActionRow(parent, title, summary, enabled, onClick)
    }

    private fun addDivider(parent: LinearLayout) {
        functionCenter.addDivider(parent)
    }
}
