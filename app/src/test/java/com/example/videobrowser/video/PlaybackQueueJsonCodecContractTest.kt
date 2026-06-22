package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

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
        val intentFactory = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerIntentFactory.kt"
        ).readText()
        val intentReader = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerIntentReader.kt"
        ).readText()
        val savedState = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerSavedState.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlayerSavedState.restore("))
        assertTrue(playerActivity.contains("NativePlayerSavedState.save("))
        assertTrue(intentFactory.contains("PlaybackQueueJsonCodec.encode(queue)"))
        assertTrue(intentReader.contains("PlaybackQueueJsonCodec::decode"))
        assertTrue(savedState.contains("PlaybackQueueJsonCodec.encode(playbackQueue)"))
        assertTrue(savedState.contains("PlaybackQueueJsonCodec::decode"))
        assertFalse(playerActivity.contains("private object PlaybackQueueJson"))
        assertFalse(playerActivity.contains("private fun decodePlaybackQueue"))
        assertFalse(playerActivity.contains("private fun playableItemsFromJson"))
        assertFalse(playerActivity.contains("private fun subtitleArrayFromJson"))
    }

}
