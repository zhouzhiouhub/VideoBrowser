package com.example.videobrowser.storage

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Bookmark Import Export Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkImportExportWiringContractTest {
    /**
     * 测试函数 `mainActivityWiresBookmarkImportExportThroughSaf`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Wires Bookmark Import Export Through Saf` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityWiresBookmarkImportExportThroughSaf() {
        val functionCenterAssembly = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterAssemblyController.kt"
        ).readText()
        val activityResultLaunchers = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserActivityResultLaunchers.kt"
        ).readText()
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/storage/BookmarkImportExportController.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(activityResultLaunchers.contains("ActivityResultContracts.CreateDocument(\"text/plain\")"))
        assertTrue(activityResultLaunchers.contains("ActivityResultContracts.OpenDocument()"))
        assertTrue(activityResultLaunchers.contains("bookmarkImportExportController()?.exportToUri(uri)"))
        assertTrue(activityResultLaunchers.contains("bookmarkImportExportController()?.importFromUri(uri)"))
        assertTrue(activityResultLaunchers.contains("bookmarkExportLauncher.launch(BookmarkImportExportController.EXPORT_FILE_NAME)"))
        assertTrue(activityResultLaunchers.contains("bookmarkImportLauncher.launch(BookmarkImportExportController.IMPORT_MIME_TYPES)"))
        assertTrue(controller.contains("const val EXPORT_FILE_NAME = \"videobrowser-bookmarks.txt\""))
        assertTrue(controller.contains("val IMPORT_MIME_TYPES = arrayOf(\"text/plain\", \"application/json\", \"*/*\")"))
        assertTrue(controller.contains("savedPageRepository.exportBookmarks()"))
        assertTrue(controller.contains("contentResolver.openOutputStream(uri)"))
        assertTrue(controller.contains("contentResolver.openInputStream(uri)"))
        assertTrue(controller.contains("savedPageRepository.importBookmarks(payload)"))
        assertTrue(controller.contains("updateBookmarkButton()"))
        assertTrue(functionCenterAssembly.contains("exportBookmarks = activityResultLaunchers::launchBookmarkExport"))
        assertTrue(functionCenterAssembly.contains("importBookmarks = activityResultLaunchers::launchBookmarkImport"))
        assertTrue(pages.contains("exportBookmarks: () -> Unit"))
        assertTrue(pages.contains("importBookmarks: () -> Unit"))
        assertTrue(strings.contains("toast_bookmarks_exported"))
        assertTrue(strings.contains("toast_bookmarks_imported"))
        assertTrue(readme.contains("收藏夹支持文件夹分组、导入和导出"))
    }

}
