package com.example.videobrowser.download

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadStatusWiringContractTest {
    @Test
    fun downloadControllerTracksStartedAndCompletedSystemDownloads() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadController.kt"
        ).readText()

        assertTrue(controller.contains("DownloadStatus.IN_PROGRESS"))
        assertTrue(controller.contains("DownloadManager.ACTION_DOWNLOAD_COMPLETE"))
        assertTrue(controller.contains("DownloadManager.Query().setFilterById(downloadId)"))
        assertTrue(controller.contains("DownloadManager.STATUS_SUCCESSFUL"))
        assertTrue(controller.contains("DownloadManager.STATUS_FAILED"))
        assertTrue(controller.contains("DownloadManager.COLUMN_REASON"))
        assertTrue(controller.contains("DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR"))
        assertTrue(controller.contains("DownloadManager.COLUMN_TOTAL_SIZE_BYTES"))
        assertTrue(controller.contains("statusReason"))
        assertTrue(controller.contains("downloadRecordRepository.updateSnapshot"))
    }

    @Test
    fun mainActivityDisposesDownloadStatusReceiver() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertTrue(mainActivity.contains("downloadController.dispose()"))
    }

    @Test
    fun downloadsPageShowsStatusInRecordSummary() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(downloadsPage.contains("downloadStatusTitleResId(record.status)"))
        assertTrue(downloadsPage.contains("DownloadStatusSynchronizer"))
        assertTrue(downloadsPage.contains("queryDownloadStatusSnapshot"))
        assertTrue(downloadsPage.contains("progressSummary(record)"))
        assertTrue(downloadsPage.contains("downloadFailureReasonText(record.statusReason)"))
        assertTrue(strings.contains("download_status_in_progress"))
        assertTrue(strings.contains("download_status_completed"))
        assertTrue(strings.contains("download_status_failed"))
        assertTrue(strings.contains("download_progress_percent"))
        assertTrue(strings.contains("download_progress_downloaded"))
        assertTrue(strings.contains("download_failure_reason"))
    }

    @Test
    fun downloadsPageSupportsSearchFiltering() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val search = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadRecordSearch.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(downloadsPage.contains("DownloadRecordSearch.filter(allRecords, query)"))
        assertTrue(downloadsPage.contains("R.string.action_search_download_records"))
        assertTrue(downloadsPage.contains("private fun showSearchDialog"))
        assertTrue(downloadsPage.contains("R.string.action_clear_search"))
        assertTrue(downloadsPage.contains("R.string.dialog_download_records_search_empty"))
        assertTrue(search.contains("record.fileName"))
        assertTrue(search.contains("record.sourceUrl"))
        assertTrue(strings.contains("action_search_download_records"))
        assertTrue(strings.contains("action_search_download_records_summary"))
        assertTrue(strings.contains("hint_download_records_search"))
        assertTrue(strings.contains("dialog_download_records_search_empty"))
    }

    @Test
    fun downloadsPageSupportsStatusAndCategoryFilters() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
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
        assertTrue(downloadsPage.contains("private fun showStatusFilterDialog"))
        assertTrue(downloadsPage.contains("private fun showCategoryFilterDialog"))
        assertTrue(downloadsPage.contains("setSingleChoiceItems(labels.toTypedArray(), checkedIndex)"))
        assertTrue(downloadsPage.contains("DownloadStatus.entries"))
        assertTrue(downloadsPage.contains("DownloadCategory.entries"))
        assertTrue(filter.contains("DownloadCategory.from(record.mimeType, record.fileName)"))
        assertTrue(filter.contains("record.status == status"))
        assertTrue(strings.contains("action_filter_download_status"))
        assertTrue(strings.contains("action_filter_download_category"))
        assertTrue(strings.contains("action_clear_download_filters"))
        assertTrue(strings.contains("download_filter_all_status"))
        assertTrue(strings.contains("download_filter_all_categories"))
    }

    @Test
    fun downloadsPageCanCancelInProgressRecords() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(downloadsPage.contains("DownloadCancellationPolicy.canCancel(record)"))
        assertTrue(downloadsPage.contains("confirmCancelDownload(record)"))
        assertTrue(downloadsPage.contains("DownloadCanceller(downloadRecordRepository)"))
        assertTrue(downloadsPage.contains("downloadManager.remove(*downloadIds)"))
        assertTrue(downloadsPage.contains("R.string.action_cancel_download"))
        assertTrue(strings.contains("action_cancel_download"))
        assertTrue(strings.contains("download_status_canceled"))
        assertTrue(strings.contains("dialog_cancel_download_message"))
        assertTrue(strings.contains("toast_download_canceled"))
    }

    @Test
    fun downloadsPageCanRemoveSingleRecords() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val repository = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadRecordRepository.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(repository.contains("fun remove(downloadId: Long): Boolean"))
        assertTrue(downloadsPage.contains("showDownloadActionsDialog(record, retryable, cancelable)"))
        assertTrue(downloadsPage.contains("DownloadRecordRemover(downloadRecordRepository)"))
        assertTrue(downloadsPage.contains("confirmRemoveDownloadRecord(record)"))
        assertTrue(downloadsPage.contains("downloadManager.remove(*downloadIds)"))
        assertTrue(strings.contains("action_remove_download_record"))
        assertTrue(strings.contains("dialog_remove_download_record_message"))
        assertTrue(strings.contains("toast_download_record_removed"))
    }

    @Test
    fun downloadsPageCanCopyRecordSourceUrls() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(downloadsPage.contains("ClipData.newPlainText"))
        assertTrue(downloadsPage.contains("Context.CLIPBOARD_SERVICE"))
        assertTrue(downloadsPage.contains("copyDownloadSourceUrl(record)"))
        assertTrue(downloadsPage.contains("record.sourceUrl"))
        assertTrue(strings.contains("action_copy_download_source"))
        assertTrue(strings.contains("clipboard_download_source_url"))
        assertTrue(strings.contains("toast_download_source_copied"))
    }

    @Test
    fun downloadsPageCanShareCompletedFiles() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()

        assertTrue(downloadsPage.contains("record.status == DownloadStatus.COMPLETED"))
        assertTrue(downloadsPage.contains("shareDownloadedFile(record)"))
        assertTrue(downloadsPage.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(downloadsPage.contains("putExtra(Intent.EXTRA_STREAM, uri)"))
        assertTrue(downloadsPage.contains("ClipData.newUri(activity.contentResolver, record.fileName, uri)"))
        assertTrue(downloadsPage.contains("Intent.createChooser(intent, activity.getString(R.string.action_share_file))"))
        assertTrue(downloadsPage.contains("Intent.FLAG_GRANT_READ_URI_PERMISSION"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
