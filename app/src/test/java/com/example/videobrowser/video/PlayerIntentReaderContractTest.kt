package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerIntentReaderContractTest {
    @Test
    fun playerActivityDelegatesIntentReadingToReader() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val reader = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerIntentReader.kt"
        ).readText()
        val playerInitializer = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerInitializer.kt"
        ).readText()
        val factory = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerIntentFactory.kt"
        ).readText()
        val extras = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerIntentExtras.kt"
        ).readText()

        assertTrue(playerActivity.contains("PlayerIntentFactory.create("))
        assertTrue(playerActivity.contains("private val intentReader: PlayerIntentReader"))
        assertTrue(playerActivity.contains("intentReader.playbackQueue()"))
        assertTrue(playerActivity.contains("requestHeaders = intentReader::requestHeaders"))
        assertTrue(playerInitializer.contains("setDefaultRequestProperties(requestHeaders())"))
        assertTrue(playerActivity.contains("intentReader.isPrivateBrowsing()"))
        assertFalse(playerActivity.contains("private fun requestHeaders()"))
        assertFalse(playerActivity.contains("private fun currentPlayableMediaItem()"))
        assertFalse(playerActivity.contains("private fun subtitleCandidatesFromIntent()"))
        assertFalse(playerActivity.contains("private fun playbackQueueFromIntent()"))
        assertTrue(factory.contains("putExtra(PlayerIntentExtras.MEDIA_URI, mediaUri)"))
        assertTrue(factory.contains("putExtra(PlayerIntentExtras.PRIVATE_BROWSING, privateBrowsing)"))
        assertTrue(factory.contains("putExtra(PlayerIntentExtras.PLAYBACK_QUEUE"))
        assertTrue(reader.contains("internal class PlayerIntentReader"))
        assertTrue(reader.contains("PlaybackQueueJsonCodec::decode"))
        assertTrue(extras.contains("internal object PlayerIntentExtras"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
