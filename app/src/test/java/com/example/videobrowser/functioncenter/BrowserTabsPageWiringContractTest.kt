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
        assertTrue(tabsPage.contains("action_close_tab"))
        assertTrue(tabsPage.contains("switchTab(tab.id)"))
        assertTrue(tabsPage.contains("closeTab(tab.id)"))
        assertTrue(strings.contains("title_tabs"))
        assertTrue(strings.contains("action_show_tabs"))
        assertTrue(strings.contains("action_new_tab"))
        assertTrue(strings.contains("action_close_tab"))
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
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
