package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Native Playback Shuffle Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class NativePlaybackShuffleWiringContractTest {
    /**
     * 测试函数 `playerActivityWiresShuffleThroughPlaybackQueueMenu`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Wires Shuffle Through Playback Queue Menu` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivityWiresShuffleThroughPlaybackQueueMenu() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val queueDialog = File(
            "src/main/java/com/example/videobrowser/video/NativePlaybackQueueDialogController.kt"
        ).readText()
        val queue = File(
            "src/main/java/com/example/videobrowser/video/PlaybackQueue.kt"
        ).readText()
        val savedState = File(
            "src/main/java/com/example/videobrowser/video/NativePlayerSavedState.kt"
        ).readText()

        assertTrue(source.contains("private fun toggleShuffleMode(): Boolean"))
        assertTrue(source.contains("playbackQueue.toggleShuffle()"))
        assertFalse(source.contains("playbackQueue.shuffle()"))
        assertFalse(source.contains("playbackQueue.restoreOriginalOrder()"))
        assertTrue(queue.contains("fun toggleShuffle("))
        assertTrue(queue.contains("restoreOriginalOrder()"))
        assertTrue(queue.contains("shuffle("))
        assertTrue(source.contains("private fun syncPlayerQueueToPlaybackQueue()"))
        assertTrue(source.contains("playbackQueue.items.map(PlayableMediaItemMedia3Converter::toMediaItem)"))
        assertTrue(queueDialog.contains(".setPositiveButton(shuffleActionLabel(queue))"))
        assertTrue(source.contains("NativePlayerSavedState.save("))
        assertTrue(savedState.contains("outState.putString(STATE_PLAYBACK_QUEUE, PlaybackQueueJsonCodec.encode(playbackQueue))"))
        assertTrue(savedState.contains("savedInstanceState.getString(STATE_PLAYBACK_QUEUE)"))
    }

    /**
     * 测试函数 `playbackQueueJsonPersistsOriginalOrderForShuffleRestore`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `playback Queue Json Persists Original Order For Shuffle Restore` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playbackQueueJsonPersistsOriginalOrderForShuffleRestore() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/PlaybackQueueJsonCodec.kt"
        ).readText()

        assertTrue(source.contains("\"originalItems\""))
        assertTrue(source.contains("originalItems = originalItems"))
    }

    /**
     * 测试函数 `shuffleStringsExist`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `shuffle Strings Exist` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun shuffleStringsExist() {
        val strings = File("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"video_queue_shuffle\""))
        assertTrue(strings.contains("name=\"video_queue_restore_order\""))
    }
}
