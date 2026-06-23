package com.example.videobrowser.functioncenter

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.download.AndroidSystemDownloadRemover
import com.example.videobrowser.download.DownloadRecordCleaner
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.storage.SavedPageRepository

class BrowserDataClearActions(
    private val context: Context,
    private val browserManagers: () -> List<BrowserManager>,
    private val savedPageRepository: SavedPageRepository,
    private val downloadRecordRepository: DownloadRecordRepository
) {
    private val systemDownloadRemover = AndroidSystemDownloadRemover(context)
    private val downloadRecordCleaner = DownloadRecordCleaner(
        downloadRecordRepository,
        systemDownloadRemover
    )

    fun clearBookmarks() {
        savedPageRepository.clear(SavedPageRepository.SavedPageCollection.BOOKMARKS)
    }

    fun clearDownloadRecordsAndFiles() {
        downloadRecordCleaner.clearRecordsAndFiles()
    }

    fun clearHistory(range: BrowserHistoryClearRange, nowMillis: Long = System.currentTimeMillis()): Int {
        val historyCount = savedPageRepository.history().size
        val cutoffMillis = range.cutoffMillis(nowMillis)
        return if (cutoffMillis == null) {
            savedPageRepository.clearHistory()
            historyCount
        } else {
            savedPageRepository.clearHistoryUpdatedSince(cutoffMillis)
        }
    }

    fun removeCookie(pageUrl: String, cookieName: String) {
        CookieManager.getInstance().apply {
            setCookie(pageUrl, "$cookieName=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT")
            setCookie(pageUrl, "$cookieName=; Max-Age=0; Path=/")
            flush()
        }
    }

    fun clearAllCookies() {
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
    }

    fun clearCache() {
        browserManagers().forEach { manager -> manager.clearCache() }
    }

    fun removeSiteData(origin: String) {
        WebStorage.getInstance().deleteOrigin(origin)
    }

    fun clearSiteData() {
        WebStorage.getInstance().deleteAllData()
    }
}
