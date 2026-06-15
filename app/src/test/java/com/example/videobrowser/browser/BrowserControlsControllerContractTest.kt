package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserControlsControllerContractTest {
    @Test
    fun refreshButtonStopsLoadingWhilePageIsLoading() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserControlsController.kt"
        ).readText()
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val stopIcon = projectFile("src/main/res/drawable/ic_stop_24.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("fun stopLoading()"))
        assertTrue(browserManager.contains("webView.stopLoading()"))
        assertTrue(controller.contains("private var isPageLoading = false"))
        assertTrue(controller.contains("if (isPageLoading)"))
        assertTrue(controller.contains("browserManager().stopLoading()"))
        assertTrue(controller.contains("browserManager().reload()"))
        assertTrue(controller.contains("this.isPageLoading = isPageLoading"))
        assertTrue(controller.contains("refreshButton.visibility = if (visibility.showRefresh) View.VISIBLE else View.GONE"))
        assertTrue(controller.contains("private fun updateRefreshButton()"))
        assertTrue(controller.contains("R.string.action_stop_loading"))
        assertTrue(controller.contains("R.drawable.ic_stop_24"))
        assertTrue(controller.contains("R.id.refreshButton"))
        assertTrue(controller.contains("bottomBar.findViewById<View>(id).visibility == View.VISIBLE"))
        assertTrue(strings.contains("action_stop_loading"))
        assertTrue(stopIcon.contains("M6,6H18V18H6Z"))
        assertTrue(readme.contains("加载中可停止加载"))
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
