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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
