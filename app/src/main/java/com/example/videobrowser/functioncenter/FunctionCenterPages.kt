package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.UrlUtils

class FunctionCenterPages(
    activity: AppCompatActivity,
    functionCenter: FunctionCenterController,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val savedPageRepository: SavedPageRepository,
    adBlockLogger: AdBlockLogger,
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
    clearBrowserData: () -> Unit,
    setPrivateBrowsingEnabled: (Boolean) -> Unit,
    restoreDefaultSettings: () -> Unit,
    private val showFileOperationsPage: () -> Unit,
    private val startElementPicker: () -> Unit,
    private val applyDesktopMode: (Boolean) -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val loadUrl: (String) -> Unit
) {
    private val host = FunctionCenterPageHost(activity, functionCenter)
    private val currentSiteSettingsPage = CurrentSiteSettingsPage(
        host = host,
        settingsManager = settingsManager,
        browserManager = browserManager,
        currentSiteHost = currentSiteHost,
        isAdBlockEnabled = isAdBlockEnabled,
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
        isJsInjectionEnabled = isJsInjectionEnabled,
        isPageCleanupEnabled = isPageCleanupEnabled,
        isVideoEnhancementEnabled = isVideoEnhancementEnabled,
        setPrivateBrowsingEnabled = setPrivateBrowsingEnabled,
        injectPageFeatures = injectPageFeatures,
        showBookmarks = ::showBookmarks,
        showHistory = ::showHistory,
        showFileOperationsPage = showFileOperationsPage,
        showAdBlockLog = adBlockLogPage::show,
        showUserWhitelistManager = userWhitelistPage::show,
        clearBrowserData = clearBrowserData,
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
            host.addProfileHeader(
                parent = content,
                title = activity.getString(R.string.function_center_profile_name),
                summary = activity.getString(R.string.function_center_profile_summary)
            ) {
                browserSettingsPage.show()
            }
            addProfileShortcutSection(content)
            addProfileFeatureSection(content)
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
                listOf(
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.title_history),
                        summary = activity.getString(R.string.action_show_history_summary),
                        iconResId = R.drawable.ic_history_24
                    ) { showHistory() },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.title_bookmarks),
                        summary = activity.getString(R.string.action_show_bookmarks_summary),
                        iconResId = R.drawable.ic_star_24
                    ) { showBookmarks() },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_file_operations),
                        summary = activity.getString(R.string.action_file_operations_summary),
                        iconResId = R.drawable.ic_file_24
                    ) { showFileOperationsPage() },
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_show_ad_block_log),
                        summary = activity.getString(R.string.action_show_ad_block_log_summary),
                        iconResId = R.drawable.ic_settings_24,
                        enabled = !isPrivateBrowsingEnabled()
                    ) { adBlockLogPage.show() }
                )
            )
        }
    }

    private fun addProfileFeatureSection(parent: LinearLayout) {
        browserSettingsPage.addExpandedBrowserSettings(parent)
        browserSettingsPage.addExpandedDataManagement(parent)
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
