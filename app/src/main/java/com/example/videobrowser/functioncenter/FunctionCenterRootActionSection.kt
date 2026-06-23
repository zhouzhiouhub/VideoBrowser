package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.UrlUtils

internal class FunctionCenterRootActionSection(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val isDesktopModeEnabled: () -> Boolean,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val openHomePage: () -> Unit,
    private val shareCurrentUrl: () -> Unit,
    private val saveCurrentPageArchive: () -> Unit,
    private val printCurrentPage: () -> Unit,
    private val toggleCurrentBookmark: () -> Unit,
    private val startElementPicker: () -> Unit,
    private val applyDesktopMode: (Boolean) -> Unit,
    private val showFeatureToggleToast: (String, Boolean) -> Unit,
    private val showBrowserTabs: () -> Unit,
    private val showBookmarks: () -> Unit,
    private val showHistory: () -> Unit,
    private val showPlaybackHistory: () -> Unit,
    private val showDownloads: () -> Unit,
    private val showFileOperationsPage: () -> Unit,
    private val showCurrentSiteSettings: () -> Unit
) {
    private val activity = host.activity

    fun add(
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

        host.contentFactory.addFunctionSection(parent, "") { section ->
            host.gridFactory.addActionGrid(
                section,
                FunctionCenterRootActionCatalog.actions(
                    hasPage = hasPage,
                    hasSite = siteHost != null,
                    isPrivateBrowsing = isPrivateBrowsingEnabled()
                ).map { action ->
                    createAction(action, pageSummary, siteSummary, hasPage)
                }
            )
        }
    }

    private fun createAction(
        action: FunctionCenterRootAction,
        pageSummary: String,
        siteSummary: String,
        hasPage: Boolean
    ): FunctionCenterGridAction {
        return when (action) {
            FunctionCenterRootAction.TABS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_show_tabs),
                    summary = activity.getString(R.string.action_show_tabs_summary),
                    iconResId = R.drawable.ic_tabs_24
                ) {
                    showBrowserTabs()
                }
            }

            FunctionCenterRootAction.HOME -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.setting_home_page),
                    summary = activity.getString(R.string.action_open_home_page_summary),
                    iconResId = R.drawable.ic_home_24,
                    enabled = hasPage
                ) {
                    runPageAction(openHomePage)
                }
            }

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

            FunctionCenterRootAction.SAVE_PAGE_ARCHIVE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_save_page_archive),
                    summary = activity.getString(R.string.action_save_page_archive_summary),
                    iconResId = R.drawable.ic_file_24,
                    enabled = hasPage
                ) {
                    runPageAction(saveCurrentPageArchive)
                }
            }

            FunctionCenterRootAction.PRINT_PAGE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_print_page),
                    summary = activity.getString(R.string.action_print_page_summary),
                    iconResId = R.drawable.ic_print_24,
                    enabled = hasPage
                ) {
                    runPageAction(printCurrentPage)
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
                    showPlaybackHistory()
                }
            }

            FunctionCenterRootAction.DOWNLOADS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.title_downloads),
                    summary = activity.getString(R.string.action_show_downloads_summary),
                    iconResId = R.drawable.ic_download_24
                ) {
                    showDownloads()
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
                    showCurrentSiteSettings()
                }
            }
        }
    }

    private fun runPageAction(action: () -> Unit) {
        action()
        close()
    }

    private fun close(): Boolean {
        return host.close()
    }
}
