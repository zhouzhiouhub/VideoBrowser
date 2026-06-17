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
    /**
     * 测试函数 `downloadControllerCanRetrySavedRecords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `download Controller Can Retry Saved Records` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadControllerCanRetrySavedRecords() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadController.kt"
        ).readText()

        assertTrue(controller.contains("fun retry(record: DownloadRecord)"))
        assertTrue(controller.contains("url = record.sourceUrl"))
        assertTrue(controller.contains("mimeType = record.mimeType"))
    }

    /**
     * 测试函数 `downloadsPageRetriesFailedRecordsInsteadOfOpeningMissingFiles`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `downloads Page Retries Failed Records Instead Of Opening Missing Files` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `mainActivityPassesDownloadControllerRetryIntoFunctionCenter`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Passes Download Controller Retry Into Function Center` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityPassesDownloadControllerRetryIntoFunctionCenter() {
        val functionCenterAssembly = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterAssemblyController.kt"
        )
            .readText()
        val functionCenterPages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()

        assertTrue(functionCenterPages.contains("retryDownload: (DownloadRecord) -> Unit"))
        assertTrue(functionCenterPages.contains("retryDownload = retryDownload"))
        assertTrue(functionCenterAssembly.contains("retryDownload = downloadController::retry"))
    }

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
