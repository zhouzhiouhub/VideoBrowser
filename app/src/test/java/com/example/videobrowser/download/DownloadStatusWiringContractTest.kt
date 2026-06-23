package com.example.videobrowser.download

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Status Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadStatusWiringContractTest {
    /**
     * 测试函数 `downloadControllerTracksStartedAndCompletedSystemDownloads`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `download Controller Tracks Started And Completed System Downloads` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadControllerTracksStartedAndCompletedSystemDownloads() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadController.kt"
        ).readText()
        val snapshotReader = projectFile(
            "src/main/java/com/example/videobrowser/download/AndroidDownloadStatusSnapshotReader.kt"
        ).readText()
        val enqueueController = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadEnqueueController.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(enqueueController.contains("DownloadStatus.IN_PROGRESS"))
        assertTrue(controller.contains("DownloadManager.ACTION_DOWNLOAD_COMPLETE"))
        assertTrue(controller.contains("AndroidDownloadStatusSnapshotReader(activity)"))
        assertTrue(controller.contains("queryFailureSnapshot = DownloadStatusSnapshot(status = DownloadStatus.FAILED)"))
        assertTrue(snapshotReader.contains("DownloadManager.Query().setFilterById(downloadId)"))
        assertTrue(snapshotReader.contains("DownloadManager.STATUS_SUCCESSFUL"))
        assertTrue(snapshotReader.contains("DownloadManager.STATUS_FAILED"))
        assertTrue(snapshotReader.contains("DownloadManager.COLUMN_REASON"))
        assertTrue(snapshotReader.contains("DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR"))
        assertTrue(snapshotReader.contains("DownloadManager.COLUMN_TOTAL_SIZE_BYTES"))
        assertTrue(controller.contains("statusReason"))
        assertTrue(controller.contains("downloadRecordRepository.contains(downloadId)"))
        assertTrue(controller.contains("downloadRecordRepository.updateSnapshot"))
        assertTrue(repository().contains("fun contains(downloadId: Long): Boolean"))
        assertTrue(readme.contains("下载完成广播只处理本应用记录过的下载 ID"))
    }

    /**
     * 测试函数 `mainActivityDisposesDownloadStatusReceiver`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Disposes Download Status Receiver` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityDisposesDownloadStatusReceiver() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val lifecycleController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityLifecycleController.kt"
        ).readText()

        assertTrue(mainActivity.contains("browserActivityLifecycleController.handleDestroy()"))
        assertTrue(lifecycleController.contains("downloadController()?.dispose()"))
    }

    /**
     * 测试函数 `downloadsPageShowsStatusInRecordSummary`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `downloads Page Shows Status In Record Summary` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadsPageShowsStatusInRecordSummary() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val formatter = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageTextFormatter.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(downloadsPage.contains("textFormatter.recordSummary(record, retryable, cancelable)"))
        assertTrue(formatter.contains("downloadStatusTitleResId(record.status)"))
        assertTrue(downloadsPage.contains("DownloadStatusSynchronizer"))
        assertTrue(downloadsPage.contains("queryDownloadStatusSnapshot"))
        assertTrue(formatter.contains("progressSummary(record)"))
        assertTrue(formatter.contains("downloadFailureReasonText(record.statusReason)"))
        assertTrue(strings.contains("download_status_in_progress"))
        assertTrue(strings.contains("download_status_completed"))
        assertTrue(strings.contains("download_status_failed"))
        assertTrue(strings.contains("download_progress_percent"))
        assertTrue(strings.contains("download_progress_downloaded"))
        assertTrue(strings.contains("download_failure_reason"))
    }

    /**
     * 测试函数 `downloadsPageSupportsSearchFiltering`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `downloads Page Supports Search Filtering` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadsPageSupportsSearchFiltering() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageDialogController.kt"
        ).readText()
        val search = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadRecordSearch.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(downloadsPage.contains("DownloadRecordSearch.filter(allRecords, query)"))
        assertTrue(downloadsPage.contains("R.string.action_search_download_records"))
        assertTrue(downloadsPage.contains("dialogController.showSearchDialog(query, statusFilter, categoryFilter)"))
        assertTrue(dialogController.contains("fun showSearchDialog"))
        assertTrue(downloadsPage.contains("R.string.action_clear_search"))
        assertTrue(downloadsPage.contains("R.string.dialog_download_records_search_empty"))
        assertTrue(search.contains("record.fileName"))
        assertTrue(search.contains("record.sourceUrl"))
        assertTrue(strings.contains("action_search_download_records"))
        assertTrue(strings.contains("action_search_download_records_summary"))
        assertTrue(strings.contains("hint_download_records_search"))
        assertTrue(strings.contains("dialog_download_records_search_empty"))
    }

    /**
     * 测试函数 `downloadsPageSupportsStatusAndCategoryFilters`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `downloads Page Supports Status And Category Filters` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadsPageSupportsStatusAndCategoryFilters() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageDialogController.kt"
        ).readText()
        val filter = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadRecordFilter.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(downloadsPage.contains("DownloadRecordFilter.filter("))
        assertTrue(downloadsPage.contains("statusFilter: DownloadStatus? = null"))
        assertTrue(downloadsPage.contains("categoryFilter: DownloadCategory? = null"))
        assertTrue(downloadsPage.contains("R.string.action_filter_download_status"))
        assertTrue(downloadsPage.contains("R.string.action_filter_download_category"))
        assertTrue(downloadsPage.contains("R.string.action_clear_download_filters"))
        assertTrue(downloadsPage.contains("dialogController.showStatusFilterDialog(query, statusFilter, categoryFilter)"))
        assertTrue(downloadsPage.contains("dialogController.showCategoryFilterDialog(query, statusFilter, categoryFilter)"))
        assertTrue(dialogController.contains("fun showStatusFilterDialog"))
        assertTrue(dialogController.contains("fun showCategoryFilterDialog"))
        assertTrue(dialogController.contains("private fun <T> showSingleChoiceFilterDialog("))
        assertTrue(dialogController.contains("SingleChoiceDialog.show("))
        assertTrue(dialogController.contains("DownloadStatus.entries"))
        assertTrue(dialogController.contains("DownloadCategory.entries"))
        assertTrue(filter.contains("DownloadCategory.from(record.mimeType, record.fileName)"))
        assertTrue(filter.contains("record.status == status"))
        assertTrue(strings.contains("action_filter_download_status"))
        assertTrue(strings.contains("action_filter_download_category"))
        assertTrue(strings.contains("action_clear_download_filters"))
        assertTrue(strings.contains("download_filter_all_status"))
        assertTrue(strings.contains("download_filter_all_categories"))
    }

    /**
     * 测试函数 `downloadsPageCanCancelInProgressRecords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `downloads Page Can Cancel In Progress Records` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadsPageCanCancelInProgressRecords() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageDialogController.kt"
        ).readText()
        val operations = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadRecordPageOperations.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(downloadsPage.contains("DownloadCancellationPolicy.canCancel(record)"))
        assertTrue(dialogController.contains("confirmCancelDownload(record)"))
        assertTrue(operations.contains("AndroidSystemDownloadRemover(activity)"))
        assertTrue(operations.contains("DownloadCanceller(downloadRecordRepository, systemDownloadRemover)"))
        assertTrue(operations.contains("downloadCanceller.cancel(record)"))
        assertTrue(dialogController.contains("R.string.action_cancel_download"))
        assertTrue(strings.contains("action_cancel_download"))
        assertTrue(strings.contains("download_status_canceled"))
        assertTrue(strings.contains("dialog_cancel_download_message"))
        assertTrue(strings.contains("toast_download_canceled"))
    }

    /**
     * 测试函数 `downloadsPageCanRemoveSingleRecords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `downloads Page Can Remove Single Records` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadsPageCanRemoveSingleRecords() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageDialogController.kt"
        ).readText()
        val operations = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadRecordPageOperations.kt"
        ).readText()
        val repository = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadRecordRepository.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(repository.contains("fun remove(downloadId: Long): Boolean"))
        assertTrue(downloadsPage.contains("showDownloadActionsDialog(record, retryable, cancelable)"))
        assertTrue(operations.contains("DownloadRecordRemover(downloadRecordRepository, systemDownloadRemover)"))
        assertTrue(dialogController.contains("confirmRemoveDownloadRecord(record)"))
        assertTrue(operations.contains("downloadRecordRemover.remove(record)"))
        assertTrue(strings.contains("action_remove_download_record"))
        assertTrue(strings.contains("dialog_remove_download_record_message"))
        assertTrue(strings.contains("toast_download_record_removed"))
    }

    /**
     * 测试函数 `downloadsPageCanCopyRecordSourceUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `downloads Page Can Copy Record Source Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadsPageCanCopyRecordSourceUrls() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageDialogController.kt"
        ).readText()
        val operations = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadRecordPageOperations.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(operations.contains("ClipboardTextActions.copyPlainText("))
        assertTrue(dialogController.contains("recordOperations.copyDownloadSourceUrl(record)"))
        assertTrue(operations.contains("record.sourceUrl"))
        assertTrue(strings.contains("action_copy_download_source"))
        assertTrue(strings.contains("clipboard_download_source_url"))
        assertTrue(strings.contains("toast_download_source_copied"))
    }

    /**
     * 测试函数 `downloadsPageCanShareCompletedFiles`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `downloads Page Can Share Completed Files` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadsPageCanShareCompletedFiles() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageDialogController.kt"
        ).readText()
        val launcher = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadedFileLauncher.kt"
        ).readText()

        assertTrue(dialogController.contains("record.status == DownloadStatus.COMPLETED"))
        assertTrue(dialogController.contains("shareDownloadedFile(record)"))
        assertTrue(launcher.contains("FileShareIntentFactory.create("))
        assertTrue(launcher.contains("displayName = record.fileName"))
        assertTrue(launcher.contains("mimeType = record.mimeType"))
        assertTrue(launcher.contains("startDownloadedFileIntent(intent, R.string.action_share_file)"))
        assertTrue(launcher.contains("startDownloadedFileIntent(intent, R.string.action_open_file)"))
        assertTrue(launcher.contains("private fun startDownloadedFileIntent(intent: Intent, chooserTitleRes: Int)"))
        assertTrue(launcher.contains("Intent.createChooser(intent, activity.getString(chooserTitleRes))"))
        assertEquals(1, Regex("Intent\\.createChooser\\(intent").findAll(launcher).count())
        assertEquals(1, Regex("catch \\(_: ActivityNotFoundException\\)").findAll(launcher).count())
        assertEquals(1, Regex("catch \\(_: SecurityException\\)").findAll(launcher).count())
    }



    /**
     * 测试函数 `repository`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `repository` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun repository(): String {
        return projectFile("src/main/java/com/example/videobrowser/download/DownloadRecordRepository.kt")
            .readText()
    }
}
