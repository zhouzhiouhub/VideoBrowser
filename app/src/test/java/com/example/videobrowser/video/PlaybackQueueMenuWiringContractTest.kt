package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback Queue Menu Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class PlaybackQueueMenuWiringContractTest {
    /**
     * 测试函数 `playerActivityWiresPlaybackQueueMenuActions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Wires Playback Queue Menu Actions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivityWiresPlaybackQueueMenuActions() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val formatter = File(
            "src/main/java/com/example/videobrowser/video/PlaybackQueueLabelFormatter.kt"
        ).readText()

        assertTrue(source.contains("PlaybackCommand.ShowQueue"))
        assertTrue(source.contains("private fun showPlaybackQueueMenu()"))
        assertTrue(source.contains("private fun removeMediaFromQueue(index: Int)"))
        assertTrue(source.contains("PlaybackCommand.SelectQueueItem(index)"))
        assertTrue(source.contains("PlaybackCommand.ToggleShuffle"))
        assertTrue(source.contains("playbackQueue = playbackQueue.select(index)"))
        assertTrue(source.contains("playbackQueue = playbackQueue.removeAt(index)"))
        assertTrue(source.contains("playbackQueue.hasMultipleItems"))
        assertTrue(source.contains("playbackQueue.canRemoveAt(index)"))
        assertTrue(source.contains("showPlaybackQueueMenu()"))
        assertTrue(source.contains("PlaybackQueueLabelFormatter.labels("))
        assertTrue(formatter.contains("object PlaybackQueueLabelFormatter"))
        assertTrue(formatter.contains("fun labels(queue: PlaybackQueue, nowPlayingLabel: String)"))
        assertFalse(source.contains("playbackQueue.items.size <= 1"))
        assertFalse(source.contains("playbackQueue.items.size > 1"))
        assertFalse(source.contains("item.uri.substringAfterLast('/')"))
    }

    /**
     * 测试函数 `overlayExposesPlaybackQueueButton`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `overlay Exposes Playback Queue Button` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun overlayExposesPlaybackQueueButton() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()

        assertTrue(source.contains("var onPlaybackQueueRequested: (() -> Unit)? = null"))
        assertTrue(source.contains("private val queueButton = controlTextView()"))
        assertTrue(source.contains("R.string.video_control_queue"))
        assertTrue(source.contains("onPlaybackQueueRequested?.invoke()"))
        assertTrue(source.contains("queueButton.visibility = visibility"))
    }

    /**
     * 测试函数 `queueStringsExist`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `queue Strings Exist` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun queueStringsExist() {
        val strings = File("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"video_control_queue\""))
        assertTrue(strings.contains("name=\"video_queue_title\""))
        assertTrue(strings.contains("name=\"video_queue_remove\""))
        assertTrue(strings.contains("name=\"video_queue_now_playing\""))
    }
}
