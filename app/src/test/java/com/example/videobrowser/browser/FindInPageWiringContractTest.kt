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

    @Test
    fun mainActivityShowsFindDialogAndCallsController() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertTrue(mainActivity.contains("FindInPageController"))
        assertTrue(mainActivity.contains("findInPageController.search"))
        assertTrue(mainActivity.contains("findInPageController.findNext"))
        assertTrue(mainActivity.contains("findInPageController.findPrevious"))
        assertTrue(mainActivity.contains("findInPageController.clear"))
        assertTrue(mainActivity.contains("setFindResultListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->"))
        assertTrue(mainActivity.contains("private fun findInPageStatusText("))
        assertTrue(mainActivity.contains("R.string.find_in_page_status_matches"))
        assertTrue(mainActivity.contains("currentBrowserManager().setFindResultListener(null)"))
        assertTrue(mainActivity.contains("dialog.getButton(AlertDialog.BUTTON_NEGATIVE)"))
        assertTrue(mainActivity.contains("dialog.setOnDismissListener"))
        assertTrue(mainActivity.contains("findInPage = ::showFindInPageDialog"))
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()
        assertTrue(strings.contains("action_find_previous"))
        assertTrue(strings.contains("find_in_page_status_matches"))
        assertTrue(strings.contains("find_in_page_status_no_matches"))
        assertTrue(readme.contains("上一处或下一处匹配，并显示匹配数量"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
