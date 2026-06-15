package com.example.videobrowser.browser.search

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchProviderControllerContractTest {
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
        assertTrue(controller.contains("R.string.action_open_recent_site"))
        assertTrue(controller.contains("R.drawable.ic_history_24"))
        assertTrue(mainActivity.contains("savedPageRepository = savedPageRepository"))
        assertTrue(quickLinkBuilder.contains("object HomeQuickLinkBuilder"))
        assertTrue(quickLinkBuilder.contains("excludedUrls: Collection<String>"))
        assertTrue(strings.contains("action_open_recent_site"))
        assertTrue(readme.contains("首页会从浏览历史生成最近访问入口"))
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
