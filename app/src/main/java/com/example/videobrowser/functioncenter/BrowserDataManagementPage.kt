package com.example.videobrowser.functioncenter

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
import java.net.URI
import java.util.Locale

data class BrowserCookieItem(
    val name: String
)

object BrowserCookieParser {
    fun parse(rawCookieHeader: String?): List<BrowserCookieItem> {
        return rawCookieHeader
            ?.split(';')
            ?.mapNotNull(::parseCookiePart)
            ?.distinctBy { cookie -> cookie.name }
            ?: emptyList()
    }

    private fun parseCookiePart(part: String): BrowserCookieItem? {
        val trimmed = part.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val separatorIndex = trimmed.indexOf('=')
        if (separatorIndex <= 0) {
            return null
        }
        val name = trimmed.substring(0, separatorIndex).trim()
        if (name.isEmpty()) {
            return null
        }
        return BrowserCookieItem(name = name)
    }
}

object BrowserDataDisplayFormatter {
    fun siteDataUsageSummary(usageBytes: Long): String {
        return formatBytes(usageBytes)
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) {
            return "0 B"
        }
        val units = arrayOf("B", "KB", "MB", "GB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1024.0 && unitIndex < units.lastIndex) {
            value /= 1024.0
            unitIndex += 1
        }
        return String.format(Locale.US, "%.1f %s", value, units[unitIndex])
    }
}

object BrowserSiteDataOriginSearch {
    fun filterOriginNames(origins: List<String>, query: String?): List<String> {
        return origins.filter { origin -> matches(origin, query) }
    }

    fun matches(origin: String, query: String?): Boolean {
        val terms = queryTerms(query)
        if (terms.isEmpty()) {
            return true
        }
        val searchableText = listOf(origin, originHost(origin))
            .joinToString(" ")
            .lowercase(Locale.ROOT)
        return terms.all { term -> searchableText.contains(term) }
    }

    private fun queryTerms(query: String?): List<String> {
        return query
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.split(Regex("\\s+"))
            ?.filter { term -> term.isNotBlank() }
            ?: emptyList()
    }

    private fun originHost(origin: String): String {
        return runCatching { URI(origin).host }
            .getOrNull()
            .orEmpty()
    }
}

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
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

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
                    summary = activity.getString(R.string.action_clear_history_summary),
                    enabled = historyCount > 0
                ) {
                    showClearHistoryDialog()
                }
            }

            if (historyCount == 0) {
                host.addEmptyState(content, activity.getString(R.string.toast_history_empty))
            }
        }
    }

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

    private fun clearDownloadRecordsAndFiles() {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        DownloadRecordCleaner(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.clearRecordsAndFiles()
    }

    private fun showClearHistoryDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_history_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                savedPageRepository.clearHistory()
                Toast.makeText(activity, R.string.toast_history_cleared, Toast.LENGTH_SHORT).show()
                showBrowsingHistoryData(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

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

    private fun removeCookie(pageUrl: String, cookieName: String) {
        CookieManager.getInstance().apply {
            setCookie(pageUrl, "$cookieName=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT")
            setCookie(pageUrl, "$cookieName=; Max-Age=0; Path=/")
            flush()
        }
    }

    private fun currentSiteDataSearchSummary(query: String?): String {
        return query
            ?.takeIf { it.isNotBlank() }
            ?: activity.getString(R.string.action_search_site_data_summary)
    }

}
