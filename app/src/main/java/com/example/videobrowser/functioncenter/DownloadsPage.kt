package com.example.videobrowser.functioncenter

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.download.DownloadCategory
import com.example.videobrowser.download.DownloadCategoryGroup
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.download.DownloadRecordCleaner
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.download.DownloadRetryPolicy
import com.example.videobrowser.download.DownloadStatus
import com.example.videobrowser.utils.UrlUtils
import java.text.DateFormat
import java.util.Date

class DownloadsPage(
    private val host: FunctionCenterPageHost,
    private val downloadRecordRepository: DownloadRecordRepository,
    private val retryDownload: (DownloadRecord) -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show(replaceCurrent: Boolean = false) {
        val records = downloadRecordRepository.records()

        host.showPage(
            title = activity.getString(R.string.title_downloads),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (records.isNotEmpty()) {
                host.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_actions)
                ) { section ->
                    host.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_clear),
                        summary = activity.getString(R.string.action_clear_download_records_summary)
                    ) {
                        confirmClearRecords()
                    }
                }
            }

            if (records.isEmpty()) {
                host.addEmptyState(content, activity.getString(R.string.dialog_download_records_empty))
                return@showPage
            }

            DownloadCategoryGroup.from(records).forEach { group ->
                host.addFunctionSection(
                    content,
                    activity.getString(categoryTitleResId(group.category))
                ) { section ->
                    group.records.forEach { record ->
                        val retryable = DownloadRetryPolicy.canRetry(record)
                        host.addActionRow(
                            parent = section,
                            title = record.title.ifBlank { record.fileName },
                            summary = recordSummary(record, retryable)
                        ) {
                            if (retryable) {
                                retryDownload(record)
                                Toast.makeText(
                                    activity,
                                    R.string.toast_download_retry_started,
                                    Toast.LENGTH_SHORT
                                ).show()
                                show(replaceCurrent = true)
                            } else {
                                openDownloadedFile(record)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun confirmClearRecords() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_download_records_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                clearRecordsAndFiles()
                Toast.makeText(activity, R.string.toast_download_records_cleared, Toast.LENGTH_SHORT).show()
                show(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun clearRecordsAndFiles() {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        DownloadRecordCleaner(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.clearRecordsAndFiles()
    }

    private fun openDownloadedFile(record: DownloadRecord) {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = runCatching {
            downloadManager.getUriForDownloadedFile(record.downloadId)
        }.getOrNull()

        if (uri == null) {
            Toast.makeText(activity, R.string.toast_download_file_unavailable, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, record.mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            activity.startActivity(
                Intent.createChooser(intent, activity.getString(R.string.action_open_file))
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        } catch (_: SecurityException) {
            Toast.makeText(activity, R.string.toast_download_file_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun recordSummary(record: DownloadRecord, retryable: Boolean = false): String {
        val createdAt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(Date(record.createdAtMillis))
        val status = activity.getString(downloadStatusTitleResId(record.status))
        val failureReason = if (record.status == DownloadStatus.FAILED) {
            downloadFailureReasonText(record.statusReason)
        } else {
            null
        }
        val retryAction = if (retryable) {
            activity.getString(R.string.action_retry_download)
        } else {
            null
        }
        return listOfNotNull(status, failureReason, retryAction, createdAt, UrlUtils.displayUrl(record.sourceUrl))
            .joinToString(" | ")
    }

    private fun downloadFailureReasonText(statusReason: Int?): String? {
        return statusReason?.let { reason ->
            activity.getString(R.string.download_failure_reason, reason)
        }
    }

    private fun downloadStatusTitleResId(status: DownloadStatus): Int {
        return when (status) {
            DownloadStatus.IN_PROGRESS -> R.string.download_status_in_progress
            DownloadStatus.COMPLETED -> R.string.download_status_completed
            DownloadStatus.FAILED -> R.string.download_status_failed
        }
    }

    private fun categoryTitleResId(category: DownloadCategory): Int {
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
}
