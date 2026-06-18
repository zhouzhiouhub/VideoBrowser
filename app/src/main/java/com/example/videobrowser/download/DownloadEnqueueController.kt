package com.example.videobrowser.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager

/**
 * 系统下载入队控制器。
 *
 * DownloadController 负责接收 WebView 下载事件，本类负责安全校验、确认弹窗和 DownloadManager 入队。
 */
internal class DownloadEnqueueController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val downloadRecordRepository: DownloadRecordRepository
) {
    fun enqueue(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?
    ) {
        // 真正创建系统下载前先做 URL 和文件名安全检查，必要时弹出确认对话框。
        if (url.isNullOrBlank()) {
            Toast.makeText(activity, R.string.toast_download_failed, Toast.LENGTH_SHORT).show()
            return
        }
        if (!DownloadSafetyPolicy.isDownloadableNetworkUrl(url)) {
            Toast.makeText(activity, R.string.toast_download_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = DownloadSafetyPolicy.safeDownloadFileName(
            URLUtil.guessFileName(url, contentDisposition, mimeType)
        )
        confirmDownloadIfNeeded(
            url = url,
            fileName = fileName,
            mimeType = mimeType,
            confirmed = {
                enqueueConfirmed(
                    url = url,
                    userAgent = userAgent,
                    mimeType = mimeType,
                    fileName = fileName
                )
            }
        )
    }

    private fun confirmDownloadIfNeeded(
        url: String,
        fileName: String,
        mimeType: String?,
        confirmed: () -> Unit
    ) {
        if (DownloadSafetyPolicy.requiresInsecureTransportConfirmation(browserManager().currentUrl(), url)) {
            showInsecureDownloadConfirmation(
                fileName = fileName,
                confirmed = {
                    confirmAppPackageDownloadIfNeeded(
                        fileName = fileName,
                        mimeType = mimeType,
                        confirmed = confirmed
                    )
                }
            )
            return
        }

        confirmAppPackageDownloadIfNeeded(
            fileName = fileName,
            mimeType = mimeType,
            confirmed = confirmed
        )
    }

    private fun confirmAppPackageDownloadIfNeeded(
        fileName: String,
        mimeType: String?,
        confirmed: () -> Unit
    ) {
        if (DownloadSafetyPolicy.requiresConfirmation(fileName, mimeType)) {
            showRiskyDownloadConfirmation(
                fileName = fileName,
                confirmed = confirmed
            )
            return
        }

        confirmed()
    }

    private fun showRiskyDownloadConfirmation(fileName: String, confirmed: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_confirm_app_download)
            .setMessage(activity.getString(R.string.dialog_confirm_app_download_message, fileName))
            .setPositiveButton(R.string.action_download_anyway) { _, _ -> confirmed() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showInsecureDownloadConfirmation(fileName: String, confirmed: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_confirm_insecure_download)
            .setMessage(activity.getString(R.string.dialog_confirm_insecure_download_message, fileName))
            .setPositiveButton(R.string.action_download_anyway) { _, _ -> confirmed() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun enqueueConfirmed(
        url: String,
        userAgent: String?,
        mimeType: String?,
        fileName: String
    ) {
        // DownloadManager 负责后台下载和系统通知；应用自己只保存一份轻量记录用于功能中心展示。
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
                    createdAtMillis = System.currentTimeMillis(),
                    status = DownloadStatus.IN_PROGRESS
                )
            )
        }.onSuccess {
            Toast.makeText(activity, R.string.toast_download_started, Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(activity, R.string.toast_download_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
