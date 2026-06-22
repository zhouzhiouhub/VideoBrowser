package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback Queue Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueWiringContractTest {
    /**
     * 测试函数 `playerActivityUsesQueueBackedMediaItems`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Uses Queue Backed Media Items` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivityUsesQueueBackedMediaItems() {
        val playerActivity = projectFile("src/main/java/com/example/videobrowser/video/PlayerActivity.kt")
            .readText()
        val playerInitializer = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerInitializer.kt"
        ).readText()
        val queueController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerQueueController.kt"
        ).readText()
        val intentReader = projectFile("src/main/java/com/example/videobrowser/video/PlayerIntentReader.kt")
            .readText()

        assertTrue(playerActivity.contains("private lateinit var playbackQueue: PlaybackQueue"))
        assertTrue(intentReader.contains("PlaybackQueue.single("))
        assertTrue(playerActivity.contains("NativePlayerInitializer("))
        assertTrue(playerInitializer.contains("setMediaItems("))
        assertTrue(playerInitializer.contains("PlayableMediaItemMedia3Converter::toMediaItem"))
        assertTrue(playerActivity.contains("NativePlayerQueueController("))
        assertTrue(queueController.contains("playbackQueue.previous().currentIndex"))
        assertTrue(queueController.contains("playbackQueue.next().currentIndex"))
        assertFalse(playerActivity.contains("currentMediaItemIndex - 1"))
        assertFalse(playerActivity.contains("currentMediaItemIndex + 1 < playbackQueue.items.size"))
        assertFalse(queueController.contains("currentMediaItemIndex() - 1"))
        assertFalse(queueController.contains("currentMediaItemIndex() + 1 < playbackQueue.items.size"))
    }

}
