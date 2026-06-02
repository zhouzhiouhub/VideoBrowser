package com.example.videobrowser.browser

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.download.DownloadController
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.utils.MediaUrlUtils

class PageActionsController(
    private val activity: AppCompatActivity,
    private val browserManager: BrowserManager,
    private val downloadController: DownloadController,
    private val settingsManager: SettingsManager,
    private val savedPageRepository: SavedPageRepository,
    private val currentActionableUrl: () -> String?,
    private val currentShareableUrl: () -> String?,
    private val currentPageTitle: () -> String,
    private val isShareableUrl: (String) -> Boolean,
    private val openNativePlayer: (
        url: String,
        mimeType: String?,
        userAgentOverride: String?,
        titleOverride: String?
    ) -> Unit,
    private val openExternalUrl: (String) -> Unit,
    private val updateBookmarkButton: () -> Unit,
    private val updateNavigationButtons: () -> Unit,
    private val updatePrivateBrowsingUi: () -> Unit,
    private val recreateActivity: () -> Unit
) {
    fun openLocalDocumentUri(
        uri: Uri,
        displayName: String? = null,
        mimeType: String? = null
    ) {
        val resolvedMimeType = mimeType ?: activity.contentResolver.getType(uri)
        val title = displayName ?: localDisplayName(uri)
        if (MediaUrlUtils.isPlayableMediaUri(uri, resolvedMimeType)) {
            openNativePlayer(
                uri.toString(),
                resolvedMimeType,
                null,
                title
            )
            return
        }

        openExternalDocument(uri, resolvedMimeType)
    }

    fun downloadCurrentUrl() {
        val url = currentShareableUrl() ?: run {
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        downloadController.enqueue(
            url = url,
            userAgent = browserManager.userAgentString(),
            contentDisposition = null,
            mimeType = null
        )
    }

    fun toggleCurrentBookmark() {
        val page = currentSavedPage() ?: run {
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }

        if (savedPageRepository.isBookmarked(page.url)) {
            savedPageRepository.removeBookmark(page.url)
            Toast.makeText(activity, R.string.toast_bookmark_removed, Toast.LENGTH_SHORT).show()
        } else {
            savedPageRepository.addBookmark(page)
            Toast.makeText(activity, R.string.toast_bookmark_saved, Toast.LENGTH_SHORT).show()
        }
        updateBookmarkButton()
    }

    fun copyCurrentUrl() {
        val url = currentShareableUrl() ?: run {
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(activity.getString(R.string.clipboard_page_url), url)
        )
        Toast.makeText(activity, R.string.toast_link_copied, Toast.LENGTH_SHORT).show()
    }

    fun shareCurrentUrl() {
        val url = currentShareableUrl() ?: run {
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.action_share_page)))
    }

    fun openCurrentUrlExternally() {
        val url = currentShareableUrl() ?: run {
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        openExternalUrl(url)
    }

    fun openCurrentUrlInNativePlayer() {
        val url = currentShareableUrl() ?: run {
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        if (!MediaUrlUtils.isPlayableMediaUri(Uri.parse(url))) {
            Toast.makeText(activity, R.string.toast_media_url_unsupported, Toast.LENGTH_SHORT).show()
            return
        }
        openNativePlayer(url, null, null, null)
    }

    fun clearBrowserData() {
        browserManager.clearBrowsingData()
        savedPageRepository.clearHistory()
        Toast.makeText(activity, R.string.toast_browser_data_cleared, Toast.LENGTH_SHORT).show()
        updateNavigationButtons()
    }

    fun setPrivateBrowsingEnabled(enabled: Boolean) {
        if (settingsManager.isPrivateBrowsingEnabled() == enabled) {
            updatePrivateBrowsingUi()
            return
        }

        settingsManager.setPrivateBrowsingEnabled(enabled)
        browserManager.setPrivateBrowsingEnabled(enabled)
        browserManager.clearBrowsingData()
        if (enabled) {
            savedPageRepository.clearHistory()
        }
        updatePrivateBrowsingUi()
        browserManager.reload()
        updateNavigationButtons()
        Toast.makeText(
            activity,
            if (enabled) {
                R.string.toast_private_browsing_enabled
            } else {
                R.string.toast_private_browsing_disabled
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    fun restoreDefaultSettings() {
        settingsManager.restoreDefaults()
        Toast.makeText(activity, R.string.toast_default_settings_restored, Toast.LENGTH_SHORT).show()
        recreateActivity()
    }

    fun addHistoryEntry(url: String?) {
        if (settingsManager.isPrivateBrowsingEnabled()) {
            return
        }
        val page = currentSavedPage(url) ?: return
        savedPageRepository.addHistory(page)
    }

    private fun openExternalDocument(uri: Uri, mimeType: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            activity.startActivity(
                Intent.createChooser(intent, activity.getString(R.string.action_open_file))
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        }
    }

    private fun localDisplayName(uri: Uri): String? {
        return activity.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) {
                null
            } else {
                cursor.getStringOrNull(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }

    private fun currentSavedPage(urlOverride: String? = null): SavedPage? {
        val url = urlOverride ?: currentActionableUrl()
        if (url.isNullOrBlank() || !isShareableUrl(url)) {
            return null
        }
        val title = currentPageTitle()
            .takeIf { it.isNotBlank() && !it.equals(url, ignoreCase = true) }
            ?: Uri.parse(url).host
            ?: url
        return SavedPage(title = title, url = url)
    }

    private fun Cursor.getStringOrNull(index: Int): String? {
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }
}
