package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserTabSessionWiringContractTest {
    @Test
    fun mainActivityOwnsBrowserTabsAndSessionBinding() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertTrue(mainActivity.contains("BrowserTabStore"))
        assertTrue(mainActivity.contains("BrowserTabSessionBinding"))
        assertTrue(mainActivity.contains("BrowserTabSessionRepository"))
        assertTrue(mainActivity.contains("standardTabSessionBinding"))
        assertTrue(mainActivity.contains("restoreStandardTabSession()"))
        assertTrue(mainActivity.contains("saveStandardTabSession()"))
        assertTrue(mainActivity.contains("openInitialStandardPage()"))
        assertTrue(mainActivity.contains("standardTabSessionBinding.handlePageMetadataChanged(url, title)"))
    }

    @Test
    fun sessionControllerExposesPageMetadataCallback() {
        val sessionController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserSessionController.kt"
        ).readText()

        assertTrue(sessionController.contains("onPageMetadataChanged: (String?, String?) -> Unit"))
        assertTrue(sessionController.contains("onPageMetadataChanged(currentPageUrl, currentPageTitle)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
