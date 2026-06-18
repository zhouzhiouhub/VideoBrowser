package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 DownloadsPage 可以拆开理解为“Downloads Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.download.AndroidDownloadStatusSnapshotReader
import com.example.videobrowser.download.DownloadCancellationPolicy
import com.example.videobrowser.download.DownloadCancellationResult
import com.example.videobrowser.download.DownloadCanceller
import com.example.videobrowser.download.DownloadCategory
import com.example.videobrowser.download.DownloadCategoryGroup
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.download.DownloadRecordCleaner
import com.example.videobrowser.download.DownloadRecordFilter
import com.example.videobrowser.download.DownloadRecordRemoveResult
import com.example.videobrowser.download.DownloadRecordRemover
import com.example.videobrowser.download.DownloadRecordRepository
import com.example.videobrowser.download.DownloadRecordSearch
import com.example.videobrowser.download.DownloadRetryPolicy
import com.example.videobrowser.download.DownloadStatus
import com.example.videobrowser.download.DownloadStatusSnapshot
import com.example.videobrowser.download.DownloadStatusSynchronizer

class DownloadsPage(
    private val host: FunctionCenterPageHost,
    private val downloadRecordRepository: DownloadRecordRepository,
    private val retryDownload: (DownloadRecord) -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity
    private val textFormatter = DownloadsPageTextFormatter(activity)
    private val statusSnapshotReader = AndroidDownloadStatusSnapshotReader(activity)
    private val downloadedFileLauncher = DownloadedFileLauncher(activity)

    /**
     * 函数 `show`：控制 `show` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     * @param query 参数类型为 `String?`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @param statusFilter 参数类型为 `DownloadStatus?`，表示函数执行 `statusFilter` 相关逻辑时需要读取或处理的输入。
     * @param categoryFilter 参数类型为 `DownloadCategory?`，表示函数执行 `categoryFilter` 相关逻辑时需要读取或处理的输入。
     */
    fun show(
        replaceCurrent: Boolean = false,
        query: String? = null,
        statusFilter: DownloadStatus? = null,
        categoryFilter: DownloadCategory? = null
    ) {
        val allRecords = refreshDownloadRecords()
        val searchedRecords = DownloadRecordSearch.filter(allRecords, query)
        val records = DownloadRecordFilter.filter(
            records = searchedRecords,
            status = statusFilter,
            category = categoryFilter
        )

        host.showPage(
            title = activity.getString(R.string.title_downloads),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (allRecords.isNotEmpty()) {
                host.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_actions)
                ) { section ->
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_search_download_records),
                            summary = textFormatter.currentSearchSummary(query)
                        ) {
                            showSearchDialog(query, statusFilter, categoryFilter)
                        }
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_filter_download_status),
                            summary = textFormatter.statusFilterSummary(statusFilter)
                        ) {
                            showStatusFilterDialog(query, statusFilter, categoryFilter)
                        }
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_filter_download_category),
                            summary = textFormatter.categoryFilterSummary(categoryFilter)
                        ) {
                            showCategoryFilterDialog(query, statusFilter, categoryFilter)
                        }
                    if (!query.isNullOrBlank()) {
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_clear_search),
                            summary = query
                        ) {
                            show(
                                replaceCurrent = true,
                                statusFilter = statusFilter,
                                categoryFilter = categoryFilter
                            )
                        }
                    }
                    if (statusFilter != null || categoryFilter != null) {
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_clear_download_filters),
                            summary = textFormatter.currentFilterSummary(statusFilter, categoryFilter)
                        ) {
                            show(replaceCurrent = true, query = query)
                        }
                    }
                    host.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_refresh),
                        summary = activity.getString(R.string.action_refresh_download_records_summary)
                    ) {
                        show(
                            replaceCurrent = true,
                            query = query,
                            statusFilter = statusFilter,
                            categoryFilter = categoryFilter
                        )
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

            if (allRecords.isEmpty()) {
                host.addEmptyState(content, activity.getString(R.string.dialog_download_records_empty))
                return@showPage
            }
            if (records.isEmpty()) {
                host.addEmptyState(content, activity.getString(R.string.dialog_download_records_search_empty))
                return@showPage
            }

            DownloadCategoryGroup.from(records).forEach { group ->
                host.addFunctionSection(
                    content,
                    activity.getString(textFormatter.categoryTitleResId(group.category))
                ) { section ->
                    group.records.forEach { record ->
                        val retryable = DownloadRetryPolicy.canRetry(record)
                        val cancelable = DownloadCancellationPolicy.canCancel(record)
                        host.addActionRow(
                            parent = section,
                            title = record.title.ifBlank { record.fileName },
                            summary = textFormatter.recordSummary(record, retryable, cancelable)
                        ) {
                            showDownloadActionsDialog(record, retryable, cancelable)
                        }
                    }
                }
            }
        }
    }

    /**
     * 函数 `showSearchDialog`：控制 `show Search Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param currentQuery 参数类型为 `String?`，表示函数执行 `currentQuery` 相关逻辑时需要读取或处理的输入。
     * @param statusFilter 参数类型为 `DownloadStatus?`，表示函数执行 `statusFilter` 相关逻辑时需要读取或处理的输入。
     * @param categoryFilter 参数类型为 `DownloadCategory?`，表示函数执行 `categoryFilter` 相关逻辑时需要读取或处理的输入。
     */
    private fun showSearchDialog(
        currentQuery: String?,
        statusFilter: DownloadStatus?,
        categoryFilter: DownloadCategory?
    ) {
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setSingleLine(true)
            hint = activity.getString(R.string.hint_download_records_search)
            setText(currentQuery.orEmpty())
            setSelection(text?.length ?: 0)
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_search_download_records)
            .setView(input)
            .setPositiveButton(R.string.action_search_download_records) { _, _ ->
                show(
                    replaceCurrent = true,
                    query = input.text?.toString()?.trim().orEmpty(),
                    statusFilter = statusFilter,
                    categoryFilter = categoryFilter
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showStatusFilterDialog`：控制 `show Status Filter Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param query 参数类型为 `String?`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @param currentStatus 参数类型为 `DownloadStatus?`，表示函数执行 `currentStatus` 相关逻辑时需要读取或处理的输入。
     * @param categoryFilter 参数类型为 `DownloadCategory?`，表示函数执行 `categoryFilter` 相关逻辑时需要读取或处理的输入。
     */
    private fun showStatusFilterDialog(
        query: String?,
        currentStatus: DownloadStatus?,
        categoryFilter: DownloadCategory?
    ) {
        val statuses = DownloadStatus.entries
        val labels = listOf(activity.getString(R.string.download_filter_all_status)) +
            statuses.map { status -> activity.getString(textFormatter.downloadStatusTitleResId(status)) }
        val checkedIndex = currentStatus?.let { status -> statuses.indexOf(status) + 1 } ?: 0

        AlertDialog.Builder(activity)
            .setTitle(R.string.action_filter_download_status)
            .setSingleChoiceItems(labels.toTypedArray(), checkedIndex) { dialog, index ->
                dialog.dismiss()
                show(
                    replaceCurrent = true,
                    query = query,
                    statusFilter = statuses.getOrNull(index - 1),
                    categoryFilter = categoryFilter
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showCategoryFilterDialog`：控制 `show Category Filter Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param query 参数类型为 `String?`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @param statusFilter 参数类型为 `DownloadStatus?`，表示函数执行 `statusFilter` 相关逻辑时需要读取或处理的输入。
     * @param currentCategory 参数类型为 `DownloadCategory?`，表示函数执行 `currentCategory` 相关逻辑时需要读取或处理的输入。
     */
    private fun showCategoryFilterDialog(
        query: String?,
        statusFilter: DownloadStatus?,
        currentCategory: DownloadCategory?
    ) {
        val categories = DownloadCategory.entries
        val labels = listOf(activity.getString(R.string.download_filter_all_categories)) +
            categories.map { category -> activity.getString(textFormatter.categoryTitleResId(category)) }
        val checkedIndex = currentCategory?.let { category -> categories.indexOf(category) + 1 } ?: 0

        AlertDialog.Builder(activity)
            .setTitle(R.string.action_filter_download_category)
            .setSingleChoiceItems(labels.toTypedArray(), checkedIndex) { dialog, index ->
                dialog.dismiss()
                show(
                    replaceCurrent = true,
                    query = query,
                    statusFilter = statusFilter,
                    categoryFilter = categories.getOrNull(index - 1)
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `refreshDownloadRecords`：根据最新状态刷新 `refresh Download Records` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun refreshDownloadRecords(): List<DownloadRecord> {
        return DownloadStatusSynchronizer(
            repository = downloadRecordRepository,
            querySnapshot = ::queryDownloadStatusSnapshot
        ).refresh()
    }

    /**
     * 函数 `showDownloadActionsDialog`：控制 `show Download Actions Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     * @param retryable 参数类型为 `Boolean`，表示函数执行 `retryable` 相关逻辑时需要读取或处理的输入。
     * @param cancelable 参数类型为 `Boolean`，表示函数执行 `cancelable` 相关逻辑时需要读取或处理的输入。
     */
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

    /**
     * 函数 `downloadRecordActions`：封装 `download Record Actions` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     * @param retryable 参数类型为 `Boolean`，表示函数执行 `retryable` 相关逻辑时需要读取或处理的输入。
     * @param cancelable 参数类型为 `Boolean`，表示函数执行 `cancelable` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `confirmCancelDownload`：封装 `confirm Cancel Download` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     */
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

    /**
     * 函数 `confirmRemoveDownloadRecord`：封装 `confirm Remove Download Record` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     */
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

    /**
     * 函数 `copyDownloadSourceUrl`：封装 `copy Download Source Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     */
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

    /**
     * 函数 `cancelDownload`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun cancelDownload(record: DownloadRecord): DownloadCancellationResult {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return DownloadCanceller(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.cancel(record)
    }

    /**
     * 函数 `removeDownloadRecord`：封装 `remove Download Record` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun removeDownloadRecord(record: DownloadRecord): DownloadRecordRemoveResult {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return DownloadRecordRemover(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.remove(record)
    }

    /**
     * 函数 `confirmClearRecords`：封装 `confirm Clear Records` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 函数 `clearRecordsAndFiles`：封装 `clear Records And Files` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun clearRecordsAndFiles() {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        DownloadRecordCleaner(downloadRecordRepository) { downloadIds ->
            downloadManager.remove(*downloadIds)
        }.clearRecordsAndFiles()
    }

    /**
     * 函数 `shareDownloadedFile`：封装 `share Downloaded File` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     */
    private fun shareDownloadedFile(record: DownloadRecord) {
        downloadedFileLauncher.shareDownloadedFile(record)
    }

    /**
     * 函数 `openDownloadedFile`：启动或加载 `open Downloaded File` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     */
    private fun openDownloadedFile(record: DownloadRecord) {
        downloadedFileLauncher.openDownloadedFile(record)
    }

    /**
     * 函数 `queryDownloadStatusSnapshot`：封装 `query Download Status Snapshot` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param downloadId 参数类型为 `Long`，表示函数执行 `downloadId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun queryDownloadStatusSnapshot(downloadId: Long): DownloadStatusSnapshot? {
        return statusSnapshotReader.query(downloadId)
    }

    private data class DownloadRecordAction(
        val title: String,
        val perform: () -> Unit
    )
}
