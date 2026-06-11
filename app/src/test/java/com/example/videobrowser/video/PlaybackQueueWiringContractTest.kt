package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueWiringContractTest {
    @Test
    fun playerActivityUsesQueueBackedMediaItems() {
        val source = projectFile("src/main/java/com/example/videobrowser/video/PlayerActivity.kt")
            .readText()

        assertTrue(source.contains("private lateinit var playbackQueue: PlaybackQueue"))
        assertTrue(source.contains("PlaybackQueue.single("))
        assertTrue(source.contains("setMediaItems("))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
