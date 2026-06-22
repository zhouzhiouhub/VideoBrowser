package com.example.videobrowser.settings

import com.example.videobrowser.testutil.projectFile

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
        val downloadRecordRepository = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadRecordRepository.kt"
        ).readText()
        val playbackHistoryRepository = projectFile(
            "src/main/java/com/example/videobrowser/video/PlaybackHistoryRepository.kt"
        ).readText()
        val codec = projectFile(
            "src/main/java/com/example/videobrowser/settings/TabSeparatedLineCodec.kt"
        ).readText()

        assertTrue(codec.contains("object TabSeparatedLineCodec"))
        assertTrue(codec.contains("fun splitFields(line: String)"))
        assertTrue(codec.contains("fun joinFields(fields: List<String>)"))
        assertTrue(shortcutStore.contains("TabSeparatedLineCodec.splitPair(line)"))
        assertTrue(shortcutStore.contains("TabSeparatedLineCodec.joinPair(shortcut.name, shortcut.url)"))
        assertTrue(hideRuleStore.contains("TabSeparatedLineCodec.splitPair(line)"))
        assertTrue(hideRuleStore.contains("TabSeparatedLineCodec.joinPair(rule.host, rule.selector)"))
        assertTrue(downloadRecordRepository.contains("TabSeparatedLineCodec.splitFields(line)"))
        assertTrue(downloadRecordRepository.contains("TabSeparatedLineCodec.joinFields("))
        assertTrue(playbackHistoryRepository.contains("TabSeparatedLineCodec.splitFields(line)"))
        assertTrue(playbackHistoryRepository.contains("TabSeparatedLineCodec.joinFields("))
        assertFalse(shortcutStore.contains("line.indexOf('\\t')"))
        assertFalse(hideRuleStore.contains("line.indexOf('\\t')"))
        assertFalse(shortcutStore.contains("\"${'$'}{shortcut.name}\\t${'$'}{shortcut.url}\""))
        assertFalse(hideRuleStore.contains("\"${'$'}{rule.host}\\t${'$'}{rule.selector}\""))
        assertFalse(downloadRecordRepository.contains("private fun splitEscaped"))
        assertFalse(downloadRecordRepository.contains("private fun escape(value: String)"))
        assertFalse(playbackHistoryRepository.contains("private fun splitEscaped"))
        assertFalse(playbackHistoryRepository.contains("private fun escape(value: String)"))
    }

}
