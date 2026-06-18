package com.example.videobrowser.functioncenter

import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R

class BrowserDataManagementDialogController(
    private val activity: AppCompatActivity,
    private val clearActions: BrowserDataClearActions,
    private val reloadBrowser: () -> Unit
) {
    fun showSiteDataSearchDialog(currentQuery: String?, onSearch: (String) -> Unit) {
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
                onSearch(input.text?.toString()?.trim().orEmpty())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showRemoveCookieDialog(pageUrl: String, cookieName: String, onRemoved: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_cookie)
            .setMessage(activity.getString(R.string.dialog_remove_cookie_message, cookieName))
            .setPositiveButton(R.string.action_remove) { _, _ ->
                clearActions.removeCookie(pageUrl, cookieName)
                Toast.makeText(activity, R.string.toast_cookie_removed, Toast.LENGTH_SHORT).show()
                reloadBrowser()
                onRemoved()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showClearAllCookiesDialog(onCleared: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_all_cookies_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                clearActions.clearAllCookies()
                Toast.makeText(activity, R.string.toast_cookies_cleared, Toast.LENGTH_SHORT).show()
                reloadBrowser()
                onCleared()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showClearCacheDialog(onCleared: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_cache_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                clearActions.clearCache()
                Toast.makeText(activity, R.string.toast_cache_cleared, Toast.LENGTH_SHORT).show()
                onCleared()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showClearBookmarksDialog(onCleared: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_bookmarks_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                clearActions.clearBookmarks()
                Toast.makeText(activity, R.string.toast_bookmarks_cleared, Toast.LENGTH_SHORT).show()
                onCleared()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showClearDownloadDataDialog(onCleared: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_download_records_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                clearActions.clearDownloadRecordsAndFiles()
                Toast.makeText(activity, R.string.toast_download_records_cleared, Toast.LENGTH_SHORT).show()
                onCleared()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showClearHistoryRangeDialog(onCleared: () -> Unit) {
        val ranges = BrowserHistoryClearRange.entries
        val labels = ranges
            .map { range -> historyClearRangeLabel(range) }
            .toTypedArray()

        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setItems(labels) { _, index ->
                ranges.getOrNull(index)?.let { range -> showClearHistoryDialog(range, onCleared) }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showRemoveSiteDataDialog(origin: String, onRemoved: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_site_data)
            .setMessage(activity.getString(R.string.dialog_remove_site_data_message, origin))
            .setPositiveButton(R.string.action_remove) { _, _ ->
                clearActions.removeSiteData(origin)
                Toast.makeText(activity, R.string.toast_site_data_removed, Toast.LENGTH_SHORT).show()
                onRemoved()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showClearSiteDataDialog(onCleared: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_site_data_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                clearActions.clearSiteData()
                Toast.makeText(activity, R.string.toast_site_data_cleared, Toast.LENGTH_SHORT).show()
                onCleared()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showClearHistoryDialog(range: BrowserHistoryClearRange, onCleared: () -> Unit) {
        val rangeLabel = historyClearRangeLabel(range)
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(activity.getString(R.string.dialog_clear_history_range_message, rangeLabel))
            .setPositiveButton(R.string.action_clear) { _, _ ->
                val removedCount = clearActions.clearHistory(range)
                Toast.makeText(
                    activity,
                    activity.getString(R.string.toast_history_range_cleared, rangeLabel, removedCount),
                    Toast.LENGTH_SHORT
                ).show()
                onCleared()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

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
}
