package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.UrlUtils
import com.example.videobrowser.video.PlaybackHistoryRepository
import com.example.videobrowser.video.PlaybackProgress
import java.io.File

class FunctionCenterPages(
    activity: AppCompatActivity,
    functionCenter: FunctionCenterController,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val browserManagers: () -> List<BrowserManager>,
    private val savedPageRepository: SavedPageRepository,
    private val downloadRecordRepository: DownloadRecordRepository,
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    adBlockLogger: AdBlockLogger,
    filesDir: File,
    private val currentSiteHost: () -> String?,
    private val currentActionableUrl: () -> String?,
    private val isDesktopModeEnabled: () -> Boolean,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val isAdBlockEnabled: () -> Boolean,
    private val isSmartNoImageEnabled: () -> Boolean,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isPageCleanupEnabled: () -> Boolean,
    private val isVideoEnhancementEnabled: () -> Boolean,
    private val toggleCurrentBookmark: () -> Unit,
    private val copyCurrentUrl: () -> Unit,
    private val shareCurrentUrl: () -> Unit,
    private val openCurrentUrlExternally: () -> Unit,
    private val findInPage: () -> Unit,
    private val openCurrentUrlInNativePlayer: () -> Unit,
    private val openPlaybackHistoryItem: (PlaybackProgress) -> Unit,
    private val downloadCurrentUrl: () -> Unit,
    setPrivateBrowsingEnabled: (Boolean) -> Unit,
    restoreDefaultSettings: () -> Unit,
    private val showFileOperationsPage: () -> Unit,
    private val startElementPicker: () -> Unit,
    private val applyDesktopMode: (Boolean) -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val loadUrl: (String) -> Unit,
    private val recreateActivity: () -> Unit
) {
    private val host = FunctionCenterPageHost(activity, functionCenter)
    private val currentSiteSettingsPage = CurrentSiteSettingsPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        currentSiteHost = currentSiteHost,
        isAdBlockEnabled = isAdBlockEnabled,
        isSmartNoImageEnabled = isSmartNoImageEnabled,
        isJsInjectionEnabled = isJsInjectionEnabled,
        isPageCleanupEnabled = isPageCleanupEnabled,
        isVideoEnhancementEnabled = isVideoEnhancementEnabled,
        isPrivateBrowsingEnabled = isPrivateBrowsingEnabled,
        startElementPicker = startElementPicker,
        injectPageFeatures = injectPageFeatures,
        showRootPage = ::showRootPage
    )
    private val savedPagesPage = SavedPagesPage(
        host = host,
        savedPageRepository = savedPageRepository,
        loadUrl = loadUrl,
        showRootPage = ::showRootPage
    )
    private val downloadsPage = DownloadsPage(
        host = host,
        downloadRecordRepository = downloadRecordRepository,
        showRootPage = ::showRootPage
    )
    private val playbackHistoryPage = PlaybackHistoryPage(
        host = host,
        playbackHistoryRepository = playbackHistoryRepository,
        openPlaybackHistoryItem = openPlaybackHistoryItem,
        showRootPage = ::showRootPage
    )
    private val adBlockLogPage = AdBlockLogPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        adBlockLogger = adBlockLogger,
        showRootPage = ::showRootPage
    )
    private val userWhitelistPage = UserWhitelistPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        currentSiteHost = currentSiteHost,
        showRootPage = ::showRootPage
    )
    private val userManualRulesPage = UserManualRulesPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        showRootPage = ::showRootPage
    )
    private val ruleSubscriptionPage = RuleSubscriptionPage(
        host = host,
        filesDir = filesDir,
        onRulesChanged = recreateActivity,
        showRootPage = ::showRootPage
    )
    private val aboutPage = AboutPage(
        host = host,
        showProfilePage = ::showProfilePage
    )
    private val browserDataManagementPage = BrowserDataManagementPage(
        host = host,
        browserManager = browserManager,
        browserManagers = browserManagers,
        currentActionableUrl = currentActionableUrl,
        showRootPage = ::showRootPage
    )
    private val restoreDefaultSettingsPage = RestoreDefaultSettingsPage(
        host = host,
        restoreDefaultSettings = restoreDefaultSettings
    )
    private val browserSettingsPage = BrowserSettingsPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        isPrivateBrowsingEnabled = isPrivateBrowsingEnabled,
        isAdBlockEnabled = isAdBlockEnabled,
        isSmartNoImageEnabled = isSmartNoImageEnabled,
        isJsInjectionEnabled = isJsInjectionEnabled,
        isPageCleanupEnabled = isPageCleanupEnabled,
        isVideoEnhancementEnabled = isVideoEnhancementEnabled,
        setPrivateBrowsingEnabled = setPrivateBrowsingEnabled,
        injectPageFeatures = injectPageFeatures,
        showBookmarks = ::showBookmarks,
        showHistory = ::showHistory,
        showFileOperationsPage = showFileOperationsPage,
        showAdBlockLog = { adBlockLogPage.show() },
        showUserWhitelistManager = { userWhitelistPage.show() },
        showUserManualRulesManager = { userManualRulesPage.show() },
        showRuleSubscriptionsManager = { ruleSubscriptionPage.show() },
        showCookieManager = { browserDataManagementPage.showCookies() },
        showCacheManager = { browserDataManagementPage.showCache() },
        showSiteDataManager = { browserDataManagementPage.showSiteData() },
        showRestoreDefaultSettingsPage = restoreDefaultSettingsPage::show,
        showRootPage = ::showRootPage
    )

    private val activity = host.activity

    fun showRootPage() {
        host.showBottomSheetPage(
            title = activity.getString(R.string.title_page_tools),
            onClose = { close() }
        ) { content ->
            val siteHost = currentSiteHost()
            val pageUrl = currentActionableUrl()
            FunctionCenterRootSheetLayout.blocks().forEach { block ->
                when (block) {
                    FunctionCenterRootSheetBlock.ACTION_GRID -> {
                        addBaiduBrowserActionGrid(content, pageUrl, siteHost)
                    }

                    FunctionCenterRootSheetBlock.HISTORY_PREVIEW -> {
                        host.addHistoryPreview(
                            parent = content,
                            title = activity.getString(R.string.menu_history_title),
                            emptyMessage = activity.getString(R.string.menu_history_empty),
                            pages = savedPageRepository.history(),
                            onOpenPage = { page ->
                                close()
                                loadUrl(page.url)
                            },
                            onShowHistory = ::showHistory
                        )
                    }

                    FunctionCenterRootSheetBlock.EXPANDED_BROWSER_SETTINGS -> {
                        browserSettingsPage.addExpandedBrowserSettings(content)
                    }

                    FunctionCenterRootSheetBlock.EXPANDED_DATA_MANAGEMENT -> {
                        browserSettingsPage.addExpandedDataManagement(content)
                    }
                }
            }
        }
    }

    fun showProfilePage() {
        host.showPage(
            title = activity.getString(R.string.title_profile_page),
            onBack = { close() }
        ) { content ->
            FunctionCenterProfilePageLayout.blocks().forEach { block ->
                when (block) {
                    FunctionCenterProfilePageBlock.PROFILE_HEADER -> {
                        host.addProfileHeader(
                            parent = content,
                            title = activity.getString(R.string.function_center_profile_name),
                            summary = activity.getString(R.string.function_center_profile_summary)
                        ) {
                            browserSettingsPage.show()
                        }
                    }

                    FunctionCenterProfilePageBlock.SHORTCUTS -> addProfileShortcutSection(content)
                    FunctionCenterProfilePageBlock.FEATURES -> addProfileFeatureSection(content)
                }
            }
        }
    }

    fun handleBack(): Boolean {
        return host.handleBack()
    }

    fun close(): Boolean {
        return host.close()
    }

    private fun addFunctionNavigationSection(parent: LinearLayout, siteHost: String?) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_more)
        ) { section ->
            if (!isPrivateBrowsingEnabled()) {
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_site_settings),
                    summary = siteHost ?: activity.getString(R.string.function_center_site_action_unavailable),
                    enabled = siteHost != null
                ) {
                    currentSiteSettingsPage.show()
                }
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_browser_settings),
                summary = activity.getString(R.string.action_browser_settings_summary)
            ) {
                browserSettingsPage.show()
            }
        }
    }

    private fun addBaiduBrowserActionGrid(
        parent: LinearLayout,
        pageUrl: String?,
        siteHost: String?
    ) {
        val pageSummary = pageUrl
            ?.let(UrlUtils::displayUrl)
            ?: activity.getString(R.string.function_center_page_action_unavailable)
        val siteSummary = siteHost
            ?: activity.getString(R.string.function_center_site_action_unavailable)
        val hasPage = pageUrl != null

        host.addFunctionSection(parent, "") { section ->
            host.addActionGrid(
                section,
                FunctionCenterRootActionCatalog.actions(
                    hasPage = hasPage,
                    hasSite = siteHost != null,
                    isPrivateBrowsing = isPrivateBrowsingEnabled()
                ).map { action ->
                    createRootGridAction(action, pageSummary, siteSummary, hasPage)
                }
            )
        }
    }

    private fun createRootGridAction(
        action: FunctionCenterRootAction,
        pageSummary: String,
        siteSummary: String,
        hasPage: Boolean
    ): FunctionCenterGridAction {
        return when (action) {
            FunctionCenterRootAction.SHARE_PAGE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_share_page),
                    summary = activity.getString(R.string.action_share_page_summary),
                    iconResId = R.drawable.ic_share_24,
                    enabled = hasPage
                ) {
                    runPageAction(shareCurrentUrl)
                }
            }

            FunctionCenterRootAction.BOOKMARKS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_bookmarks),
                    summary = activity.getString(R.string.action_show_bookmarks_summary),
                    iconResId = R.drawable.ic_star_24
                ) {
                    showBookmarks()
                }
            }

            FunctionCenterRootAction.HISTORY -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_history),
                    summary = activity.getString(R.string.action_show_history_summary),
                    iconResId = R.drawable.ic_history_24
                ) {
                    showHistory()
                }
            }

            FunctionCenterRootAction.PLAYBACK_HISTORY -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_playback_history),
                    summary = activity.getString(R.string.action_show_playback_history_summary),
                    iconResId = R.drawable.ic_history_24
                ) {
                    playbackHistoryPage.show()
                }
            }

            FunctionCenterRootAction.DOWNLOADS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_downloads),
                    summary = activity.getString(R.string.action_show_downloads_summary),
                    iconResId = R.drawable.ic_download_24
                ) {
                    downloadsPage.show()
                }
            }

            FunctionCenterRootAction.FILE_OPERATIONS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_file_operations),
                    summary = activity.getString(R.string.action_file_operations_summary),
                    iconResId = R.drawable.ic_file_24
                ) {
                    showFileOperationsPage()
                }
            }

            FunctionCenterRootAction.REFRESH -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_refresh),
                    summary = pageSummary,
                    iconResId = R.drawable.ic_refresh_24,
                    enabled = hasPage
                ) {
                    browserManager().reload()
                    close()
                }
            }

            FunctionCenterRootAction.DESKTOP_MODE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.setting_desktop_mode),
                    summary = activity.getString(R.string.setting_desktop_mode_summary),
                    iconResId = R.drawable.ic_tabs_24,
                    enabled = hasPage
                ) {
                    val enabled = !isDesktopModeEnabled()
                    settingsManager.setDesktopModeEnabled(enabled)
                    applyDesktopMode(true)
                    showFeatureToggleToast(activity.getString(R.string.setting_desktop_mode), enabled)
                    close()
                }
            }

            FunctionCenterRootAction.ADD_BOOKMARK -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_add_bookmark),
                    summary = pageSummary,
                    iconResId = R.drawable.ic_star_filled_24,
                    enabled = hasPage
                ) {
                    runPageAction(toggleCurrentBookmark)
                }
            }

            FunctionCenterRootAction.PICK_ELEMENT -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_pick_element),
                    summary = siteSummary,
                    iconResId = R.drawable.ic_search_24,
                    enabled = hasPage
                ) {
                    close()
                    startElementPicker()
                }
            }

            FunctionCenterRootAction.MORE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.function_center_section_more),
                    summary = siteSummary,
                    iconResId = R.drawable.ic_more_vert_24
                ) {
                    currentSiteSettingsPage.show()
                }
            }
        }
    }

    private fun addProfileShortcutSection(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_toolbox)
        ) { section ->
            host.addActionGrid(
                section,
                FunctionCenterProfileActionCatalog.shortcuts(
                    isPrivateBrowsing = isPrivateBrowsingEnabled()
                ).map(::createProfileGridAction)
            )
        }
    }

    private fun createProfileGridAction(action: FunctionCenterProfileAction): FunctionCenterGridAction {
        return when (action) {
            FunctionCenterProfileAction.HISTORY -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_history),
                    summary = activity.getString(R.string.action_show_history_summary),
                    iconResId = R.drawable.ic_history_24
                ) { showHistory() }
            }

            FunctionCenterProfileAction.PLAYBACK_HISTORY -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_playback_history),
                    summary = activity.getString(R.string.action_show_playback_history_summary),
                    iconResId = R.drawable.ic_history_24
                ) { playbackHistoryPage.show() }
            }

            FunctionCenterProfileAction.BOOKMARKS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_bookmarks),
                    summary = activity.getString(R.string.action_show_bookmarks_summary),
                    iconResId = R.drawable.ic_star_24
                ) { showBookmarks() }
            }

            FunctionCenterProfileAction.DOWNLOADS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_downloads),
                    summary = activity.getString(R.string.action_show_downloads_summary),
                    iconResId = R.drawable.ic_download_24
                ) { downloadsPage.show() }
            }

            FunctionCenterProfileAction.FILE_OPERATIONS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_file_operations),
                    summary = activity.getString(R.string.action_file_operations_summary),
                    iconResId = R.drawable.ic_file_24
                ) { showFileOperationsPage() }
            }

            FunctionCenterProfileAction.USER_MANUAL_RULES -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_manage_user_manual_rules_short),
                    summary = activity.getString(R.string.action_manage_user_manual_rules_summary),
                    iconResId = R.drawable.ic_rule_24
                ) { userManualRulesPage.show() }
            }

            FunctionCenterProfileAction.ABOUT -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_about),
                    summary = activity.getString(R.string.action_about_summary),
                    iconResId = R.drawable.ic_info_24
                ) { aboutPage.show() }
            }
        }
    }

    private fun addProfileFeatureSection(parent: LinearLayout) {
        browserSettingsPage.addExpandedBrowserSettings(parent)
        browserSettingsPage.addProfileDataManagement(parent)
    }

    private fun addCurrentPageActionSection(
        parent: LinearLayout,
        pageUrl: String?,
        siteHost: String?
    ) {
        val pageSummary = pageUrl
            ?.let(UrlUtils::displayUrl)
            ?: activity.getString(R.string.function_center_page_action_unavailable)
        val siteSummary = siteHost
            ?: activity.getString(R.string.function_center_site_action_unavailable)
        val hasPage = pageUrl != null
        val bookmarkTitle = if (pageUrl?.let(savedPageRepository::isBookmarked) == true) {
            activity.getString(R.string.action_remove_bookmark)
        } else {
            activity.getString(R.string.action_add_bookmark)
        }

        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_page_actions)
        ) { section ->
            val actions = mutableListOf(
                    FunctionCenterGridAction(
                        title = bookmarkTitle,
                        summary = pageSummary,
                        iconResId = R.drawable.ic_star_24,
                        enabled = hasPage
                    ) {
                        runPageAction(toggleCurrentBookmark)
                    },
            )
            if (!isPrivateBrowsingEnabled()) {
                actions.add(
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_pick_element),
                        summary = siteSummary,
                        iconResId = R.drawable.ic_search_24,
                        enabled = siteHost != null
                    ) {
                        close()
                        startElementPicker()
                    },
                )
            }
            actions.addAll(
                listOf(
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_find_in_page),
                        summary = activity.getString(R.string.action_find_in_page_summary),
                        iconResId = R.drawable.ic_search_24,
                        enabled = hasPage
                    ) {
                        runPageAction(findInPage)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_copy_link),
                        summary = activity.getString(R.string.action_copy_link_summary),
                        iconResId = R.drawable.ic_file_24,
                        enabled = hasPage
                    ) {
                        runPageAction(copyCurrentUrl)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_share_page),
                        summary = activity.getString(R.string.action_share_page_summary),
                        iconResId = R.drawable.ic_share_24,
                        enabled = hasPage
                    ) {
                        runPageAction(shareCurrentUrl)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_open_external),
                        summary = activity.getString(R.string.action_open_external_summary),
                        iconResId = R.drawable.ic_tabs_24,
                        enabled = hasPage
                    ) {
                        runPageAction(openCurrentUrlExternally)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_download_current_url),
                        summary = activity.getString(R.string.action_download_current_url_summary),
                        iconResId = R.drawable.ic_download_24,
                        enabled = hasPage
                    ) {
                        runPageAction(downloadCurrentUrl)
                    },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_open_native_player),
                        summary = activity.getString(R.string.action_open_native_player_summary),
                        iconResId = R.drawable.ic_wenxin_wave_24,
                        enabled = hasPage
                    ) {
                        runPageAction(openCurrentUrlInNativePlayer)
                    }
                )
            )
            host.addActionGrid(section, actions)
            if (isPrivateBrowsingEnabled()) {
                return@addFunctionSection
            }
            host.addDivider(section)
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_desktop_mode),
                summary = activity.getString(R.string.setting_desktop_mode_summary),
                checked = isDesktopModeEnabled(),
                enabled = hasPage
            ) { enabled ->
                settingsManager.setDesktopModeEnabled(enabled)
                applyDesktopMode(true)
            }
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_smart_no_image),
                summary = activity.getString(R.string.setting_smart_no_image_summary),
                checked = isSmartNoImageEnabled(),
                enabled = hasPage
            ) { enabled ->
                settingsManager.setSmartNoImageEnabled(enabled)
                browserManager().reload()
                showFeatureToggleToast(activity.getString(R.string.setting_smart_no_image), enabled)
            }
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_page_cleanup),
                summary = activity.getString(R.string.setting_page_cleanup_summary),
                checked = isPageCleanupEnabled(),
                enabled = hasPage
            ) { enabled ->
                settingsManager.setDomAdBlockEnabled(enabled)
                injectPageFeatures()
                showFeatureToggleToast(activity.getString(R.string.setting_page_cleanup), enabled)
            }
            host.addSwitchRow(
                parent = section,
                title = activity.getString(R.string.setting_video_enhancement),
                summary = activity.getString(R.string.setting_video_enhancement_summary),
                checked = isVideoEnhancementEnabled(),
                enabled = hasPage
            ) { enabled ->
                settingsManager.setVideoEnhancementEnabled(enabled)
                injectPageFeatures()
                showFeatureToggleToast(activity.getString(R.string.setting_video_enhancement), enabled)
            }
        }
    }

    private fun runPageAction(action: () -> Unit) {
        action()
        close()
    }

    private fun showFeatureToggleToast(featureName: String, enabled: Boolean) {
        Toast.makeText(
            activity,
            activity.getString(
                if (enabled) R.string.toast_feature_enabled else R.string.toast_feature_disabled,
                featureName
            ),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showBookmarks() {
        savedPagesPage.show(
            collection = SavedPageCollection.BOOKMARKS,
            title = activity.getString(R.string.title_bookmarks),
            emptyMessage = activity.getString(R.string.toast_bookmarks_empty)
        )
    }

    private fun showHistory() {
        savedPagesPage.show(
            collection = SavedPageCollection.HISTORY,
            title = activity.getString(R.string.title_history),
            emptyMessage = activity.getString(R.string.toast_history_empty)
        )
    }
}
