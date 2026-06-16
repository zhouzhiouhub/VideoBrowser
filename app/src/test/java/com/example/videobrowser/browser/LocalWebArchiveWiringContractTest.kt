package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Local Web Archive Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalWebArchiveWiringContractTest {
    @Test
    fun pageActionsOpenLocalWebArchivesInsideBrowser() {
        val pageActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val readme = projectFile("README.md").readText()

        assertTrue(pageActionsController.contains("openLocalArchiveInBrowser: (String) -> Unit"))
        assertTrue(pageActionsController.contains("LocalWebArchivePolicy.isWebArchive(title, resolvedMimeType)"))
        assertTrue(pageActionsController.contains("openLocalArchiveInBrowser(uri.toString())"))
        assertTrue(mainActivity.contains("openLocalArchiveInBrowser = ::loadLocalDocumentUrlInBrowser"))
        assertTrue(mainActivity.contains("private fun loadLocalDocumentUrlInBrowser(url: String)"))
        assertTrue(mainActivity.contains("currentBrowserManager().load(url)"))
        assertTrue(readme.contains("MHTML/MHT 网页归档会在浏览器中打开"))
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
