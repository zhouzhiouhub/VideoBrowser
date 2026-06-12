package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserDataManagementPageTest {
    @Test
    fun cookieParserOnlyExposesCookieNames() {
        val cookies = BrowserCookieParser.parse("session=secret-token; theme=dark")

        assertEquals(listOf("session", "theme"), cookies.map { cookie -> cookie.name })
        assertFalse(
            BrowserCookieItem::class.java.declaredFields.any { field ->
                field.name == "valuePreview"
            }
        )
    }

    @Test
    fun siteDataSummaryOnlyShowsUsedStorage() {
        assertEquals("1.5 KB", BrowserDataDisplayFormatter.siteDataUsageSummary(1536L))
    }

    @Test
    fun browserDataManagementPageCanClearBookmarks() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementPage.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("fun showBookmarkData"))
        assertTrue(page.contains("savedPageRepository.bookmarks().size"))
        assertTrue(page.contains("savedPageRepository.clear(SavedPageRepository.SavedPageCollection.BOOKMARKS)"))
        assertTrue(page.contains("R.string.bookmark_record_count"))
        assertTrue(page.contains("R.string.dialog_clear_bookmarks_message"))
        assertTrue(settings.contains("showBookmarkManager: () -> Unit"))
        assertTrue(settings.contains("showBookmarkManager()"))
        assertTrue(settings.contains("R.string.action_manage_bookmarks_summary"))
        assertTrue(pages.contains("showBookmarkManager = { browserDataManagementPage.showBookmarkData() }"))
        assertTrue(strings.contains("title_bookmark_data_management"))
        assertTrue(strings.contains("action_manage_bookmarks_summary"))
        assertTrue(strings.contains("action_clear_bookmarks_summary"))
        assertTrue(strings.contains("dialog_clear_bookmarks_message"))
        assertTrue(strings.contains("toast_bookmarks_cleared"))
    }

    @Test
    fun browserDataManagementPageCanClearDownloadRecords() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementPage.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val catalog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterDataManagementActionCatalog.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("downloadRecordRepository: DownloadRecordRepository"))
        assertTrue(page.contains("fun showDownloadData"))
        assertTrue(page.contains("downloadRecordRepository.records().size"))
        assertTrue(page.contains("DownloadRecordCleaner(downloadRecordRepository)"))
        assertTrue(page.contains("downloadManager.remove(*downloadIds)"))
        assertTrue(page.contains("R.string.download_record_count"))
        assertTrue(settings.contains("showDownloadManager: () -> Unit"))
        assertTrue(settings.contains("showDownloadManager()"))
        assertTrue(settings.contains("R.string.action_manage_download_records_summary"))
        assertTrue(pages.contains("showDownloadManager = { browserDataManagementPage.showDownloadData() }"))
        assertTrue(pages.contains("showDownloadList = { downloadsPage.show() }"))
        assertTrue(catalog.contains("FunctionCenterDataManagementAction.DOWNLOADS"))
        assertTrue(strings.contains("title_download_data_management"))
        assertTrue(strings.contains("action_manage_download_records_summary"))
        assertTrue(strings.contains("download_record_count"))
    }

    @Test
    fun browserDataManagementPageCanClearBrowsingHistory() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementPage.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("savedPageRepository: SavedPageRepository"))
        assertTrue(page.contains("fun showBrowsingHistoryData"))
        assertTrue(page.contains("savedPageRepository.history().size"))
        assertTrue(page.contains("savedPageRepository.clearHistory()"))
        assertTrue(page.contains("R.string.history_record_count"))
        assertTrue(page.contains("R.string.dialog_clear_history_message"))
        assertTrue(settings.contains("showHistoryManager: () -> Unit"))
        assertTrue(settings.contains("showHistoryManager()"))
        assertTrue(settings.contains("R.string.action_manage_history_summary"))
        assertTrue(pages.contains("showHistoryManager = { browserDataManagementPage.showBrowsingHistoryData() }"))
        assertTrue(strings.contains("title_history_data_management"))
        assertTrue(strings.contains("action_manage_history_summary"))
        assertTrue(strings.contains("action_clear_history_summary"))
        assertTrue(strings.contains("dialog_clear_history_message"))
        assertTrue(strings.contains("toast_history_cleared"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
