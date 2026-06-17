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
    /**
     * 测试函数 `pageActionsOpenLocalWebArchivesInsideBrowser`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `page Actions Open Local Web Archives Inside Browser` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun pageActionsOpenLocalWebArchivesInsideBrowser() {
        val pageActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val localDocumentEntryController = projectFile(
            "src/main/java/com/example/videobrowser/localfiles/LocalDocumentEntryController.kt"
        ).readText()
        val pageActionAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageActionAssemblyController.kt"
        )
            .readText()
        val readme = projectFile("README.md").readText()

        assertTrue(pageActionsController.contains("openLocalArchiveInBrowser: (String) -> Unit"))
        assertTrue(pageActionsController.contains("LocalWebArchivePolicy.isWebArchive(title, resolvedMimeType)"))
        assertTrue(pageActionsController.contains("openLocalArchiveInBrowser(uri.toString())"))
        assertTrue(pageActionAssembly.contains("openLocalArchiveInBrowser = localDocumentEntryController::loadLocalDocumentUrlInBrowser"))
        assertTrue(localDocumentEntryController.contains("fun loadLocalDocumentUrlInBrowser(url: String)"))
        assertTrue(localDocumentEntryController.contains("currentBrowserManager().load(url)"))
        assertTrue(readme.contains("MHTML/MHT 网页归档会在浏览器中打开"))
    }

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
