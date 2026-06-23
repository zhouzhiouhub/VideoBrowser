package com.example.videobrowser.functioncenter

import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.utils.ActionListDialog
import com.example.videobrowser.utils.ConfirmationDialog
import com.example.videobrowser.utils.DialogAction
import com.example.videobrowser.utils.ShortToast

class BrowserDataManagementDialogController(
    private val activity: AppCompatActivity,
    private val clearActions: BrowserDataClearActions,
    private val reloadBrowser: () -> Unit
) {
    fun showSiteDataSearchDialog(currentQuery: String?, onSearch: (String) -> Unit) {
        SearchQueryDialog.show(
            activity = activity,
            titleRes = R.string.action_search_site_data,
            hintRes = R.string.hint_site_data_search,
            currentQuery = currentQuery,
            onSearch = onSearch
        )
    }

    fun showRemoveCookieDialog(pageUrl: String, cookieName: String, onRemoved: () -> Unit) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.title_remove_cookie,
            message = activity.getString(R.string.dialog_remove_cookie_message, cookieName),
            positiveButtonRes = R.string.action_remove
        ) {
            clearActions.removeCookie(pageUrl, cookieName)
            ShortToast.show(activity, R.string.toast_cookie_removed)
            reloadBrowser()
            onRemoved()
        }
    }

    fun showClearAllCookiesDialog(onCleared: () -> Unit) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            messageRes = R.string.dialog_clear_all_cookies_message,
            positiveButtonRes = R.string.action_clear
        ) {
            clearActions.clearAllCookies()
            ShortToast.show(activity, R.string.toast_cookies_cleared)
            reloadBrowser()
            onCleared()
        }
    }

    fun showClearCacheDialog(onCleared: () -> Unit) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            messageRes = R.string.dialog_clear_cache_message,
            positiveButtonRes = R.string.action_clear
        ) {
            clearActions.clearCache()
            ShortToast.show(activity, R.string.toast_cache_cleared)
            onCleared()
        }
    }

    fun showClearBookmarksDialog(onCleared: () -> Unit) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            messageRes = R.string.dialog_clear_bookmarks_message,
            positiveButtonRes = R.string.action_clear
        ) {
            clearActions.clearBookmarks()
            ShortToast.show(activity, R.string.toast_bookmarks_cleared)
            onCleared()
        }
    }

    fun showClearDownloadDataDialog(onCleared: () -> Unit) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            messageRes = R.string.dialog_clear_download_records_message,
            positiveButtonRes = R.string.action_clear
        ) {
            clearActions.clearDownloadRecordsAndFiles()
            ShortToast.show(activity, R.string.toast_download_records_cleared)
            onCleared()
        }
    }

    fun showClearHistoryRangeDialog(onCleared: () -> Unit) {
        val actions = BrowserHistoryClearRange.entries.map { range ->
            DialogAction(historyClearRangeLabel(range)) {
                showClearHistoryDialog(range, onCleared)
            }
        }
        ActionListDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            actions = actions,
            negativeButtonRes = android.R.string.cancel
        )
    }

    fun showRemoveSiteDataDialog(origin: String, onRemoved: () -> Unit) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.title_remove_site_data,
            message = activity.getString(R.string.dialog_remove_site_data_message, origin),
            positiveButtonRes = R.string.action_remove
        ) {
            clearActions.removeSiteData(origin)
            ShortToast.show(activity, R.string.toast_site_data_removed)
            onRemoved()
        }
    }

    fun showClearSiteDataDialog(onCleared: () -> Unit) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            messageRes = R.string.dialog_clear_site_data_message,
            positiveButtonRes = R.string.action_clear
        ) {
            clearActions.clearSiteData()
            ShortToast.show(activity, R.string.toast_site_data_cleared)
            onCleared()
        }
    }

    private fun showClearHistoryDialog(range: BrowserHistoryClearRange, onCleared: () -> Unit) {
        val rangeLabel = historyClearRangeLabel(range)
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            message = activity.getString(R.string.dialog_clear_history_range_message, rangeLabel),
            positiveButtonRes = R.string.action_clear
        ) {
            val removedCount = clearActions.clearHistory(range)
            ShortToast.show(
                activity,
                activity.getString(R.string.toast_history_range_cleared, rangeLabel, removedCount)
            )
            onCleared()
        }
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
