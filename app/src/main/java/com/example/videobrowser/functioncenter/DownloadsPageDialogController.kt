package com.example.videobrowser.functioncenter

import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.download.DownloadCategory
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.download.DownloadStatus
import com.example.videobrowser.utils.ActionListDialog
import com.example.videobrowser.utils.ConfirmationDialog
import com.example.videobrowser.utils.DialogAction
import com.example.videobrowser.utils.ShortToast

internal class DownloadsPageDialogController(
    private val activity: AppCompatActivity,
    private val textFormatter: DownloadsPageTextFormatter,
    private val recordOperations: DownloadRecordPageOperations,
    private val downloadedFileLauncher: DownloadedFileLauncher,
    private val retryDownload: (DownloadRecord) -> Unit,
    private val showDownloadsPage: (
        replaceCurrent: Boolean,
        query: String?,
        statusFilter: DownloadStatus?,
        categoryFilter: DownloadCategory?
    ) -> Unit
) {
    fun showSearchDialog(
        currentQuery: String?,
        statusFilter: DownloadStatus?,
        categoryFilter: DownloadCategory?
    ) {
        SearchQueryDialog.show(
            activity = activity,
            titleRes = R.string.action_search_download_records,
            hintRes = R.string.hint_download_records_search,
            currentQuery = currentQuery
        ) { query ->
            showDownloadsPage(true, query, statusFilter, categoryFilter)
        }
    }

    fun showStatusFilterDialog(
        query: String?,
        currentStatus: DownloadStatus?,
        categoryFilter: DownloadCategory?
    ) {
        showSingleChoiceFilterDialog(
            titleRes = R.string.action_filter_download_status,
            allLabelRes = R.string.download_filter_all_status,
            values = DownloadStatus.entries,
            currentValue = currentStatus,
            labelFor = { status -> activity.getString(textFormatter.downloadStatusTitleResId(status)) }
        ) { selectedStatus ->
            showDownloadsPage(true, query, selectedStatus, categoryFilter)
        }
    }

    fun showCategoryFilterDialog(
        query: String?,
        statusFilter: DownloadStatus?,
        currentCategory: DownloadCategory?
    ) {
        showSingleChoiceFilterDialog(
            titleRes = R.string.action_filter_download_category,
            allLabelRes = R.string.download_filter_all_categories,
            values = DownloadCategory.entries,
            currentValue = currentCategory,
            labelFor = { category -> activity.getString(textFormatter.categoryTitleResId(category)) }
        ) { selectedCategory ->
            showDownloadsPage(true, query, statusFilter, selectedCategory)
        }
    }

    fun showDownloadActionsDialog(
        record: DownloadRecord,
        retryable: Boolean,
        cancelable: Boolean
    ) {
        val actions = downloadRecordActions(record, retryable, cancelable)
        ActionListDialog.show(
            activity = activity,
            title = downloadRecordDisplayName(record),
            actions = actions,
            negativeButtonRes = android.R.string.cancel
        )
    }

    fun confirmClearRecords() {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            messageRes = R.string.dialog_clear_download_records_message,
            positiveButtonRes = R.string.action_clear
        ) {
            recordOperations.clearRecordsAndFiles()
            ShortToast.show(activity, R.string.toast_download_records_cleared)
            showDownloadsPage(true, null, null, null)
        }
    }

    private fun <T> showSingleChoiceFilterDialog(
        titleRes: Int,
        allLabelRes: Int,
        values: List<T>,
        currentValue: T?,
        labelFor: (T) -> String,
        onSelected: (T?) -> Unit
    ) {
        val labels = listOf(activity.getString(allLabelRes)) + values.map(labelFor)
        val checkedIndex = currentValue?.let { value -> values.indexOf(value) + 1 } ?: 0

        SingleChoiceDialog.show(
            activity = activity,
            titleRes = titleRes,
            labels = labels,
            checkedIndex = checkedIndex
        ) { index ->
            onSelected(values.getOrNull(index - 1))
        }
    }

    private fun downloadRecordActions(
        record: DownloadRecord,
        retryable: Boolean,
        cancelable: Boolean
    ): List<DialogAction> {
        return buildList {
            if (!retryable && !cancelable) {
                add(
                    DialogAction(activity.getString(R.string.action_open_file)) {
                        openDownloadedFile(record)
                    }
                )
            }
            if (record.status == DownloadStatus.COMPLETED) {
                add(
                    DialogAction(activity.getString(R.string.action_share_file)) {
                        shareDownloadedFile(record)
                    }
                )
            }
            if (retryable) {
                add(
                    DialogAction(activity.getString(R.string.action_retry_download)) {
                        retryDownload(record)
                        ShortToast.show(activity, R.string.toast_download_retry_started)
                        showDownloadsPage(true, null, null, null)
                    }
                )
            }
            if (cancelable) {
                add(
                    DialogAction(activity.getString(R.string.action_cancel_download)) {
                        confirmCancelDownload(record)
                    }
                )
            }
            add(
                DialogAction(activity.getString(R.string.action_copy_download_source)) {
                    recordOperations.copyDownloadSourceUrl(record)
                }
            )
            add(
                DialogAction(activity.getString(R.string.action_remove_download_record)) {
                    confirmRemoveDownloadRecord(record)
                }
            )
        }
    }

    private fun confirmCancelDownload(record: DownloadRecord) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_cancel_download,
            message = activity.getString(
                R.string.dialog_cancel_download_message,
                downloadRecordDisplayName(record)
            ),
            positiveButtonRes = R.string.action_cancel_download
        ) {
            val result = recordOperations.cancelDownload(record)
            val toastResId = if (result.canceled) {
                R.string.toast_download_canceled
            } else {
                R.string.toast_download_cancel_failed
            }
            ShortToast.show(activity, toastResId)
            showDownloadsPage(true, null, null, null)
        }
    }

    private fun confirmRemoveDownloadRecord(record: DownloadRecord) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_remove_download_record,
            message = activity.getString(
                R.string.dialog_remove_download_record_message,
                downloadRecordDisplayName(record)
            ),
            positiveButtonRes = R.string.action_remove
        ) {
            val result = recordOperations.removeDownloadRecord(record)
            val toastResId = if (result.recordRemoved) {
                R.string.toast_download_record_removed
            } else {
                R.string.toast_download_record_remove_failed
            }
            ShortToast.show(activity, toastResId)
            showDownloadsPage(true, null, null, null)
        }
    }

    private fun downloadRecordDisplayName(record: DownloadRecord): String {
        return record.title.ifBlank { record.fileName }
    }

    private fun shareDownloadedFile(record: DownloadRecord) {
        downloadedFileLauncher.shareDownloadedFile(record)
    }

    private fun openDownloadedFile(record: DownloadRecord) {
        downloadedFileLauncher.openDownloadedFile(record)
    }
}
