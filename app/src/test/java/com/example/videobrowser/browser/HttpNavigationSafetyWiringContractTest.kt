package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpNavigationSafetyWiringContractTest {
    @Test
    fun mainActivityConfirmsHttpsToHttpTopLevelNavigation() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt").readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation"))
        assertTrue(mainActivity.contains("private fun loadUrlInternal(url: String, allowInsecureNavigation: Boolean)"))
        assertTrue(mainActivity.contains("private fun loadUrlAfterInsecureNavigationConfirmation(url: String)"))
        assertTrue(mainActivity.contains("private fun showInsecureNavigationConfirmation(url: String)"))
        assertTrue(mainActivity.contains("view?.stopLoading()"))
        assertTrue(mainActivity.contains("R.string.title_confirm_insecure_navigation"))
        assertTrue(mainActivity.contains("R.string.dialog_confirm_insecure_navigation_message"))
        assertTrue(strings.contains("title_confirm_insecure_navigation"))
        assertTrue(strings.contains("dialog_confirm_insecure_navigation_message"))
        assertTrue(readme.contains("HTTPS 页面跳转到 HTTP 明文页面前会先确认"))
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
