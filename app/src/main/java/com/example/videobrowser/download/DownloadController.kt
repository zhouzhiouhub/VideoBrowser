package com.example.videobrowser.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.utils.MediaUrlUtils

class DownloadController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val downloadRecordRepository: DownloadRecordRepository,
    private val openNativePlayer: (
        url: String,
        mimeType: String?,
        userAgentOverride: String?,
        titleOverride: String?
    ) -> Unit,
    private val openExternalUrl: (String) -> Unit
) {
    fun attachTo(browserManager: BrowserManager) {
        browserManager.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val mediaUri = url?.takeIf {
                MediaUrlUtils.isPlayableMediaUri(Uri.parse(it), mimeType)
            }
            if (mediaUri != null) {
                openNativePlayer(mediaUri, mimeType, userAgent, null)
                return@setDownloadListener
            }

            enqueue(
                url = url,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType
            )
        }
    }

    fun attachTo(browserManagers: Iterable<BrowserManager>) {
        browserManagers.forEach(::attachTo)
    }

    fun enqueue(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?
    ) {
        if (url.isNullOrBlank()) {
            Toast.makeText(activity, R.string.toast_download_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        runCatching {
            val resolvedUserAgent = userAgent?.takeIf { it.isNotBlank() }
                ?: browserManager().userAgentString()?.takeIf { it.isNotBlank() }
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription(activity.getString(R.string.toast_download_started))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                mimeType?.takeIf { it.isNotBlank() }?.let { setMimeType(it) }
                resolvedUserAgent?.let { addRequestHeader("User-Agent", it) }
                CookieManager.getInstance().getCookie(url)?.let { addRequestHeader("Cookie", it) }
            }
            val downloadManager =
                activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)
            downloadRecordRepository.add(
                DownloadRecord(
                    downloadId = downloadId,
                    title = fileName,
                    sourceUrl = url,
                    fileName = fileName,
                    mimeType = mimeType,
                    createdAtMillis = System.currentTimeMillis()
                )
            )
        }.onSuccess {
            Toast.makeText(activity, R.string.toast_download_started, Toast.LENGTH_SHORT).show()
        }.onFailure {
            openExternalUrl(url)
        }
    }
}
