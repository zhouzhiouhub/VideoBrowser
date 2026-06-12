package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserTabWebViewWiringContractTest {
    @Test
    fun mainActivityUsesTabWebViewRegistryForStandardTabs() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertTrue(mainActivity.contains("BrowserTabWebViewRegistry<WebView>"))
        assertTrue(mainActivity.contains("standardTabWebViews.openTab"))
        assertTrue(mainActivity.contains("standardTabWebViews.switchTo"))
        assertTrue(mainActivity.contains("standardTabWebViews.closeTab"))
        assertTrue(mainActivity.contains("standardTabWebViews.closeOtherTabs"))
        assertTrue(mainActivity.contains("standardTabWebViews.openTab("))
        assertTrue(mainActivity.contains("createStandardTabWebView"))
    }

    @Test
    fun switchTabDoesNotReloadExistingTabUrl() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val switchTabBody = mainActivity.substringAfter("private fun switchTab(tabId: Long)")
            .substringBefore("private fun closeTab")

        assertFalse(switchTabBody.contains("loadUrl"))
        assertTrue(switchTabBody.contains("showActiveTab"))
    }

    @Test
    fun reopenClosedTabCreatesStandardWebViewForRestoredTab() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val reopenClosedTabBody = mainActivity.substringAfter("private fun reopenClosedTab()")
            .substringBefore("private fun switchTab")

        assertTrue(reopenClosedTabBody.contains("standardTabStore.reopenClosedTab()"))
        assertTrue(reopenClosedTabBody.contains("standardTabWebViews.activate(reopenedTab.id)"))
        assertTrue(reopenClosedTabBody.contains("saveStandardTabSession()"))
        assertTrue(reopenClosedTabBody.contains("reopenedTab.url?.let(::loadUrl) ?: openHomePage()"))
    }

    @Test
    fun duplicateTabCreatesIndependentStandardWebView() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val duplicateTabBody = mainActivity.substringAfter("private fun duplicateTab(tabId: Long)")
            .substringBefore("private fun showActiveTab")

        assertTrue(duplicateTabBody.contains("standardTabWebViews.openTab("))
        assertTrue(duplicateTabBody.contains("view = createStandardTabWebView()"))
        assertTrue(duplicateTabBody.contains("url = sourceTab.url"))
        assertTrue(duplicateTabBody.contains("title = sourceTab.title"))
        assertTrue(duplicateTabBody.contains("sourceTab.url?.let(::loadUrl) ?: openHomePage()"))
    }

    @Test
    fun openUrlInNewTabCreatesIndependentStandardWebView() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val openUrlInNewTabBody = mainActivity.substringAfter("private fun openUrlInNewTab(url: String)")
            .substringBefore("private fun showActiveTab")

        assertTrue(openUrlInNewTabBody.contains("standardTabWebViews.openTab("))
        assertTrue(openUrlInNewTabBody.contains("view = createStandardTabWebView()"))
        assertTrue(openUrlInNewTabBody.contains("url = url"))
        assertTrue(openUrlInNewTabBody.contains("loadUrl(url)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
