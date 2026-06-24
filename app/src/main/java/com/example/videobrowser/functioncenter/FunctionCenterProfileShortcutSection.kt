package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import com.example.videobrowser.R

internal class FunctionCenterProfileShortcutSection(
    private val host: FunctionCenterPageHost,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val showHistory: () -> Unit,
    private val showPlaybackHistory: () -> Unit,
    private val showBookmarks: () -> Unit,
    private val showDownloads: () -> Unit,
    private val showFileOperationsPage: () -> Unit,
    private val showSearchEngines: () -> Unit,
    private val showUserManualRules: () -> Unit,
    private val showAbout: () -> Unit
) {
    private val activity = host.activity

    fun add(parent: LinearLayout) {
        host.contentFactory.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_toolbox)
        ) { section ->
            host.gridFactory.addActionGrid(
                section,
                FunctionCenterProfileActionCatalog.shortcuts(
                    isPrivateBrowsing = isPrivateBrowsingEnabled()
                ).map(::createAction)
            )
        }
    }

    private fun createAction(action: FunctionCenterProfileAction): FunctionCenterGridAction {
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
                ) { showPlaybackHistory() }
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
                ) { showDownloads() }
            }

            FunctionCenterProfileAction.FILE_OPERATIONS -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_file_operations),
                    summary = activity.getString(R.string.action_file_operations_summary),
                    iconResId = R.drawable.ic_file_24
                ) { showFileOperationsPage() }
            }

            FunctionCenterProfileAction.SEARCH_ENGINE -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_search_engine_short),
                    summary = activity.getString(R.string.action_search_engine_summary),
                    iconResId = R.drawable.ic_search_24
                ) { showSearchEngines() }
            }

            FunctionCenterProfileAction.USER_MANUAL_RULES -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_manage_user_manual_rules_short),
                    summary = activity.getString(R.string.action_manage_user_manual_rules_summary),
                    iconResId = R.drawable.ic_rule_24
                ) { showUserManualRules() }
            }

            FunctionCenterProfileAction.ABOUT -> {
                FunctionCenterGridAction(
                    title = activity.getString(R.string.action_about),
                    summary = activity.getString(R.string.action_about_summary),
                    iconResId = R.drawable.ic_info_24
                ) { showAbout() }
            }
        }
    }
}
