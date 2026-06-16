package com.example.videobrowser.storage

/**
 * 测试阅读提示：
 * 这个测试文件验证“Bookmark Import Export Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkImportExportWiringContractTest {
    @Test
    fun mainActivityWiresBookmarkImportExportThroughSaf() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt").readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(mainActivity.contains("ActivityResultContracts.CreateDocument(\"text/plain\")"))
        assertTrue(mainActivity.contains("ActivityResultContracts.OpenDocument()"))
        assertTrue(mainActivity.contains("bookmarkExportLauncher.launch(BOOKMARK_EXPORT_FILE_NAME)"))
        assertTrue(mainActivity.contains("savedPageRepository.exportBookmarks()"))
        assertTrue(mainActivity.contains("contentResolver.openOutputStream(uri)"))
        assertTrue(mainActivity.contains("bookmarkImportLauncher.launch(arrayOf(\"text/plain\", \"application/json\", \"*/*\"))"))
        assertTrue(mainActivity.contains("contentResolver.openInputStream(uri)"))
        assertTrue(mainActivity.contains("savedPageRepository.importBookmarks(payload)"))
        assertTrue(mainActivity.contains("exportBookmarks = ::exportBookmarks"))
        assertTrue(mainActivity.contains("importBookmarks = ::importBookmarks"))
        assertTrue(pages.contains("exportBookmarks: () -> Unit"))
        assertTrue(pages.contains("importBookmarks: () -> Unit"))
        assertTrue(strings.contains("toast_bookmarks_exported"))
        assertTrue(strings.contains("toast_bookmarks_imported"))
        assertTrue(readme.contains("收藏夹支持重命名标题、文件夹分组、导入和导出"))
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
