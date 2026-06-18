package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 DownloadsPage 可以拆开理解为“Downloads Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.R
import com.example.videobrowser.download.AndroidDownloadStatusSnapshotReader
import com.example.videobrowser.download.DownloadCancellationPolicy
import com.example.videobrowser.download.DownloadCategory
import com.example.videobrowser.download.DownloadCategoryGroup
import com.example.videobrowser.download.DownloadRecord
import com.example.videobrowser.download.DownloadRecordFilter
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
    private val dialogController = DownloadsPageDialogController(
        activity = activity,
        textFormatter = textFormatter,
        recordOperations = DownloadRecordPageOperations(activity, downloadRecordRepository),
        downloadedFileLauncher = DownloadedFileLauncher(activity),
        retryDownload = retryDownload,
        showDownloadsPage = ::show
    )

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
                            summary = SearchSummaryFormatter.current(
                                query,
                                activity.getString(R.string.action_search_download_records_summary)
                            )
                        ) {
                            dialogController.showSearchDialog(query, statusFilter, categoryFilter)
                        }
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_filter_download_status),
                            summary = textFormatter.statusFilterSummary(statusFilter)
                        ) {
                            dialogController.showStatusFilterDialog(query, statusFilter, categoryFilter)
                        }
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_filter_download_category),
                            summary = textFormatter.categoryFilterSummary(categoryFilter)
                        ) {
                            dialogController.showCategoryFilterDialog(query, statusFilter, categoryFilter)
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
                        dialogController.confirmClearRecords()
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
                            dialogController.showDownloadActionsDialog(record, retryable, cancelable)
                        }
                    }
                }
            }
        }
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
     * 函数 `queryDownloadStatusSnapshot`：封装 `query Download Status Snapshot` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param downloadId 参数类型为 `Long`，表示函数执行 `downloadId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun queryDownloadStatusSnapshot(downloadId: Long): DownloadStatusSnapshot? {
        return statusSnapshotReader.query(downloadId)
    }

}
