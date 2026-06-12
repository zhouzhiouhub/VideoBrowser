package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ForwardPageActionContractTest {
    @Test
    fun pageToolsCanNavigateForwardWhenAvailable() {
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("fun goForward(): Boolean"))
        assertTrue(browserManager.contains("fun canGoForward(): Boolean"))
        assertTrue(pages.contains("R.string.action_forward"))
        assertTrue(pages.contains("R.string.action_forward_summary"))
        assertTrue(pages.contains("R.drawable.ic_arrow_forward_24"))
        assertTrue(pages.contains("enabled = hasPage && browserManager().canGoForward()"))
        assertTrue(pages.contains("runPageAction { browserManager().goForward() }"))
        assertTrue(strings.contains("action_forward_summary"))
        assertTrue(readme.contains("页面工具可执行前进"))
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
