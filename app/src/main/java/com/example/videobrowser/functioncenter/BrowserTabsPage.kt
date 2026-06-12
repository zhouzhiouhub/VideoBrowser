package com.example.videobrowser.functioncenter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserTab
import com.example.videobrowser.utils.UrlUtils

class BrowserTabsPage(
    private val host: FunctionCenterPageHost,
    private val currentTabs: () -> List<BrowserTab>,
    private val activeTabId: () -> Long,
    private val openNewTab: () -> Unit,
    private val canReopenClosedTab: () -> Boolean,
    private val reopenClosedTab: () -> Unit,
    private val switchTab: (Long) -> Unit,
    private val closeTab: (Long) -> Unit,
    private val closeOtherTabs: (Long) -> Unit,
    private val closeAllTabs: () -> Unit,
    private val duplicateTab: (Long) -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show(replaceCurrent: Boolean = false) {
        val tabs = currentTabs()
        host.showPage(
            title = activity.getString(R.string.title_tabs),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_new_tab),
                    summary = activity.getString(R.string.action_show_tabs_summary)
                ) {
                    openNewTab()
                    show(replaceCurrent = true)
                }
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_reopen_closed_tab),
                    summary = activity.getString(R.string.action_reopen_closed_tab_summary),
                    enabled = canReopenClosedTab()
                ) {
                    reopenClosedTab()
                    show(replaceCurrent = true)
                }
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_close_all_tabs),
                    summary = activity.getString(R.string.action_close_all_tabs_summary)
                ) {
                    closeAllTabs()
                    show(replaceCurrent = true)
                }
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.title_tabs)
            ) { section ->
                tabs.forEach { tab ->
                    val title = BrowserTabDisplayText.title(
                        tab = tab,
                        untitledText = activity.getString(R.string.tab_untitled)
                    )
                    val active = tab.id == activeTabId()
                    host.addActionRow(
                        parent = section,
                        title = title,
                        summary = tabSummary(tab, active),
                        enabled = !active
                    ) {
                        switchTab(tab.id)
                        host.close()
                    }
                    tab.url?.let { url ->
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_copy_link),
                            summary = UrlUtils.displayUrl(url)
                        ) {
                            copyTabUrl(url)
                        }
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_share_page),
                            summary = UrlUtils.displayUrl(url)
                        ) {
                            shareTabUrl(url)
                        }
                    }
                    host.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_duplicate_tab),
                        summary = title
                    ) {
                        duplicateTab(tab.id)
                        show(replaceCurrent = true)
                    }
                    if (tabs.size > 1) {
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_close_other_tabs),
                            summary = title
                        ) {
                            closeOtherTabs(tab.id)
                            show(replaceCurrent = true)
                        }
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_close_tab),
                            summary = title
                        ) {
                            closeTab(tab.id)
                            show(replaceCurrent = true)
                        }
                    }
                }
            }
        }
    }

    private fun copyTabUrl(url: String) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(activity.getString(R.string.clipboard_page_url), url)
        )
        Toast.makeText(activity, R.string.toast_link_copied, Toast.LENGTH_SHORT).show()
    }

    private fun shareTabUrl(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.action_share_page)))
    }

    private fun tabSummary(tab: BrowserTab, active: Boolean): String {
        val activeText = activity.getString(R.string.tab_current).takeIf { active }
        val urlText = tab.url?.let(UrlUtils::displayUrl)
        return listOfNotNull(activeText, urlText)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" | ")
            ?: activity.getString(R.string.tab_untitled)
    }
}
