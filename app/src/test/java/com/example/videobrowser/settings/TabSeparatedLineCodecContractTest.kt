package com.example.videobrowser.settings

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TabSeparatedLineCodecContractTest {
    @Test
    fun `settings stores share tab separated line parsing`() {
        val shortcutStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/CustomShortcutStore.kt"
        ).readText()
        val hideRuleStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/UserElementHideRuleStore.kt"
        ).readText()
        val codec = projectFile(
            "src/main/java/com/example/videobrowser/settings/TabSeparatedLineCodec.kt"
        ).readText()

        assertTrue(codec.contains("object TabSeparatedLineCodec"))
        assertTrue(shortcutStore.contains("TabSeparatedLineCodec.splitPair(line)"))
        assertTrue(shortcutStore.contains("TabSeparatedLineCodec.joinPair(shortcut.name, shortcut.url)"))
        assertTrue(hideRuleStore.contains("TabSeparatedLineCodec.splitPair(line)"))
        assertTrue(hideRuleStore.contains("TabSeparatedLineCodec.joinPair(rule.host, rule.selector)"))
        assertFalse(shortcutStore.contains("line.indexOf('\\t')"))
        assertFalse(hideRuleStore.contains("line.indexOf('\\t')"))
        assertFalse(shortcutStore.contains("\"${'$'}{shortcut.name}\\t${'$'}{shortcut.url}\""))
        assertFalse(hideRuleStore.contains("\"${'$'}{rule.host}\\t${'$'}{rule.selector}\""))
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
