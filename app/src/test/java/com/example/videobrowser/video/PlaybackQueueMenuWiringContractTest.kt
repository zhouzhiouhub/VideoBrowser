package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback Queue Menu Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PlaybackQueueMenuWiringContractTest {
    @Test
    fun playerActivityWiresPlaybackQueueMenuActions() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("PlaybackCommand.ShowQueue"))
        assertTrue(source.contains("private fun showPlaybackQueueMenu()"))
        assertTrue(source.contains("private fun removeMediaFromQueue(index: Int)"))
        assertTrue(source.contains("PlaybackCommand.SelectQueueItem(index)"))
        assertTrue(source.contains("PlaybackCommand.ToggleShuffle"))
        assertTrue(source.contains("playbackQueue = playbackQueue.select(index)"))
        assertTrue(source.contains("playbackQueue = playbackQueue.removeAt(index)"))
        assertTrue(source.contains("showPlaybackQueueMenu()"))
    }

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

    @Test
    fun queueStringsExist() {
        val strings = File("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"video_control_queue\""))
        assertTrue(strings.contains("name=\"video_queue_title\""))
        assertTrue(strings.contains("name=\"video_queue_remove\""))
        assertTrue(strings.contains("name=\"video_queue_now_playing\""))
    }
}
