package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Browser Tabs Page Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserTabsPageWiringContractTest {
    /**
     * 测试函数 `rootActionCatalogExposesTabsEntry`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `root Action Catalog Exposes Tabs Entry` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun rootActionCatalogExposesTabsEntry() {
        val catalog = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionCatalog.kt"
        ).readText()

        assertTrue(catalog.contains("TABS"))
        assertTrue(catalog.contains("FunctionCenterRootAction.TABS"))
    }

    /**
     * 测试函数 `functionCenterPagesOwnsBrowserTabsPageAndRootAction`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `function Center Pages Owns Browser Tabs Page And Root Action` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun functionCenterPagesOwnsBrowserTabsPageAndRootAction() {
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val rootActionSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionSection.kt"
        ).readText()

        assertTrue(pages.contains("BrowserTabsPage"))
        assertTrue(pages.contains("currentTabs: () -> List<BrowserTab>"))
        assertTrue(pages.contains("canReopenClosedTab: () -> Boolean"))
        assertTrue(pages.contains("reopenClosedTab: () -> Unit"))
        assertTrue(pages.contains("switchTab: (Long) -> Unit"))
        assertTrue(pages.contains("closeTab: (Long) -> Unit"))
        assertTrue(pages.contains("closeOtherTabs: (Long) -> Unit"))
        assertTrue(pages.contains("closeAllTabs: () -> Unit"))
        assertTrue(pages.contains("duplicateTab: (Long) -> Unit"))
        assertTrue(pages.contains("showBrowserTabs = { browserTabsPage.show() }"))
        assertTrue(rootActionSection.contains("FunctionCenterRootAction.TABS"))
        assertTrue(rootActionSection.contains("showBrowserTabs()"))
    }

    /**
     * 测试函数 `browserTabsPageListsSwitchCloseAndNewTabActions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Tabs Page Lists Switch Close And New Tab Actions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserTabsPageListsSwitchCloseAndNewTabActions() {
        val tabsPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserTabsPage.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(tabsPage.contains("action_new_tab"))
        assertTrue(tabsPage.contains("action_reopen_closed_tab"))
        assertTrue(tabsPage.contains("action_reopen_closed_tab_summary"))
        assertTrue(tabsPage.contains("action_duplicate_tab"))
        assertTrue(tabsPage.contains("action_close_tab"))
        assertTrue(tabsPage.contains("action_close_other_tabs"))
        assertTrue(tabsPage.contains("action_close_all_tabs"))
        assertTrue(tabsPage.contains("action_close_all_tabs_summary"))
        assertTrue(tabsPage.contains("enabled = canReopenClosedTab()"))
        assertTrue(tabsPage.contains("reopenClosedTab()"))
        assertTrue(tabsPage.contains("switchTab(tab.id)"))
        assertTrue(tabsPage.contains("duplicateTab(tab.id)"))
        assertTrue(tabsPage.contains("closeTab(tab.id)"))
        assertTrue(tabsPage.contains("closeOtherTabs(tab.id)"))
        assertTrue(tabsPage.contains("closeAllTabs()"))
        assertTrue(strings.contains("title_tabs"))
        assertTrue(strings.contains("action_show_tabs"))
        assertTrue(strings.contains("action_new_tab"))
        assertTrue(strings.contains("action_reopen_closed_tab"))
        assertTrue(strings.contains("action_reopen_closed_tab_summary"))
        assertTrue(strings.contains("action_duplicate_tab"))
        assertTrue(strings.contains("action_close_tab"))
        assertTrue(strings.contains("action_close_other_tabs"))
        assertTrue(strings.contains("action_close_all_tabs"))
    }

    /**
     * 测试函数 `browserTabsPageCanCopyTabUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Tabs Page Can Copy Tab Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `browserTabsPageCanShareTabUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Tabs Page Can Share Tab Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `mainActivityPassesTabActionsIntoFunctionCenter`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Passes Tab Actions Into Function Center` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityPassesTabActionsIntoFunctionCenter() {
        val functionCenterAssembly = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterAssemblyController.kt"
        )
            .readText()
        val tabActionsController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabActionsController.kt"
        ).readText()

        assertTrue(functionCenterAssembly.contains("currentTabs = browserTabActionsController::currentTabs"))
        assertTrue(functionCenterAssembly.contains("activeTabId = browserTabActionsController::activeTabId"))
        assertTrue(functionCenterAssembly.contains("openNewTab = browserTabActionsController::openNewTab"))
        assertTrue(functionCenterAssembly.contains("canReopenClosedTab = browserTabActionsController::canReopenClosedTab"))
        assertTrue(functionCenterAssembly.contains("reopenClosedTab = browserTabActionsController::reopenClosedTab"))
        assertTrue(functionCenterAssembly.contains("switchTab = browserTabActionsController::switchTab"))
        assertTrue(functionCenterAssembly.contains("closeTab = browserTabActionsController::closeTab"))
        assertTrue(functionCenterAssembly.contains("closeOtherTabs = browserTabActionsController::closeOtherTabs"))
        assertTrue(functionCenterAssembly.contains("closeAllTabs = browserTabActionsController::closeAllTabs"))
        assertTrue(tabActionsController.contains("standardTabWebViews.closeAllTabs()"))
        assertTrue(functionCenterAssembly.contains("duplicateTab = browserTabActionsController::duplicateTab"))
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
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
