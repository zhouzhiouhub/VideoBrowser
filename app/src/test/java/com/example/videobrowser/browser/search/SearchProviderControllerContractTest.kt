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
        assertTrue(controller.contains("settingsManager.removeCustomShortcut(shortcut)"))
        assertTrue(controller.contains("setOnLongClickListener"))
        assertTrue(controller.contains("showRemoveCustomShortcutDialog(shortcut)"))
        assertTrue(controller.contains("private fun showRemoveCustomShortcutDialog(shortcut: CustomShortcut)"))
        assertTrue(controller.contains("R.string.title_remove_custom_shortcut"))
        assertTrue(controller.contains("R.string.dialog_remove_custom_shortcut_message"))
        assertTrue(controller.contains("R.string.toast_custom_shortcut_removed"))
        assertTrue(controller.contains("setup()"))
        assertTrue(strings.contains("title_remove_custom_shortcut"))
        assertTrue(strings.contains("dialog_remove_custom_shortcut_message"))
        assertTrue(strings.contains("toast_custom_shortcut_removed"))
        assertTrue(readme.contains("首页快捷入口可长按移除"))
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
