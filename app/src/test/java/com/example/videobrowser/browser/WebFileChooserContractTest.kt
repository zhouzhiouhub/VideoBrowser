package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Web File Chooser Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class WebFileChooserContractTest {
    /**
     * 测试函数 `chromeClientForwardsWebFileChooserRequests`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `chrome Client Forwards Web File Chooser Requests` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun chromeClientForwardsWebFileChooserRequests() {
        val chromeClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/ChromeClient.kt"
        ).readText()

        assertTrue(chromeClient.contains("ValueCallback<Array<Uri>>"))
        assertTrue(chromeClient.contains("FileChooserParams"))
        assertTrue(chromeClient.contains("fileChooserRequested"))
        assertTrue(chromeClient.contains("override fun onShowFileChooser"))
        assertTrue(chromeClient.contains("return fileChooserRequested(filePathCallback, fileChooserParams)"))
    }

    /**
     * 测试函数 `mainActivityLaunchesSystemPickerAndReturnsChosenFilesToWebView`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Launches System Picker And Returns Chosen Files To Web View` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityLaunchesSystemPickerAndReturnsChosenFilesToWebView() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val chromeClientController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserChromeClientController.kt"
        ).readText()
        val fileChooserController = projectFile(
            "src/main/java/com/example/videobrowser/browser/WebFileChooserController.kt"
        ).readText()

        assertTrue(mainActivity.contains("ActivityResultContracts.StartActivityForResult()"))
        assertTrue(mainActivity.contains("webFileChooserController.handleActivityResult(result.resultCode, result.data)"))
        assertTrue(mainActivity.contains("private lateinit var webFileChooserController: WebFileChooserController"))
        assertTrue(fileChooserController.contains("pendingFileChooserCallback: ValueCallback<Array<Uri>>?"))
        assertTrue(fileChooserController.contains("FileChooserParams.parseResult"))
        assertTrue(fileChooserController.contains("pendingFileChooserCallback?.onReceiveValue"))
        assertTrue(chromeClientController.contains("fileChooserRequested = webFileChooserController::showFileChooser"))
        assertTrue(chromeClientController.contains("fun cancelPendingWebFileChooser()"))
        assertTrue(chromeClientController.contains("webFileChooserController.cancelPending()"))
        assertTrue(mainActivity.contains("browserChromeClientController.cancelPendingWebFileChooser()"))
        assertTrue(fileChooserController.contains("R.string.toast_file_chooser_unavailable"))
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
