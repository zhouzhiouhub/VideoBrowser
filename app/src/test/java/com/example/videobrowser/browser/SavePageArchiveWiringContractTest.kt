package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Save Page Archive Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
        val rootActionSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionSection.kt"
        ).readText()
        val rootActionCatalog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionCatalog.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val fileIcon = projectFile("src/main/res/drawable/ic_file_24.xml").readText()

        assertTrue(rootActionCatalog.contains("SAVE_PAGE_ARCHIVE"))
        assertTrue(rootActionCatalog.contains("FunctionCenterRootAction.SAVE_PAGE_ARCHIVE.takeIf { hasPage }"))
        assertTrue(functionCenterPages.contains("saveCurrentPageArchive: () -> Unit"))
        assertTrue(functionCenterPages.contains("saveCurrentPageArchive = saveCurrentPageArchive"))
        assertTrue(rootActionSection.contains("R.string.action_save_page_archive"))
        assertTrue(rootActionSection.contains("R.string.action_save_page_archive_summary"))
        assertTrue(rootActionSection.contains("R.drawable.ic_file_24"))
        assertTrue(rootActionSection.contains("runPageAction(saveCurrentPageArchive)"))
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
        val functionCenterAssembly = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterAssemblyController.kt"
        ).readText()
        val pageActionAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageActionAssemblyController.kt"
        ).readText()
        val activityResultLaunchers = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityResultLaunchers.kt"
        ).readText()
        val pageToolEntryController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageToolEntryController.kt"
        ).readText()
        val pageArchiveController = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageArchiveController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(activityResultLaunchers.contains("ActivityResultContracts.CreateDocument(PageArchiveController.MIME_TYPE)"))
        assertTrue(pageToolEntryController.contains("fun saveCurrentPageArchive()"))
        assertTrue(pageToolEntryController.contains("pageArchiveController.saveCurrentPageArchive()"))
        assertTrue(activityResultLaunchers.contains("pageArchiveController()?.handleExportResult(uri)"))
        assertTrue(pageArchiveController.contains("activeWebView().saveWebArchive("))
        assertTrue(pageArchiveController.contains("launchArchiveExport(archiveFileName(pageUrl))"))
        assertTrue(pageArchiveController.contains("PageArchiveFileName.create("))
        assertTrue(pageArchiveController.contains("private fun exportArchiveFileToUri(archiveFile: File, uri: Uri)"))
        assertTrue(pageArchiveController.contains("const val MIME_TYPE = \"multipart/related\""))
        assertTrue(pageActionAssembly.contains("launchArchiveExport = activityResultLaunchers::launchPageArchiveExport"))
        assertTrue(functionCenterAssembly.contains("saveCurrentPageArchive = browserPageToolEntryController::saveCurrentPageArchive"))
        assertTrue(pageArchiveController.contains("R.string.toast_page_archive_saved"))
        assertTrue(pageArchiveController.contains("R.string.toast_page_archive_failed"))
        assertTrue(pageArchiveController.contains("R.string.toast_page_archive_unavailable"))
        assertTrue(strings.contains("toast_page_archive_saved"))
        assertTrue(strings.contains("toast_page_archive_failed"))
        assertTrue(strings.contains("toast_page_archive_unavailable"))
        assertTrue(readme.contains("保存网页为 MHTML 离线归档"))
    }

}
