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
        assertTrue(mainActivity.contains("findInPageController.clear"))
        assertTrue(mainActivity.contains("findInPage = ::showFindInPageDialog"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
