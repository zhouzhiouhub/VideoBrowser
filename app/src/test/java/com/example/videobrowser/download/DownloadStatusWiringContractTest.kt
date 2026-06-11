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
        assertTrue(controller.contains("downloadRecordRepository.updateStatus"))
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
        assertTrue(strings.contains("download_status_in_progress"))
        assertTrue(strings.contains("download_status_completed"))
        assertTrue(strings.contains("download_status_failed"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
