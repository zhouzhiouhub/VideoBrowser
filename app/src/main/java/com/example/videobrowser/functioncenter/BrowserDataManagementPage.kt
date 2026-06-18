package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 BrowserDataManagementPage 可以拆开理解为“Browser Data Management Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.webkit.CookieManager
import android.webkit.WebStorage
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.utils.UrlUtils

class BrowserDataManagementPage(
    private val host: FunctionCenterPageHost,
    private val browserManager: () -> BrowserManager,
    private val browserManagers: () -> List<BrowserManager>,
    private val savedPageRepository: SavedPageRepository,
    private val downloadRecordRepository: DownloadRecordRepository,
    private val currentActionableUrl: () -> String?,
    private val showBookmarkList: () -> Unit,
    private val showHistoryList: () -> Unit,
    private val showDownloadList: () -> Unit,
    private val exportBookmarks: () -> Unit,
    private val importBookmarks: () -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity
    private val clearActions = BrowserDataClearActions(
        context = activity,
        browserManagers = browserManagers,
        savedPageRepository = savedPageRepository,
        downloadRecordRepository = downloadRecordRepository
    )
    private val dialogController = BrowserDataManagementDialogController(
        activity = activity,
        clearActions = clearActions,
        reloadBrowser = { browserManager().reload() }
    )

    /**
     * 函数 `showBookmarkData`：控制 `show Bookmark Data` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     */
    fun showBookmarkData(replaceCurrent: Boolean = false) {
        val bookmarkCount = savedPageRepository.bookmarks().size
        host.showPage(
            title = activity.getString(R.string.title_bookmark_data_management),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.title_bookmarks),
                    summary = activity.getString(R.string.bookmark_record_count, bookmarkCount)
                )
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_show_bookmarks),
                    summary = activity.getString(R.string.action_show_bookmarks_summary),
                    enabled = bookmarkCount > 0
                ) {
                    showBookmarkList()
                }
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_export_bookmarks),
                    summary = activity.getString(R.string.action_export_bookmarks_summary),
                    enabled = bookmarkCount > 0
                ) {
                    exportBookmarks()
                }
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_import_bookmarks),
                    summary = activity.getString(R.string.action_import_bookmarks_summary)
                ) {
                    importBookmarks()
                }
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_bookmarks_summary),
                    enabled = bookmarkCount > 0
                ) {
                    dialogController.showClearBookmarksDialog {
                        showBookmarkData(replaceCurrent = true)
                    }
                }
            }

            if (bookmarkCount == 0) {
                host.addEmptyState(content, activity.getString(R.string.toast_bookmarks_empty))
            }
        }
    }

    /**
     * 函数 `showDownloadData`：控制 `show Download Data` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     */
    fun showDownloadData(replaceCurrent: Boolean = false) {
        val downloadCount = downloadRecordRepository.records().size
        host.showPage(
            title = activity.getString(R.string.title_download_data_management),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.title_downloads),
                    summary = activity.getString(R.string.download_record_count, downloadCount)
                )
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.title_downloads),
                    summary = activity.getString(R.string.action_show_downloads_summary),
                    enabled = downloadCount > 0
                ) {
                    showDownloadList()
                }
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_download_records_summary),
                    enabled = downloadCount > 0
                ) {
                    dialogController.showClearDownloadDataDialog {
                        showDownloadData(replaceCurrent = true)
                    }
                }
            }

            if (downloadCount == 0) {
                host.addEmptyState(content, activity.getString(R.string.dialog_download_records_empty))
            }
        }
    }

    /**
     * 函数 `showBrowsingHistoryData`：控制 `show Browsing History Data` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     */
    fun showBrowsingHistoryData(replaceCurrent: Boolean = false) {
        val historyCount = savedPageRepository.history().size
        host.showPage(
            title = activity.getString(R.string.title_history_data_management),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.title_history),
                    summary = activity.getString(R.string.history_record_count, historyCount)
                )
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_show_history),
                    summary = activity.getString(R.string.action_show_history_summary),
                    enabled = historyCount > 0
                ) {
                    showHistoryList()
                }
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_history_range_summary),
                    enabled = historyCount > 0
                ) {
                    dialogController.showClearHistoryRangeDialog {
                        showBrowsingHistoryData(replaceCurrent = true)
                    }
                }
            }

            if (historyCount == 0) {
                host.addEmptyState(content, activity.getString(R.string.toast_history_empty))
            }
        }
    }

    /**
     * 函数 `showCookies`：控制 `show Cookies` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     */
    fun showCookies(replaceCurrent: Boolean = false) {
        val pageUrl = currentActionableUrl()
        val cookies = BrowserCookieParser.parse(
            pageUrl?.let { url -> CookieManager.getInstance().getCookie(url) }
        )

        host.showPage(
            title = activity.getString(R.string.title_cookie_management),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (pageUrl == null) {
                host.addEmptyState(content, activity.getString(R.string.dialog_cookie_management_no_site))
                return@showPage
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.function_center_site_host),
                    summary = UrlUtils.displayUrl(pageUrl)
                )
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_all_cookies_summary)
                ) {
                    dialogController.showClearAllCookiesDialog {
                        showCookies(replaceCurrent = true)
                    }
                }
            }

            if (cookies.isEmpty()) {
                host.addEmptyState(content, activity.getString(R.string.dialog_cookie_management_empty))
            } else {
                host.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_records)
                ) { section ->
                    cookies.forEach { cookie ->
                        host.addActionRow(
                            parent = section,
                            title = cookie.name,
                            summary = ""
                        ) {
                            dialogController.showRemoveCookieDialog(pageUrl, cookie.name) {
                                showCookies(replaceCurrent = true)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 函数 `showCache`：控制 `show Cache` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     */
    fun showCache(replaceCurrent: Boolean = false) {
        host.showPage(
            title = activity.getString(R.string.title_cache_management),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_cache_summary)
                ) {
                    dialogController.showClearCacheDialog {
                        showCache(replaceCurrent = true)
                    }
                }
            }
        }
    }

    /**
     * 函数 `showSiteData`：控制 `show Site Data` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     * @param query 参数类型为 `String?`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     */
    fun showSiteData(replaceCurrent: Boolean = false, query: String? = null) {
        WebStorage.getInstance().getOrigins { origins ->
            activity.runOnUiThread {
                val siteDataOrigins = origins
                    ?.values
                    ?.filterIsInstance<WebStorage.Origin>()
                    ?.sortedBy { origin -> origin.origin }
                    ?: emptyList()
                showSiteDataOrigins(siteDataOrigins, replaceCurrent, query)
            }
        }
    }

    /**
     * 函数 `showSiteDataOrigins`：控制 `show Site Data Origins` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param origins 参数类型为 `List<WebStorage.Origin>`，表示函数执行 `origins` 相关逻辑时需要读取或处理的输入。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     * @param query 参数类型为 `String?`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     */
    private fun showSiteDataOrigins(
        origins: List<WebStorage.Origin>,
        replaceCurrent: Boolean,
        query: String?
    ) {
        val filteredOrigins = origins.filter { origin ->
            BrowserSiteDataOriginSearch.matches(origin.origin, query)
        }
        host.showPage(
            title = activity.getString(R.string.title_site_data_management),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (origins.isNotEmpty()) {
                host.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_actions)
                ) { section ->
                    host.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_search_site_data),
                        summary = currentSiteDataSearchSummary(query)
                    ) {
                        dialogController.showSiteDataSearchDialog(query) { searchQuery ->
                            showSiteData(
                                replaceCurrent = true,
                                query = searchQuery
                            )
                        }
                    }
                    if (!query.isNullOrBlank()) {
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_clear_search),
                            summary = query
                        ) {
                            showSiteData(replaceCurrent = true)
                        }
                    }
                    host.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_clear),
                        summary = activity.getString(R.string.action_clear_site_data_summary)
                    ) {
                        dialogController.showClearSiteDataDialog {
                            showSiteData(replaceCurrent = true)
                        }
                    }
                }
            }

            if (origins.isEmpty()) {
                host.addEmptyState(content, activity.getString(R.string.dialog_site_data_empty))
            } else {
                host.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_records)
                ) { section ->
                    if (filteredOrigins.isEmpty()) {
                        host.addEmptyState(section, activity.getString(R.string.dialog_site_data_search_empty))
                        return@addFunctionSection
                    }
                    filteredOrigins.forEach { origin ->
                        host.addActionRow(
                            parent = section,
                            title = origin.origin,
                            summary = activity.getString(
                                R.string.site_data_usage_summary,
                                BrowserDataDisplayFormatter.siteDataUsageSummary(origin.usage)
                            )
                        ) {
                            dialogController.showRemoveSiteDataDialog(origin.origin) {
                                showSiteData(replaceCurrent = true, query = query)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 函数 `currentSiteDataSearchSummary`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param query 参数类型为 `String?`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun currentSiteDataSearchSummary(query: String?): String {
        return query
            ?.takeIf { it.isNotBlank() }
            ?: activity.getString(R.string.action_search_site_data_summary)
    }

}
