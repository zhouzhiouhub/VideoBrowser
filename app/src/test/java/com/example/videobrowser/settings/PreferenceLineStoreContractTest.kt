package com.example.videobrowser.settings

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferenceLineStoreContractTest {
    @Test
    fun newlinePreferenceStorageIsSharedBySettingsStores() {
        val lineStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/PreferenceLineStore.kt"
        ).readText()
        val hostSetStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsHostSetStore.kt"
        ).readText()
        val customShortcutStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/CustomShortcutStore.kt"
        ).readText()
        val customSearchEngineStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/CustomSearchEngineStore.kt"
        ).readText()
        val removedSearchProviderStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/RemovedSearchProviderStore.kt"
        ).readText()
        val userElementHideRuleStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/UserElementHideRuleStore.kt"
        ).readText()
        val downloadRecordRepository = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadRecordRepository.kt"
        ).readText()
        val playbackHistoryRepository = projectFile(
            "src/main/java/com/example/videobrowser/video/PlaybackHistoryRepository.kt"
        ).readText()

        assertTrue(lineStore.contains("fun loadLines(): Sequence<String>"))
        assertTrue(lineStore.contains("fun saveLines(lines: Collection<String>)"))
        assertEquals(1, Regex("preferenceStore\\.getString\\(").findAll(lineStore).count())
        assertEquals(1, Regex("preferenceStore\\.putString\\(").findAll(lineStore).count())
        assertEquals(1, Regex("joinToString\\(separator = \"\\\\n\"\\)").findAll(lineStore).count())

        listOf(
            hostSetStore,
            customShortcutStore,
            customSearchEngineStore,
            removedSearchProviderStore,
            userElementHideRuleStore,
            downloadRecordRepository,
            playbackHistoryRepository
        ).forEach { source ->
            assertTrue(source.contains("PreferenceLineStore("))
            assertFalse(source.contains("preferenceStore.getString("))
            assertFalse(source.contains("preferenceStore.putString("))
            assertFalse(source.contains("joinToString(separator = \"\\n\")"))
        }
    }
}
