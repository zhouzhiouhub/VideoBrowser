package com.example.videobrowser.functioncenter

import android.content.Context
import com.example.videobrowser.R
import com.example.videobrowser.download.DownloadCategory
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.download.DownloadStatus
import com.example.videobrowser.utils.ShortDateTimeFormatter
import com.example.videobrowser.utils.UrlUtils

internal class DownloadsPageTextFormatter(
    private val context: Context
) {
    fun recordSummary(
        record: DownloadRecord,
        retryable: Boolean = false,
        cancelable: Boolean = false
    ): String {
        val createdAt = ShortDateTimeFormatter.format(record.createdAtMillis)
        val status = context.getString(downloadStatusTitleResId(record.status))
        val progress = progressSummary(record)
        val failureReason = if (record.status == DownloadStatus.FAILED) {
            downloadFailureReasonText(record.statusReason)
        } else {
            null
        }
        val retryAction = if (retryable) {
            context.getString(R.string.action_retry_download)
        } else {
            null
        }
        val cancelAction = if (cancelable) {
            context.getString(R.string.action_cancel_download)
        } else {
            null
        }
        return listOfNotNull(
            status,
            progress,
            failureReason,
            retryAction,
            cancelAction,
            createdAt,
            UrlUtils.displayUrl(record.sourceUrl)
        ).joinToString(" | ")
    }

    fun statusFilterSummary(statusFilter: DownloadStatus?): String {
        return statusFilter
            ?.let { status -> context.getString(downloadStatusTitleResId(status)) }
            ?: context.getString(R.string.download_filter_all_status)
    }

    fun categoryFilterSummary(categoryFilter: DownloadCategory?): String {
        return categoryFilter
            ?.let { category -> context.getString(categoryTitleResId(category)) }
            ?: context.getString(R.string.download_filter_all_categories)
    }

    fun currentFilterSummary(
        statusFilter: DownloadStatus?,
        categoryFilter: DownloadCategory?
    ): String {
        return listOfNotNull(
            statusFilter?.let(::statusFilterSummary),
            categoryFilter?.let(::categoryFilterSummary)
        ).joinToString(" | ")
    }

    fun downloadStatusTitleResId(status: DownloadStatus): Int {
        return when (status) {
            DownloadStatus.IN_PROGRESS -> R.string.download_status_in_progress
            DownloadStatus.COMPLETED -> R.string.download_status_completed
            DownloadStatus.FAILED -> R.string.download_status_failed
            DownloadStatus.CANCELED -> R.string.download_status_canceled
        }
    }

    fun categoryTitleResId(category: DownloadCategory): Int {
        return when (category) {
            DownloadCategory.VIDEO -> R.string.download_category_video
            DownloadCategory.IMAGE -> R.string.download_category_image
            DownloadCategory.AUDIO -> R.string.download_category_audio
            DownloadCategory.DOCUMENT -> R.string.download_category_document
            DownloadCategory.APP -> R.string.download_category_app
            DownloadCategory.ARCHIVE -> R.string.download_category_archive
            DownloadCategory.OTHER -> R.string.download_category_other
        }
    }

    private fun progressSummary(record: DownloadRecord): String? {
        val progress = record.progress
        val downloaded = record.bytesDownloaded
        val total = record.totalBytes
        val percent = progress.percent()
        return when {
            percent != null && downloaded != null && total != null -> {
                context.getString(
                    R.string.download_progress_percent,
                    percent,
                    BrowserDataDisplayFormatter.formatBytes(downloaded),
                    BrowserDataDisplayFormatter.formatBytes(total)
                )
            }
            progress.hasDownloadedBytes && downloaded != null -> {
                context.getString(
                    R.string.download_progress_downloaded,
                    BrowserDataDisplayFormatter.formatBytes(downloaded)
                )
            }
            else -> null
        }
    }

    private fun downloadFailureReasonText(statusReason: Int?): String? {
        return statusReason?.let { reason ->
            context.getString(R.string.download_failure_reason, reason)
        }
    }
}
