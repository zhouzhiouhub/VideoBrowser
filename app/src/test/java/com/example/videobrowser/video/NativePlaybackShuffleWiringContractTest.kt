package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Native Playback Shuffle Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NativePlaybackShuffleWiringContractTest {
    @Test
    fun playerActivityWiresShuffleThroughPlaybackQueueMenu() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("private fun toggleShuffleMode(): Boolean"))
        assertTrue(source.contains("playbackQueue.shuffle()"))
        assertTrue(source.contains("playbackQueue.restoreOriginalOrder()"))
        assertTrue(source.contains("private fun syncPlayerQueueToPlaybackQueue()"))
        assertTrue(source.contains("exoPlayer.setMediaItems(playbackQueue.items.map(::toMediaItem), currentMediaItemIndex, playbackPosition)"))
        assertTrue(source.contains(".setPositiveButton(shuffleActionLabel())"))
        assertTrue(source.contains("outState.putString(STATE_PLAYBACK_QUEUE, PlaybackQueueJson.encode(playbackQueue))"))
        assertTrue(source.contains("savedInstanceState.getString(STATE_PLAYBACK_QUEUE)"))
    }

    @Test
    fun playbackQueueJsonPersistsOriginalOrderForShuffleRestore() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("\"originalItems\""))
        assertTrue(source.contains("originalItems = originalItems"))
    }

    @Test
    fun shuffleStringsExist() {
        val strings = File("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"video_queue_shuffle\""))
        assertTrue(strings.contains("name=\"video_queue_restore_order\""))
    }
}
