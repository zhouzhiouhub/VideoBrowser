package com.example.videobrowser.settings

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTextNormalizerContractTest {
    @Test
    fun `shortcut and element rule stores share text normalization`() {
        val normalizer = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsTextNormalizer.kt"
        ).readText()
        val shortcutStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/CustomShortcutStore.kt"
        ).readText()
        val elementRuleStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/UserElementHideRuleStore.kt"
        ).readText()

        assertTrue(normalizer.contains("Regex(\"\\\\s+\")"))
        assertTrue(shortcutStore.contains("SettingsTextNormalizer.collapseWhitespace(name)"))
        assertTrue(elementRuleStore.contains("SettingsTextNormalizer.collapseWhitespace(selector)"))
        assertFalse(shortcutStore.contains("Regex(\"\\\\s+\")"))
        assertFalse(elementRuleStore.contains("Regex(\"\\\\s+\")"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
