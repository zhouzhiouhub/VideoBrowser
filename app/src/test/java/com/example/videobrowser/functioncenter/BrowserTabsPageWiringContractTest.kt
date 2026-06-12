package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserTabsPageWiringContractTest {
    @Test
    fun rootActionCatalogExposesTabsEntry() {
        val catalog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionCatalog.kt"
        ).readText()

        assertTrue(catalog.contains("TABS"))
        assertTrue(catalog.contains("FunctionCenterRootAction.TABS"))
    }

    @Test
    fun functionCenterPagesOwnsBrowserTabsPageAndRootAction() {
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()

        assertTrue(pages.contains("BrowserTabsPage"))
        assertTrue(pages.contains("currentTabs: () -> List<BrowserTab>"))
        assertTrue(pages.contains("switchTab: (Long) -> Unit"))
        assertTrue(pages.contains("closeTab: (Long) -> Unit"))
        assertTrue(pages.contains("closeOtherTabs: (Long) -> Unit"))
        assertTrue(pages.contains("duplicateTab: (Long) -> Unit"))
        assertTrue(pages.contains("FunctionCenterRootAction.TABS"))
        assertTrue(pages.contains("browserTabsPage.show()"))
    }

    @Test
    fun browserTabsPageListsSwitchCloseAndNewTabActions() {
        val tabsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserTabsPage.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(tabsPage.contains("action_new_tab"))
        assertTrue(tabsPage.contains("action_duplicate_tab"))
        assertTrue(tabsPage.contains("action_close_tab"))
        assertTrue(tabsPage.contains("action_close_other_tabs"))
        assertTrue(tabsPage.contains("switchTab(tab.id)"))
        assertTrue(tabsPage.contains("duplicateTab(tab.id)"))
        assertTrue(tabsPage.contains("closeTab(tab.id)"))
        assertTrue(tabsPage.contains("closeOtherTabs(tab.id)"))
        assertTrue(strings.contains("title_tabs"))
        assertTrue(strings.contains("action_show_tabs"))
        assertTrue(strings.contains("action_new_tab"))
        assertTrue(strings.contains("action_duplicate_tab"))
        assertTrue(strings.contains("action_close_tab"))
        assertTrue(strings.contains("action_close_other_tabs"))
    }

    @Test
    fun browserTabsPageCanCopyTabUrls() {
        val tabsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserTabsPage.kt"
        ).readText()

        assertTrue(tabsPage.contains("tab.url?.let { url ->"))
        assertTrue(tabsPage.contains("R.string.action_copy_link"))
        assertTrue(tabsPage.contains("private fun copyTabUrl(url: String)"))
        assertTrue(tabsPage.contains("Context.CLIPBOARD_SERVICE"))
        assertTrue(tabsPage.contains("ClipData.newPlainText(activity.getString(R.string.clipboard_page_url), url)"))
        assertTrue(tabsPage.contains("R.string.toast_link_copied"))
    }

    @Test
    fun browserTabsPageCanShareTabUrls() {
        val tabsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserTabsPage.kt"
        ).readText()

        assertTrue(tabsPage.contains("R.string.action_share_page"))
        assertTrue(tabsPage.contains("private fun shareTabUrl(url: String)"))
        assertTrue(tabsPage.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(tabsPage.contains("putExtra(Intent.EXTRA_TEXT, url)"))
        assertTrue(tabsPage.contains("Intent.createChooser(intent, activity.getString(R.string.action_share_page))"))
    }

    @Test
    fun mainActivityPassesTabActionsIntoFunctionCenter() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertTrue(mainActivity.contains("currentTabs = ::currentTabs"))
        assertTrue(mainActivity.contains("activeTabId = ::activeTabId"))
        assertTrue(mainActivity.contains("openNewTab = ::openNewTab"))
        assertTrue(mainActivity.contains("switchTab = ::switchTab"))
        assertTrue(mainActivity.contains("closeTab = ::closeTab"))
        assertTrue(mainActivity.contains("closeOtherTabs = ::closeOtherTabs"))
        assertTrue(mainActivity.contains("duplicateTab = ::duplicateTab"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
