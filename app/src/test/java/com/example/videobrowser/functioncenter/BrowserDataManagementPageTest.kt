package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Data Management Page Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserDataManagementPageTest {
    /**
     * 测试函数 `cookieParserOnlyExposesCookieNames`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `cookie Parser Only Exposes Cookie Names` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `siteDataSummaryOnlyShowsUsedStorage`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `site Data Summary Only Shows Used Storage` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun siteDataSummaryOnlyShowsUsedStorage() {
        assertEquals("1.5 KB", BrowserDataDisplayFormatter.siteDataUsageSummary(1536L))
    }

    /**
     * 测试函数 `siteDataOriginSearchMatchesOriginAndHostCaseInsensitively`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `site Data Origin Search Matches Origin And Host Case Insensitively` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun siteDataOriginSearchMatchesOriginAndHostCaseInsensitively() {
        val origins = listOf(
            "https://Video.Example.com",
            "https://news.example.org",
            "file://android_asset"
        )

        assertEquals(
            listOf("https://Video.Example.com"),
            BrowserSiteDataOriginSearch.filterOriginNames(origins, "video EXAMPLE")
        )
        assertEquals(origins, BrowserSiteDataOriginSearch.filterOriginNames(origins, " "))
        assertTrue(BrowserSiteDataOriginSearch.filterOriginNames(origins, "missing").isEmpty())
    }

    /**
     * 测试函数 `browserHistoryClearRangeCalculatesCutoffs`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser History Clear Range Calculates Cutoffs` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserHistoryClearRangeCalculatesCutoffs() {
        val now = 10L * 24L * 60L * 60L * 1000L

        assertEquals(now - 60L * 60L * 1000L, BrowserHistoryClearRange.LAST_HOUR.cutoffMillis(now))
        assertEquals(now - 24L * 60L * 60L * 1000L, BrowserHistoryClearRange.LAST_24_HOURS.cutoffMillis(now))
        assertEquals(now - 7L * 24L * 60L * 60L * 1000L, BrowserHistoryClearRange.LAST_7_DAYS.cutoffMillis(now))
        assertTrue(BrowserHistoryClearRange.ALL.cutoffMillis(now) == null)
    }

    /**
     * 测试函数 `siteDataManagementCanSearchOrigins`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `site Data Management Can Search Origins` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun siteDataManagementCanSearchOrigins() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementPage.kt"
        ).readText()
        val originSearch = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSiteDataOriginSearch.kt"
        ).readText()
        val siteDataPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSiteDataManagementPage.kt"
        ).readText()
        val dialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementDialogController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(originSearch.contains("object BrowserSiteDataOriginSearch"))
        assertTrue(page.contains("fun showSiteData(replaceCurrent: Boolean = false, query: String? = null)"))
        assertTrue(page.contains("private val siteDataPage = BrowserSiteDataManagementPage("))
        assertTrue(page.contains("siteDataPage.show(replaceCurrent, query)"))
        assertTrue(siteDataPage.contains("BrowserSiteDataOriginSearch.matches(origin.origin, query)"))
        assertTrue(dialogs.contains("fun showSiteDataSearchDialog"))
        assertTrue(siteDataPage.contains("dialogController.showRemoveSiteDataDialog(origin.origin)"))
        assertTrue(siteDataPage.contains("show(replaceCurrent = true, query = query)"))
        assertTrue(siteDataPage.contains("R.string.action_search_site_data"))
        assertTrue(siteDataPage.contains("R.string.action_clear_search"))
        assertTrue(siteDataPage.contains("R.string.dialog_site_data_search_empty"))
        assertTrue(strings.contains("action_search_site_data"))
        assertTrue(strings.contains("action_search_site_data_summary"))
        assertTrue(strings.contains("hint_site_data_search"))
        assertTrue(strings.contains("dialog_site_data_search_empty"))
        assertTrue(readme.contains("站点数据管理（支持按域名搜索）"))
    }

    /**
     * 测试函数 `browserDataManagementPageCanClearBookmarks`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Data Management Page Can Clear Bookmarks` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserDataManagementPageCanClearBookmarks() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementPage.kt"
        ).readText()
        val clearActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataClearActions.kt"
        ).readText()
        val dialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementDialogController.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val dataSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsDataManagementSection.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("fun showBookmarkData"))
        assertTrue(page.contains("savedPageRepository.bookmarks().size"))
        assertTrue(clearActions.contains("savedPageRepository.clear(SavedPageRepository.SavedPageCollection.BOOKMARKS)"))
        assertTrue(page.contains("R.string.bookmark_record_count"))
        assertTrue(page.contains("exportBookmarks: () -> Unit"))
        assertTrue(page.contains("importBookmarks: () -> Unit"))
        assertTrue(page.contains("R.string.action_export_bookmarks"))
        assertTrue(page.contains("R.string.action_import_bookmarks"))
        assertTrue(dialogs.contains("R.string.dialog_clear_bookmarks_message"))
        assertTrue(settings.contains("showBookmarkManager: () -> Unit"))
        assertTrue(settings.contains("BrowserSettingsDataManagementSection("))
        assertTrue(dataSection.contains("showBookmarkManager()"))
        assertTrue(dataSection.contains("R.string.action_manage_bookmarks_summary"))
        assertTrue(pages.contains("showBookmarkManager = { browserDataManagementPage.showBookmarkData() }"))
        assertTrue(pages.contains("exportBookmarks = exportBookmarks"))
        assertTrue(pages.contains("importBookmarks = importBookmarks"))
        assertTrue(strings.contains("title_bookmark_data_management"))
        assertTrue(strings.contains("action_manage_bookmarks_summary"))
        assertTrue(strings.contains("action_export_bookmarks"))
        assertTrue(strings.contains("action_import_bookmarks"))
        assertTrue(strings.contains("action_clear_bookmarks_summary"))
        assertTrue(strings.contains("dialog_clear_bookmarks_message"))
        assertTrue(strings.contains("toast_bookmarks_cleared"))
    }

    /**
     * 测试函数 `browserDataManagementPageCanClearDownloadRecords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Data Management Page Can Clear Download Records` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserDataManagementPageCanClearDownloadRecords() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementPage.kt"
        ).readText()
        val clearActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataClearActions.kt"
        ).readText()
        val dialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementDialogController.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val dataSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsDataManagementSection.kt"
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
        assertTrue(clearActions.contains("DownloadRecordCleaner(downloadRecordRepository)"))
        assertTrue(clearActions.contains("downloadManager.remove(*downloadIds)"))
        assertTrue(page.contains("R.string.download_record_count"))
        assertTrue(settings.contains("showDownloadManager: () -> Unit"))
        assertTrue(dataSection.contains("showDownloadManager()"))
        assertTrue(dataSection.contains("R.string.action_manage_download_records_summary"))
        assertTrue(pages.contains("showDownloadManager = { browserDataManagementPage.showDownloadData() }"))
        assertTrue(pages.contains("showDownloadList = { downloadsPage.show() }"))
        assertTrue(catalog.contains("FunctionCenterDataManagementAction.DOWNLOADS"))
        assertTrue(strings.contains("title_download_data_management"))
        assertTrue(strings.contains("action_manage_download_records_summary"))
        assertTrue(strings.contains("download_record_count"))
    }

    /**
     * 测试函数 `browserDataManagementPageCanClearBrowsingHistory`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Data Management Page Can Clear Browsing History` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserDataManagementPageCanClearBrowsingHistory() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementPage.kt"
        ).readText()
        val clearActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataClearActions.kt"
        ).readText()
        val dialogs = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataManagementDialogController.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val dataSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsDataManagementSection.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("savedPageRepository: SavedPageRepository"))
        assertTrue(page.contains("fun showBrowsingHistoryData"))
        assertTrue(page.contains("savedPageRepository.history().size"))
        assertTrue(clearActions.contains("savedPageRepository.clearHistory()"))
        assertTrue(dialogs.contains("BrowserHistoryClearRange.entries"))
        assertTrue(clearActions.contains("savedPageRepository.clearHistoryUpdatedSince(cutoffMillis)"))
        assertTrue(page.contains("R.string.action_clear_history_range_summary"))
        assertTrue(dialogs.contains("R.string.dialog_clear_history_range_message"))
        assertTrue(dialogs.contains("R.string.toast_history_range_cleared"))
        assertTrue(page.contains("R.string.history_record_count"))
        assertTrue(settings.contains("showHistoryManager: () -> Unit"))
        assertTrue(dataSection.contains("showHistoryManager()"))
        assertTrue(dataSection.contains("R.string.action_manage_history_summary"))
        assertTrue(pages.contains("showHistoryManager = { browserDataManagementPage.showBrowsingHistoryData() }"))
        assertTrue(strings.contains("title_history_data_management"))
        assertTrue(strings.contains("action_manage_history_summary"))
        assertTrue(strings.contains("action_clear_history_summary"))
        assertTrue(strings.contains("action_clear_history_range_summary"))
        assertTrue(strings.contains("history_clear_range_last_hour"))
        assertTrue(strings.contains("history_clear_range_last_24_hours"))
        assertTrue(strings.contains("history_clear_range_last_7_days"))
        assertTrue(strings.contains("history_clear_range_all"))
        assertTrue(strings.contains("dialog_clear_history_range_message"))
        assertTrue(strings.contains("toast_history_range_cleared"))
        assertTrue(strings.contains("dialog_clear_history_message"))
        assertTrue(strings.contains("toast_history_cleared"))
    }

}
