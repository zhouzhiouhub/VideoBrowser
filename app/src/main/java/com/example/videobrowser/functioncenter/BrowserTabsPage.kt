package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 BrowserTabsPage 可以拆开理解为“Browser Tabs Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
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

    /**
     * 函数 `show`：控制 `show` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     */
    fun show(replaceCurrent: Boolean = false) {
        val tabs = currentTabs()
        host.showPage(
            title = activity.getString(R.string.title_tabs),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_new_tab),
                    summary = activity.getString(R.string.action_show_tabs_summary)
                ) {
                    openNewTab()
                    show(replaceCurrent = true)
                }
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_reopen_closed_tab),
                    summary = activity.getString(R.string.action_reopen_closed_tab_summary),
                    enabled = canReopenClosedTab()
                ) {
                    reopenClosedTab()
                    show(replaceCurrent = true)
                }
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_close_all_tabs),
                    summary = activity.getString(R.string.action_close_all_tabs_summary)
                ) {
                    closeAllTabs()
                    show(replaceCurrent = true)
                }
            }

            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.title_tabs)
            ) { section ->
                tabs.forEach { tab ->
                    val title = BrowserTabDisplayText.title(
                        tab = tab,
                        untitledText = activity.getString(R.string.tab_untitled)
                    )
                    val active = tab.id == activeTabId()
                    host.contentFactory.addActionRow(
                        parent = section,
                        title = title,
                        summary = tabSummary(tab, active),
                        enabled = !active
                    ) {
                        switchTab(tab.id)
                        host.close()
                    }
                    tab.url?.let { url ->
                        host.contentFactory.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_copy_link),
                            summary = UrlUtils.displayUrl(url)
                        ) {
                            copyTabUrl(url)
                        }
                        host.contentFactory.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_share_page),
                            summary = UrlUtils.displayUrl(url)
                        ) {
                            shareTabUrl(url)
                        }
                    }
                    host.contentFactory.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_duplicate_tab),
                        summary = title
                    ) {
                        duplicateTab(tab.id)
                        show(replaceCurrent = true)
                    }
                    if (tabs.size > 1) {
                        host.contentFactory.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_close_other_tabs),
                            summary = title
                        ) {
                            closeOtherTabs(tab.id)
                            show(replaceCurrent = true)
                        }
                        host.contentFactory.addActionRow(
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

    /**
     * 函数 `copyTabUrl`：封装 `copy Tab Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    private fun copyTabUrl(url: String) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(activity.getString(R.string.clipboard_page_url), url)
        )
        Toast.makeText(activity, R.string.toast_link_copied, Toast.LENGTH_SHORT).show()
    }

    /**
     * 函数 `shareTabUrl`：封装 `share Tab Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    private fun shareTabUrl(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.action_share_page)))
    }

    /**
     * 函数 `tabSummary`：封装 `tab Summary` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tab 参数类型为 `BrowserTab`，表示函数执行 `tab` 相关逻辑时需要读取或处理的输入。
     * @param active 参数类型为 `Boolean`，表示函数执行 `active` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun tabSummary(tab: BrowserTab, active: Boolean): String {
        val activeText = activity.getString(R.string.tab_current).takeIf { active }
        val urlText = tab.url?.let(UrlUtils::displayUrl)
        return listOfNotNull(activeText, urlText)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" | ")
            ?: activity.getString(R.string.tab_untitled)
    }
}
