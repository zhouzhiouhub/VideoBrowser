package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Find In Page Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FindInPageWiringContractTest {
    /**
     * 测试函数 `browserManagerExposesWebViewFindOperations`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Manager Exposes Web View Find Operations` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserManagerExposesWebViewFindOperations() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()

        assertTrue(browserManager.contains("fun findAllAsync(query: String)"))
        assertTrue(browserManager.contains("webView.findAllAsync(query)"))
        assertTrue(browserManager.contains("fun findNext(forward: Boolean"))
        assertTrue(browserManager.contains("webView.findNext(forward)"))
        assertTrue(browserManager.contains("fun clearFindMatches()"))
        assertTrue(browserManager.contains("webView.clearMatches()"))
        assertTrue(browserManager.contains("fun setFindResultListener(listener: ((Int, Int, Boolean) -> Unit)?)"))
        assertTrue(browserManager.contains("targetWebView.setFindListener("))
        assertTrue(browserManager.contains("WebView.FindListener"))
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/FindInPageController.kt"
        ).readText()
        assertTrue(controller.contains("fun findPrevious()"))
        assertTrue(controller.contains("findNext(forward = false)"))
    }

    /**
     * 测试函数 `functionCenterHasFindInPageAction`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `function Center Has Find In Page Action` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun functionCenterHasFindInPageAction() {
        val functionCenterPages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(functionCenterPages.contains("findInPage: () -> Unit"))
        assertTrue(functionCenterPages.contains("R.string.action_find_in_page"))
        assertTrue(functionCenterPages.contains("runPageAction(findInPage)"))
        assertTrue(strings.contains("action_find_in_page"))
        assertTrue(strings.contains("action_find_in_page_summary"))
    }

    /**
     * 测试函数 `mainActivityShowsFindDialogAndCallsController`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Shows Find Dialog And Calls Controller` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityShowsFindDialogAndCallsController() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val findDialogController = projectFile(
            "src/main/java/com/example/videobrowser/browser/FindInPageDialogController.kt"
        ).readText()
        val pageToolEntryController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageToolEntryController.kt"
        ).readText()

        assertTrue(mainActivity.contains("FindInPageController"))
        assertTrue(mainActivity.contains("private lateinit var findInPageDialogController: FindInPageDialogController"))
        assertTrue(pageToolEntryController.contains("findInPageDialogController.showDialog()"))
        assertTrue(mainActivity.contains("setFindResultListener = { listener ->"))
        assertTrue(mainActivity.contains("browserStandardWebViewHostController.currentBrowserManager()"))
        assertTrue(mainActivity.contains(".setFindResultListener(listener)"))
        assertTrue(findDialogController.contains("findInPageController.search"))
        assertTrue(findDialogController.contains("findInPageController.findNext"))
        assertTrue(findDialogController.contains("findInPageController.findPrevious"))
        assertTrue(findDialogController.contains("findInPageController.clear"))
        assertTrue(findDialogController.contains("setFindResultListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->"))
        assertTrue(findDialogController.contains("private fun statusText("))
        assertTrue(findDialogController.contains("R.string.find_in_page_status_matches"))
        assertTrue(findDialogController.contains("setFindResultListener(null)"))
        assertTrue(findDialogController.contains("dialog.getButton(AlertDialog.BUTTON_NEGATIVE)"))
        assertTrue(findDialogController.contains("dialog.setOnDismissListener"))
        assertTrue(mainActivity.contains("findInPage = browserPageToolEntryController::showFindInPageDialog"))
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()
        assertTrue(strings.contains("action_find_previous"))
        assertTrue(strings.contains("find_in_page_status_matches"))
        assertTrue(strings.contains("find_in_page_status_no_matches"))
        assertTrue(readme.contains("上一处或下一处匹配，并显示匹配数量"))
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
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
