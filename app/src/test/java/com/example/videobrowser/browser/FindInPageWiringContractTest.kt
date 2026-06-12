package com.example.videobrowser.browser

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
        assertTrue(mainActivity.contains("dialog.getButton(AlertDialog.BUTTON_NEGATIVE)"))
        assertTrue(mainActivity.contains("dialog.setOnDismissListener"))
        assertTrue(mainActivity.contains("findInPage = ::showFindInPageDialog"))
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()
        assertTrue(strings.contains("action_find_previous"))
        assertTrue(readme.contains("上一处或下一处匹配"))
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
