package com.example.videobrowser.browser.search

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Search Provider Controller Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchProviderControllerContractTest {
    /**
     * 测试函数 `homeCustomShortcutsCanBeRemovedFromStartPage`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `home Custom Shortcuts Can Be Removed From Start Page` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun homeCustomShortcutsCanBeRemovedFromStartPage() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderDialogController.kt"
        ).readText()
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsManager.kt"
        ).readText()
        val itemFactory = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderItemFactory.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(settings.contains("fun removeCustomShortcut(shortcut: CustomShortcut): Boolean"))
        assertTrue(settings.contains("fun updateCustomShortcut(shortcut: CustomShortcut, name: String, url: String): Boolean"))
        assertTrue(itemFactory.contains("setOnLongClickListener"))
        assertTrue(controller.contains("onCustomShortcutLongClick = dialogController::showCustomShortcutActionsDialog"))
        assertFalse(controller.contains("private fun showCustomShortcutActionsDialog(shortcut: CustomShortcut)"))
        assertTrue(dialogController.contains("R.string.action_edit"))
        assertTrue(dialogController.contains("showEditCustomShortcutDialog(shortcut)"))
        assertTrue(dialogController.contains("settingsManager.updateCustomShortcut(shortcut, name, url)"))
        assertTrue(dialogController.contains("settingsManager.removeCustomShortcut(shortcut)"))
        assertFalse(controller.contains("private fun showEditCustomShortcutDialog(shortcut: CustomShortcut)"))
        assertFalse(controller.contains("private fun showRemoveCustomShortcutDialog(shortcut: CustomShortcut)"))
        assertTrue(dialogController.contains("private fun showCustomShortcutEditorDialog("))
        assertTrue(dialogController.contains("R.string.title_edit_custom_shortcut"))
        assertTrue(dialogController.contains("R.string.title_remove_custom_shortcut"))
        assertTrue(dialogController.contains("R.string.dialog_remove_custom_shortcut_message"))
        assertTrue(dialogController.contains("R.string.toast_custom_shortcut_updated"))
        assertTrue(dialogController.contains("R.string.toast_custom_shortcut_removed"))
        assertTrue(controller.contains("onDataChanged = ::setup"))
        assertTrue(strings.contains("action_edit"))
        assertTrue(strings.contains("title_edit_custom_shortcut"))
        assertTrue(strings.contains("title_remove_custom_shortcut"))
        assertTrue(strings.contains("dialog_remove_custom_shortcut_message"))
        assertTrue(strings.contains("toast_custom_shortcut_updated"))
        assertTrue(strings.contains("toast_custom_shortcut_removed"))
        assertTrue(readme.contains("首页快捷入口可长按编辑或移除"))
    }

    /**
     * 测试函数 `startPageShowsRecentHistoryQuickLinksOutsidePrivateMode`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `start Page Shows Recent History Quick Links Outside Private Mode` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun startPageShowsRecentHistoryQuickLinksOutsidePrivateMode() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderDialogController.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val searchAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/BrowserSearchAssemblyController.kt"
        ).readText()
        val quickLinkBuilder = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/HomeQuickLinkBuilder.kt"
        ).readText()
        val itemFactory = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderItemFactory.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(controller.contains("savedPageRepository: SavedPageRepository"))
        assertTrue(controller.contains("if (!isPrivateBrowsingEnabled())"))
        assertTrue(controller.contains("HomeQuickLinkBuilder.fromHistory("))
        assertTrue(controller.contains("history = savedPageRepository.history()"))
        assertTrue(controller.contains("excludedUrls = homeQuickLinkExcludedUrls(customShortcuts)"))
        assertTrue(controller.contains("private fun addRecentHistoryItem(quickLink: HomeQuickLink)"))
        assertTrue(controller.contains("private fun createRecentHistoryItem(quickLink: HomeQuickLink)"))
        assertTrue(controller.contains("onRecentHistoryLongClick = dialogController::showRemoveRecentHistoryDialog"))
        assertFalse(controller.contains("private fun showRemoveRecentHistoryDialog(quickLink: HomeQuickLink)"))
        assertTrue(dialogController.contains("savedPageRepository.remove("))
        assertTrue(dialogController.contains("SavedPageRepository.SavedPageCollection.HISTORY"))
        assertTrue(dialogController.contains("R.string.title_remove_recent_site"))
        assertTrue(dialogController.contains("R.string.dialog_remove_recent_site_message"))
        assertTrue(dialogController.contains("R.string.toast_recent_site_removed"))
        assertTrue(itemFactory.contains("R.string.action_open_recent_site"))
        assertTrue(itemFactory.contains("R.drawable.ic_history_24"))
        assertTrue(searchAssembly.contains("savedPageRepository = savedPageRepository"))
        assertTrue(quickLinkBuilder.contains("object HomeQuickLinkBuilder"))
        assertTrue(quickLinkBuilder.contains("excludedUrls: Collection<String>"))
        assertTrue(strings.contains("action_open_recent_site"))
        assertTrue(strings.contains("title_remove_recent_site"))
        assertTrue(strings.contains("dialog_remove_recent_site_message"))
        assertTrue(strings.contains("toast_recent_site_removed"))
        assertTrue(readme.contains("首页会从浏览历史生成最近访问入口"))
        assertTrue(readme.contains("最近访问入口可长按移除"))
    }

    @Test
    fun providerListItemAssemblyIsShared() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()

        assertTrue(controller.contains("private fun addProviderListItem(item: LinearLayout, badge: View, label: View)"))
        assertTrue(controller.contains("addProviderListItem(item, badge, label)"))
        assertTrue(controller.contains("badge = itemFactory.createCustomShortcutBadge(shortcut)"))
        assertTrue(controller.contains("badge = itemFactory.createRecentHistoryBadge()"))
        assertTrue(controller.contains("badge = itemFactory.createAddShortcutBadge()"))
        assertEquals(
            1,
            Regex("LinearLayout\\.LayoutParams\\(dp\\(48\\), dp\\(48\\)\\)").findAll(controller).count()
        )
        assertEquals(1, Regex("providerList\\.addView\\(item, itemFactory\\.providerItemLayoutParams\\(\\)\\)").findAll(controller).count())
    }

}
