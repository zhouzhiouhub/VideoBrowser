package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SavePageArchiveWiringContractTest {
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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
