package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 BrowserDataManagementPage 可以拆开理解为“Browser Data Management Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.app.DownloadManager
import android.content.Context
import android.text.InputType
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.download.DownloadRecordCleaner
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
                    showClearBookmarksDialog()
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
                    showClearDownloadDataDialog()
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
                    showClearHistoryRangeDialog()
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
                    showClearAllCookiesDialog()
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
                            showRemoveCookieDialog(pageUrl, cookie.name)
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
                    showClearCacheDialog()
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
                        showSiteDataSearchDialog(query)
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
                        showClearSiteDataDialog()
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
                            showRemoveSiteDataDialog(origin.origin, query)
                        }
                    }
                }
            }
        }
    }

    /**
     * 函数 `showSiteDataSearchDialog`：控制 `show Site Data Search Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param currentQuery 参数类型为 `String?`，表示函数执行 `currentQuery` 相关逻辑时需要读取或处理的输入。
     */
    private fun showSiteDataSearchDialog(currentQuery: String?) {
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setSingleLine(true)
            hint = activity.getString(R.string.hint_site_data_search)
            setText(currentQuery.orEmpty())
            setSelection(text?.length ?: 0)
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_search_site_data)
            .setView(input)
            .setPositiveButton(R.string.action_search_site_data) { _, _ ->
                showSiteData(
                    replaceCurrent = true,
                    query = input.text?.toString()?.trim().orEmpty()
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showRemoveCookieDialog`：控制 `show Remove Cookie Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param cookieName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    private fun showRemoveCookieDialog(pageUrl: String, cookieName: String) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_cookie)
            .setMessage(activity.getString(R.string.dialog_remove_cookie_message, cookieName))
            .setPositiveButton(R.string.action_remove) { _, _ ->
                removeCookie(pageUrl, cookieName)
                Toast.makeText(activity, R.string.toast_cookie_removed, Toast.LENGTH_SHORT).show()
                browserManager().reload()
                showCookies(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showClearAllCookiesDialog`：控制 `show Clear All Cookies Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearAllCookiesDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_all_cookies_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                CookieManager.getInstance().apply {
                    removeAllCookies(null)
                    flush()
                }
                Toast.makeText(activity, R.string.toast_cookies_cleared, Toast.LENGTH_SHORT).show()
                browserManager().reload()
                showCookies(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showClearCacheDialog`：控制 `show Clear Cache Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearCacheDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_cache_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                browserManagers().forEach { manager -> manager.clearCache() }
                Toast.makeText(activity, R.string.toast_cache_cleared, Toast.LENGTH_SHORT).show()
                showCache(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showClearBookmarksDialog`：控制 `show Clear Bookmarks Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearBookmarksDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_bookmarks_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                savedPageRepository.clear(SavedPageRepository.SavedPageCollection.BOOKMARKS)
                Toast.makeText(activity, R.string.toast_bookmarks_cleared, Toast.LENGTH_SHORT).show()
                showBookmarkData(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showClearDownloadDataDialog`：控制 `show Clear Download Data Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearDownloadDataDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_download_records_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                clearDownloadRecordsAndFiles()
                Toast.makeText(activity, R.string.toast_download_records_cleared, Toast.LENGTH_SHORT).show()
                showDownloadData(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `clearDownloadRecordsAndFiles`：封装 `clear Download Records And Files` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun clearDownloadRecordsAndFiles() {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        DownloadRecordCleaner(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.clearRecordsAndFiles()
    }

    /**
     * 函数 `showClearHistoryRangeDialog`：控制 `show Clear History Range Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearHistoryRangeDialog() {
        val ranges = BrowserHistoryClearRange.entries
        val labels = ranges
            .map { range -> historyClearRangeLabel(range) }
            .toTypedArray()

        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setItems(labels) { _, index ->
                ranges.getOrNull(index)?.let(::showClearHistoryDialog)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showClearHistoryDialog`：控制 `show Clear History Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param range 参数类型为 `BrowserHistoryClearRange`，表示函数执行 `range` 相关逻辑时需要读取或处理的输入。
     */
    private fun showClearHistoryDialog(range: BrowserHistoryClearRange) {
        val rangeLabel = historyClearRangeLabel(range)
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(activity.getString(R.string.dialog_clear_history_range_message, rangeLabel))
            .setPositiveButton(R.string.action_clear) { _, _ ->
                val removedCount = clearHistory(range)
                Toast.makeText(
                    activity,
                    activity.getString(R.string.toast_history_range_cleared, rangeLabel, removedCount),
                    Toast.LENGTH_SHORT
                ).show()
                showBrowsingHistoryData(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `clearHistory`：封装 `clear History` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param range 参数类型为 `BrowserHistoryClearRange`，表示函数执行 `range` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun clearHistory(range: BrowserHistoryClearRange): Int {
        val historyCount = savedPageRepository.history().size
        val cutoffMillis = range.cutoffMillis(System.currentTimeMillis())
        return if (cutoffMillis == null) {
            savedPageRepository.clearHistory()
            historyCount
        } else {
            savedPageRepository.clearHistoryUpdatedSince(cutoffMillis)
        }
    }

    /**
     * 函数 `historyClearRangeLabel`：封装 `history Clear Range Label` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param range 参数类型为 `BrowserHistoryClearRange`，表示函数执行 `range` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun historyClearRangeLabel(range: BrowserHistoryClearRange): String {
        return activity.getString(
            when (range) {
                BrowserHistoryClearRange.LAST_HOUR -> R.string.history_clear_range_last_hour
                BrowserHistoryClearRange.LAST_24_HOURS -> R.string.history_clear_range_last_24_hours
                BrowserHistoryClearRange.LAST_7_DAYS -> R.string.history_clear_range_last_7_days
                BrowserHistoryClearRange.ALL -> R.string.history_clear_range_all
            }
        )
    }

    /**
     * 函数 `showRemoveSiteDataDialog`：控制 `show Remove Site Data Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param origin 参数类型为 `String`，表示函数执行 `origin` 相关逻辑时需要读取或处理的输入。
     * @param query 参数类型为 `String?`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     */
    private fun showRemoveSiteDataDialog(origin: String, query: String?) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_site_data)
            .setMessage(activity.getString(R.string.dialog_remove_site_data_message, origin))
            .setPositiveButton(R.string.action_remove) { _, _ ->
                WebStorage.getInstance().deleteOrigin(origin)
                Toast.makeText(activity, R.string.toast_site_data_removed, Toast.LENGTH_SHORT).show()
                showSiteData(replaceCurrent = true, query = query)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showClearSiteDataDialog`：控制 `show Clear Site Data Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearSiteDataDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_site_data_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                WebStorage.getInstance().deleteAllData()
                Toast.makeText(activity, R.string.toast_site_data_cleared, Toast.LENGTH_SHORT).show()
                showSiteData(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `removeCookie`：封装 `remove Cookie` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param cookieName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    private fun removeCookie(pageUrl: String, cookieName: String) {
        CookieManager.getInstance().apply {
            setCookie(pageUrl, "$cookieName=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT")
            setCookie(pageUrl, "$cookieName=; Max-Age=0; Path=/")
            flush()
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
