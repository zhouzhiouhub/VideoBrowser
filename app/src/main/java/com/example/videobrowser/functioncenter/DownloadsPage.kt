package com.example.videobrowser.functioncenter

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.download.DownloadCancellationPolicy
import com.example.videobrowser.download.DownloadCancellationResult
import com.example.videobrowser.download.DownloadCanceller
import com.example.videobrowser.download.DownloadCategory
import com.example.videobrowser.download.DownloadCategoryGroup
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.download.DownloadRecordCleaner
import com.example.videobrowser.download.DownloadRecordRemoveResult
import com.example.videobrowser.download.DownloadRecordRemover
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.download.DownloadRetryPolicy
import com.example.videobrowser.download.DownloadStatus
import com.example.videobrowser.download.DownloadStatusSnapshot
import com.example.videobrowser.download.DownloadStatusSynchronizer
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
        val records = refreshDownloadRecords()

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
                        title = activity.getString(R.string.action_refresh),
                        summary = activity.getString(R.string.action_refresh_download_records_summary)
                    ) {
                        show(replaceCurrent = true)
                    }
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
                        val cancelable = DownloadCancellationPolicy.canCancel(record)
                        host.addActionRow(
                            parent = section,
                            title = record.title.ifBlank { record.fileName },
                            summary = recordSummary(record, retryable, cancelable)
                        ) {
                            showDownloadActionsDialog(record, retryable, cancelable)
                        }
                    }
                }
            }
        }
    }

    private fun refreshDownloadRecords(): List<DownloadRecord> {
        return DownloadStatusSynchronizer(
            repository = downloadRecordRepository,
            querySnapshot = ::queryDownloadStatusSnapshot
        ).refresh()
    }

    private fun showDownloadActionsDialog(
        record: DownloadRecord,
        retryable: Boolean,
        cancelable: Boolean
    ) {
        val actions = downloadRecordActions(record, retryable, cancelable)
        AlertDialog.Builder(activity)
            .setTitle(record.title.ifBlank { record.fileName })
            .setItems(actions.map { action -> action.title }.toTypedArray()) { _, index ->
                actions.getOrNull(index)?.perform?.invoke()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun downloadRecordActions(
        record: DownloadRecord,
        retryable: Boolean,
        cancelable: Boolean
    ): List<DownloadRecordAction> {
        return buildList {
            if (!retryable && !cancelable) {
                add(
                    DownloadRecordAction(activity.getString(R.string.action_open_file)) {
                        openDownloadedFile(record)
                    }
                )
            }
            if (record.status == DownloadStatus.COMPLETED) {
                add(
                    DownloadRecordAction(activity.getString(R.string.action_share_file)) {
                        shareDownloadedFile(record)
                    }
                )
            }
            if (retryable) {
                add(
                    DownloadRecordAction(activity.getString(R.string.action_retry_download)) {
                        retryDownload(record)
                        Toast.makeText(
                            activity,
                            R.string.toast_download_retry_started,
                            Toast.LENGTH_SHORT
                        ).show()
                        show(replaceCurrent = true)
                    }
                )
            }
            if (cancelable) {
                add(
                    DownloadRecordAction(activity.getString(R.string.action_cancel_download)) {
                        confirmCancelDownload(record)
                    }
                )
            }
            add(
                DownloadRecordAction(activity.getString(R.string.action_copy_download_source)) {
                    copyDownloadSourceUrl(record)
                }
            )
            add(
                DownloadRecordAction(activity.getString(R.string.action_remove_download_record)) {
                    confirmRemoveDownloadRecord(record)
                }
            )
        }
    }

    private fun confirmCancelDownload(record: DownloadRecord) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_cancel_download)
            .setMessage(
                activity.getString(
                    R.string.dialog_cancel_download_message,
                    record.title.ifBlank { record.fileName }
                )
            )
            .setPositiveButton(R.string.action_cancel_download) { _, _ ->
                val result = cancelDownload(record)
                val toastResId = if (result.canceled) {
                    R.string.toast_download_canceled
                } else {
                    R.string.toast_download_cancel_failed
                }
                Toast.makeText(activity, toastResId, Toast.LENGTH_SHORT).show()
                show(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun confirmRemoveDownloadRecord(record: DownloadRecord) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_remove_download_record)
            .setMessage(
                activity.getString(
                    R.string.dialog_remove_download_record_message,
                    record.title.ifBlank { record.fileName }
                )
            )
            .setPositiveButton(R.string.action_remove) { _, _ ->
                val result = removeDownloadRecord(record)
                val toastResId = if (result.recordRemoved) {
                    R.string.toast_download_record_removed
                } else {
                    R.string.toast_download_record_remove_failed
                }
                Toast.makeText(activity, toastResId, Toast.LENGTH_SHORT).show()
                show(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun copyDownloadSourceUrl(record: DownloadRecord) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                activity.getString(R.string.clipboard_download_source_url),
                record.sourceUrl
            )
        )
        Toast.makeText(activity, R.string.toast_download_source_copied, Toast.LENGTH_SHORT).show()
    }

    private fun cancelDownload(record: DownloadRecord): DownloadCancellationResult {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return DownloadCanceller(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.cancel(record)
    }

    private fun removeDownloadRecord(record: DownloadRecord): DownloadRecordRemoveResult {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return DownloadRecordRemover(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.remove(record)
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

    private fun shareDownloadedFile(record: DownloadRecord) {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = runCatching {
            downloadManager.getUriForDownloadedFile(record.downloadId)
        }.getOrNull()

        if (uri == null) {
            Toast.makeText(activity, R.string.toast_download_file_unavailable, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = record.mimeType ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(activity.contentResolver, record.fileName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            activity.startActivity(
                Intent.createChooser(intent, activity.getString(R.string.action_share_file))
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        } catch (_: SecurityException) {
            Toast.makeText(activity, R.string.toast_download_file_unavailable, Toast.LENGTH_SHORT).show()
        }
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

    private fun recordSummary(
        record: DownloadRecord,
        retryable: Boolean = false,
        cancelable: Boolean = false
    ): String {
        val createdAt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(Date(record.createdAtMillis))
        val status = activity.getString(downloadStatusTitleResId(record.status))
        val progress = progressSummary(record)
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
        val cancelAction = if (cancelable) {
            activity.getString(R.string.action_cancel_download)
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

    private fun progressSummary(record: DownloadRecord): String? {
        val progress = record.progress
        val downloaded = record.bytesDownloaded
        val total = record.totalBytes
        val percent = progress.percent()
        return when {
            percent != null && downloaded != null && total != null -> {
                activity.getString(
                    R.string.download_progress_percent,
                    percent,
                    BrowserDataDisplayFormatter.formatBytes(downloaded),
                    BrowserDataDisplayFormatter.formatBytes(total)
                )
            }
            progress.hasDownloadedBytes && downloaded != null -> {
                activity.getString(
                    R.string.download_progress_downloaded,
                    BrowserDataDisplayFormatter.formatBytes(downloaded)
                )
            }
            else -> null
        }
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
            DownloadStatus.CANCELED -> R.string.download_status_canceled
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

    private fun queryDownloadStatusSnapshot(downloadId: Long): DownloadStatusSnapshot? {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = runCatching {
            downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        }.getOrNull() ?: return null
        return cursor.use(::snapshotFromCursor)
    }

    private fun snapshotFromCursor(cursor: Cursor): DownloadStatusSnapshot? {
        if (!cursor.moveToFirst()) {
            return null
        }
        val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        if (statusColumn < 0) {
            return null
        }
        val reasonColumn = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val downloadedColumn = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
        val totalColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
        val statusReason = reasonColumn
            .takeIf { column -> column >= 0 }
            ?.let { column -> cursor.getInt(column) }
        val bytesDownloaded = downloadedColumn
            .takeIf { column -> column >= 0 }
            ?.let { column -> cursor.getLong(column) }
            ?.takeIf { value -> value >= 0L }
        val totalBytes = totalColumn
            .takeIf { column -> column >= 0 }
            ?.let { column -> cursor.getLong(column) }
            ?.takeIf { value -> value >= 0L }

        return when (cursor.getInt(statusColumn)) {
            DownloadManager.STATUS_SUCCESSFUL -> DownloadStatusSnapshot(
                status = DownloadStatus.COMPLETED,
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes
            )
            DownloadManager.STATUS_FAILED -> DownloadStatusSnapshot(
                status = DownloadStatus.FAILED,
                statusReason = statusReason,
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes
            )
            DownloadManager.STATUS_PENDING,
            DownloadManager.STATUS_PAUSED,
            DownloadManager.STATUS_RUNNING -> DownloadStatusSnapshot(
                status = DownloadStatus.IN_PROGRESS,
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes
            )
            else -> null
        }
    }

    private data class DownloadRecordAction(
        val title: String,
        val perform: () -> Unit
    )
}
