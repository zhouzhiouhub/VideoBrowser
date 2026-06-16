package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Tab Session Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
    fun tabSessionRepositoryRestoresOnlyWebUrls() {
        val repository = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabSessionRepository.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(repository.contains("private fun normalizeRestorableWebUrl(url: String?): String?"))
        assertTrue(repository.contains("scheme != \"http\" && scheme != \"https\""))
        assertTrue(repository.contains("uri.host.isNullOrBlank()"))
        assertTrue(readme.contains("标准标签页会话只恢复带主机名的 HTTP/HTTPS 页面 URL"))
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
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
