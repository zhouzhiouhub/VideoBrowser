package com.example.videobrowser.functioncenter

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.utils.UrlUtils
import java.util.Locale

data class BrowserCookieItem(
    val name: String,
    val valuePreview: String
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
        val value = trimmed.substring(separatorIndex + 1).trim()
        if (name.isEmpty()) {
            return null
        }
        return BrowserCookieItem(
            name = name,
            valuePreview = value.take(MAX_VALUE_PREVIEW_LENGTH)
        )
    }

    private const val MAX_VALUE_PREVIEW_LENGTH = 64
}

class BrowserDataManagementPage(
    private val host: FunctionCenterPageHost,
    private val browserManager: () -> BrowserManager,
    private val browserManagers: () -> List<BrowserManager>,
    private val currentActionableUrl: () -> String?,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun showCookies() {
        val pageUrl = currentActionableUrl()
        val cookies = BrowserCookieParser.parse(
            pageUrl?.let { url -> CookieManager.getInstance().getCookie(url) }
        )

        host.showPage(
            title = activity.getString(R.string.title_cookie_management),
            onBack = showRootPage
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
                            summary = cookie.valuePreview.ifBlank {
                                activity.getString(R.string.cookie_value_empty)
                            }
                        ) {
                            showRemoveCookieDialog(pageUrl, cookie.name)
                        }
                    }
                }
            }
        }
    }

    fun showCache() {
        host.showPage(
            title = activity.getString(R.string.title_cache_management),
            onBack = showRootPage
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
            host.addFunctionMessage(content, activity.getString(R.string.cache_management_item_limit))
        }
    }

    fun showSiteData() {
        WebStorage.getInstance().getOrigins { origins ->
            activity.runOnUiThread {
                val siteDataOrigins = origins
                    ?.values
                    ?.filterIsInstance<WebStorage.Origin>()
                    ?.sortedBy { origin -> origin.origin }
                    ?: emptyList()
                showSiteDataOrigins(siteDataOrigins)
            }
        }
    }

    private fun showSiteDataOrigins(origins: List<WebStorage.Origin>) {
        host.showPage(
            title = activity.getString(R.string.title_site_data_management),
            onBack = showRootPage
        ) { content ->
            if (origins.isNotEmpty()) {
                host.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_actions)
                ) { section ->
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
                    origins.forEach { origin ->
                        host.addActionRow(
                            parent = section,
                            title = origin.origin,
                            summary = activity.getString(
                                R.string.site_data_usage_summary,
                                formatBytes(origin.usage),
                                formatBytes(origin.quota)
                            )
                        ) {
                            showRemoveSiteDataDialog(origin.origin)
                        }
                    }
                }
            }
        }
    }

    private fun showRemoveCookieDialog(pageUrl: String, cookieName: String) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_cookie)
            .setMessage(activity.getString(R.string.dialog_remove_cookie_message, cookieName))
            .setPositiveButton(R.string.action_remove) { _, _ ->
                removeCookie(pageUrl, cookieName)
                Toast.makeText(activity, R.string.toast_cookie_removed, Toast.LENGTH_SHORT).show()
                browserManager().reload()
                showCookies()
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
                showCookies()
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
                showCache()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showRemoveSiteDataDialog(origin: String) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_site_data)
            .setMessage(activity.getString(R.string.dialog_remove_site_data_message, origin))
            .setPositiveButton(R.string.action_remove) { _, _ ->
                WebStorage.getInstance().deleteOrigin(origin)
                Toast.makeText(activity, R.string.toast_site_data_removed, Toast.LENGTH_SHORT).show()
                showSiteData()
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
                showSiteData()
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

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) {
            return activity.getString(R.string.site_data_size_zero)
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
