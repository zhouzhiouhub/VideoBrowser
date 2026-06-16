package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback Queue Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
