package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueJsonCodecContractTest {
    @Test
    fun codecOwnsPlaybackQueueJsonSerialization() {
        val codec = projectFile(
            "src/main/java/com/example/videobrowser/video/PlaybackQueueJsonCodec.kt"
        ).readText()

        assertTrue(codec.contains("object PlaybackQueueJsonCodec"))
        assertTrue(codec.contains("fun encode(queue: PlaybackQueue): String"))
        assertTrue(codec.contains("fun decode(encodedQueue: String): PlaybackQueue?"))
        assertTrue(codec.contains("\"originalItems\""))
        assertTrue(codec.contains("subtitleArrayFromJson"))
        assertTrue(codec.contains("sourceFromName"))
    }

    @Test
    fun playerActivityDelegatesPlaybackQueueJsonToCodec() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(playerActivity.contains("PlaybackQueueJsonCodec.encode(playbackQueue)"))
        assertTrue(playerActivity.contains("PlaybackQueueJsonCodec.encode(it)"))
        assertTrue(playerActivity.contains("PlaybackQueueJsonCodec::decode"))
        assertFalse(playerActivity.contains("private object PlaybackQueueJson"))
        assertFalse(playerActivity.contains("private fun decodePlaybackQueue"))
        assertFalse(playerActivity.contains("private fun playableItemsFromJson"))
        assertFalse(playerActivity.contains("private fun subtitleArrayFromJson"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
