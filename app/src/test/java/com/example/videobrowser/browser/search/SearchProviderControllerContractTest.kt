package com.example.videobrowser.browser.search

/**
 * 测试阅读提示：
 * 这个测试文件验证“Search Provider Controller Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
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
        val settings = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsManager.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(settings.contains("fun removeCustomShortcut(shortcut: CustomShortcut): Boolean"))
        assertTrue(settings.contains("fun updateCustomShortcut(shortcut: CustomShortcut, name: String, url: String): Boolean"))
        assertTrue(controller.contains("setOnLongClickListener"))
        assertTrue(controller.contains("showCustomShortcutActionsDialog(shortcut)"))
        assertTrue(controller.contains("private fun showCustomShortcutActionsDialog(shortcut: CustomShortcut)"))
        assertTrue(controller.contains("R.string.action_edit"))
        assertTrue(controller.contains("showEditCustomShortcutDialog(shortcut)"))
        assertTrue(controller.contains("settingsManager.updateCustomShortcut(shortcut, name, url)"))
        assertTrue(controller.contains("settingsManager.removeCustomShortcut(shortcut)"))
        assertTrue(controller.contains("private fun showEditCustomShortcutDialog(shortcut: CustomShortcut)"))
        assertTrue(controller.contains("private fun showRemoveCustomShortcutDialog(shortcut: CustomShortcut)"))
        assertTrue(controller.contains("private fun showCustomShortcutEditorDialog("))
        assertTrue(controller.contains("R.string.title_edit_custom_shortcut"))
        assertTrue(controller.contains("R.string.title_remove_custom_shortcut"))
        assertTrue(controller.contains("R.string.dialog_remove_custom_shortcut_message"))
        assertTrue(controller.contains("R.string.toast_custom_shortcut_updated"))
        assertTrue(controller.contains("R.string.toast_custom_shortcut_removed"))
        assertTrue(controller.contains("setup()"))
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
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val quickLinkBuilder = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/HomeQuickLinkBuilder.kt"
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
        assertTrue(controller.contains("showRemoveRecentHistoryDialog(quickLink)"))
        assertTrue(controller.contains("private fun showRemoveRecentHistoryDialog(quickLink: HomeQuickLink)"))
        assertTrue(controller.contains("savedPageRepository.remove("))
        assertTrue(controller.contains("SavedPageRepository.SavedPageCollection.HISTORY"))
        assertTrue(controller.contains("R.string.title_remove_recent_site"))
        assertTrue(controller.contains("R.string.dialog_remove_recent_site_message"))
        assertTrue(controller.contains("R.string.toast_recent_site_removed"))
        assertTrue(controller.contains("R.string.action_open_recent_site"))
        assertTrue(controller.contains("R.drawable.ic_history_24"))
        assertTrue(mainActivity.contains("savedPageRepository = savedPageRepository"))
        assertTrue(quickLinkBuilder.contains("object HomeQuickLinkBuilder"))
        assertTrue(quickLinkBuilder.contains("excludedUrls: Collection<String>"))
        assertTrue(strings.contains("action_open_recent_site"))
        assertTrue(strings.contains("title_remove_recent_site"))
        assertTrue(strings.contains("dialog_remove_recent_site_message"))
        assertTrue(strings.contains("toast_recent_site_removed"))
        assertTrue(readme.contains("首页会从浏览历史生成最近访问入口"))
        assertTrue(readme.contains("最近访问入口可长按移除"))
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
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
