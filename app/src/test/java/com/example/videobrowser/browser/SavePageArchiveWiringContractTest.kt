package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Save Page Archive Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SavePageArchiveWiringContractTest {
    /**
     * 测试函数 `functionCenterHasSavePageArchiveAction`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `function Center Has Save Page Archive Action` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun functionCenterHasSavePageArchiveAction() {
        val functionCenterPages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val rootActionCatalog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionCatalog.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val fileIcon = projectFile("src/main/res/drawable/ic_file_24.xml").readText()

        assertTrue(rootActionCatalog.contains("SAVE_PAGE_ARCHIVE"))
        assertTrue(rootActionCatalog.contains("FunctionCenterRootAction.SAVE_PAGE_ARCHIVE.takeIf { hasPage }"))
        assertTrue(functionCenterPages.contains("saveCurrentPageArchive: () -> Unit"))
        assertTrue(functionCenterPages.contains("R.string.action_save_page_archive"))
        assertTrue(functionCenterPages.contains("R.string.action_save_page_archive_summary"))
        assertTrue(functionCenterPages.contains("R.drawable.ic_file_24"))
        assertTrue(functionCenterPages.contains("runPageAction(saveCurrentPageArchive)"))
        assertTrue(strings.contains("action_save_page_archive"))
        assertTrue(strings.contains("action_save_page_archive_summary"))
        assertTrue(fileIcon.contains("<vector"))
    }

    /**
     * 测试函数 `mainActivitySavesCurrentWebViewAsMhtmlArchive`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Saves Current Web View As Mhtml Archive` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivitySavesCurrentWebViewAsMhtmlArchive() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("ActivityResultContracts.CreateDocument(PAGE_ARCHIVE_MIME_TYPE)"))
        assertTrue(mainActivity.contains("private fun saveCurrentPageArchive()"))
        assertTrue(mainActivity.contains("activeWebView.saveWebArchive("))
        assertTrue(mainActivity.contains("pageArchiveExportLauncher.launch(currentPageArchiveFileName(pageUrl))"))
        assertTrue(mainActivity.contains("PageArchiveFileName.create("))
        assertTrue(mainActivity.contains("private fun exportPageArchiveToUri(archiveFile: File, uri: Uri)"))
        assertTrue(mainActivity.contains("saveCurrentPageArchive = ::saveCurrentPageArchive"))
        assertTrue(mainActivity.contains("R.string.toast_page_archive_saved"))
        assertTrue(mainActivity.contains("R.string.toast_page_archive_failed"))
        assertTrue(mainActivity.contains("R.string.toast_page_archive_unavailable"))
        assertTrue(strings.contains("toast_page_archive_saved"))
        assertTrue(strings.contains("toast_page_archive_failed"))
        assertTrue(strings.contains("toast_page_archive_unavailable"))
        assertTrue(readme.contains("保存网页为 MHTML 离线归档"))
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
