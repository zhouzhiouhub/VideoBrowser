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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
