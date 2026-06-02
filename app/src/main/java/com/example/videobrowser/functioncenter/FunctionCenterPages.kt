package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
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
    private val browserManager: BrowserManager,
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
    injectPageFeatures: () -> Unit,
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
        restoreDefaultSettings = restoreDefaultSettings,
        showRootPage = ::showRootPage
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
            addCurrentPageActionSection(content, pageUrl, siteHost)
            addFunctionNavigationSection(content, siteHost)
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
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_site_settings),
                summary = siteHost ?: activity.getString(R.string.function_center_site_action_unavailable),
                enabled = siteHost != null
            ) {
                currentSiteSettingsPage.show()
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

        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_page_actions)
        ) { section ->
            host.addActionRow(section, bookmarkTitle, pageSummary, enabled = hasPage) {
                toggleCurrentBookmark()
                showRootPage()
            }
            host.addActionRow(
                section,
                activity.getString(R.string.action_copy_link),
                pageSummary,
                enabled = hasPage
            ) {
                copyCurrentUrl()
            }
            host.addActionRow(
                section,
                activity.getString(R.string.action_share_page),
                pageSummary,
                enabled = hasPage
            ) {
                shareCurrentUrl()
            }
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
            host.addActionRow(
                section,
                activity.getString(R.string.action_open_external),
                pageSummary,
                enabled = hasPage
            ) {
                openCurrentUrlExternally()
            }
            host.addActionRow(
                section,
                activity.getString(R.string.action_open_native_player),
                pageSummary,
                enabled = hasPage
            ) {
                openCurrentUrlInNativePlayer()
            }
            host.addActionRow(
                section,
                activity.getString(R.string.action_download_current_url),
                pageSummary,
                enabled = hasPage
            ) {
                downloadCurrentUrl()
            }
            host.addActionRow(
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
