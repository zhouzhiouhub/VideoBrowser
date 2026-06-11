package com.example.videobrowser.video

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PlaybackHistoryPageWiringContractTest {
    @Test
    fun functionCenterPagesExposeNativePlaybackHistoryPage() {
        val source = File(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()

        assertTrue(source.contains("PlaybackHistoryPage("))
        assertTrue(source.contains("playbackHistoryPage.show()"))
        assertTrue(source.contains("openPlaybackHistoryItem"))
        assertTrue(source.contains("FunctionCenterRootAction.PLAYBACK_HISTORY"))
        assertTrue(source.contains("FunctionCenterProfileAction.PLAYBACK_HISTORY"))
    }

    @Test
    fun playbackHistoryPageListsClearsAndOpensNativeRecords() {
        val source = File(
            "src/main/java/com/example/videobrowser/functioncenter/PlaybackHistoryPage.kt"
        ).readText()

        assertTrue(source.contains("playbackHistoryRepository.records()"))
        assertTrue(source.contains("playbackHistoryRepository.clear()"))
        assertTrue(source.contains("openPlaybackHistoryItem(record)"))
        assertTrue(source.contains("R.string.title_playback_history"))
        assertTrue(source.contains("R.string.dialog_playback_history_empty"))
    }

    @Test
    fun mainActivityPassesPlaybackHistoryRepositoryAndNativeOpenCallback() {
        val source = File("src/main/java/com/example/videobrowser/MainActivity.kt").readText()

        assertTrue(source.contains("playbackHistoryRepository = PlaybackHistoryRepository(preferenceStore)"))
        assertTrue(source.contains("playbackHistoryRepository = playbackHistoryRepository"))
        assertTrue(source.contains("openPlaybackHistoryItem = ::openPlaybackHistoryItem"))
        assertTrue(source.contains("private fun openPlaybackHistoryItem(progress: PlaybackProgress)"))
        assertTrue(source.contains("openNativePlayer("))
        assertTrue(source.contains("url = progress.mediaIdentity"))
    }

    @Test
    fun playbackHistoryStringsExist() {
        val strings = File("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"title_playback_history\""))
        assertTrue(strings.contains("name=\"action_show_playback_history_summary\""))
        assertTrue(strings.contains("name=\"action_clear_playback_history_summary\""))
        assertTrue(strings.contains("name=\"dialog_playback_history_empty\""))
        assertTrue(strings.contains("name=\"dialog_clear_playback_history_message\""))
        assertTrue(strings.contains("name=\"toast_playback_history_cleared\""))
    }
}
