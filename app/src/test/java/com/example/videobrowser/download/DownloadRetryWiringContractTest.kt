package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Retry Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadRetryWiringContractTest {
    @Test
    fun downloadControllerCanRetrySavedRecords() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadController.kt"
        ).readText()

        assertTrue(controller.contains("fun retry(record: DownloadRecord)"))
        assertTrue(controller.contains("url = record.sourceUrl"))
        assertTrue(controller.contains("mimeType = record.mimeType"))
    }

    @Test
    fun downloadsPageRetriesFailedRecordsInsteadOfOpeningMissingFiles() {
        val downloadsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(downloadsPage.contains("retryDownload: (DownloadRecord) -> Unit"))
        assertTrue(downloadsPage.contains("DownloadRetryPolicy.canRetry(record)"))
        assertTrue(downloadsPage.contains("retryDownload(record)"))
        assertTrue(downloadsPage.contains("R.string.action_retry_download"))
        assertTrue(strings.contains("action_retry_download"))
    }

    @Test
    fun mainActivityPassesDownloadControllerRetryIntoFunctionCenter() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val functionCenterPages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()

        assertTrue(functionCenterPages.contains("retryDownload: (DownloadRecord) -> Unit"))
        assertTrue(functionCenterPages.contains("retryDownload = retryDownload"))
        assertTrue(mainActivity.contains("retryDownload = downloadController::retry"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
