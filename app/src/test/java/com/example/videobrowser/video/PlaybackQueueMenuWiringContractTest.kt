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
        val playerActivity = File(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val queueController = File(
            "src/main/java/com/example/videobrowser/video/NativePlayerQueueController.kt"
        ).readText()
        val dialogController = File(
            "src/main/java/com/example/videobrowser/video/NativePlaybackQueueDialogController.kt"
        ).readText()
        val commandDispatcher = File(
            "src/main/java/com/example/videobrowser/video/NativePlaybackCommandDispatcher.kt"
        ).readText()
        val formatter = File(
            "src/main/java/com/example/videobrowser/video/PlaybackQueueLabelFormatter.kt"
        ).readText()

        assertTrue(commandDispatcher.contains("PlaybackCommand.ShowQueue"))
        assertTrue(commandDispatcher.contains("playbackQueueDialogController.showMenu()"))
        assertTrue(dialogController.contains("fun showMenu()"))
        assertTrue(dialogController.contains("private fun showRemoveMenu()"))
        assertTrue(playerActivity.contains("onRemoveMedia = nativePlayerQueueController::removeMediaFromQueue"))
        assertTrue(playerActivity.contains("handlePlaybackCommand(PlaybackCommand.SelectQueueItem(index))"))
        assertTrue(playerActivity.contains("handlePlaybackCommand(PlaybackCommand.ToggleShuffle)"))
        assertTrue(commandDispatcher.contains("queueController.playMediaAt(command.index)"))
        assertFalse(playerActivity.contains("private fun removeMediaFromQueue(index: Int)"))
        assertFalse(playerActivity.contains("playbackQueue = playbackQueue.select(index)"))
        assertFalse(playerActivity.contains("playbackQueue = playbackQueue.removeAt(index)"))
        assertTrue(queueController.contains("fun removeMediaFromQueue(index: Int)"))
        assertTrue(queueController.contains("val selectedQueue = playbackQueue.select(index)"))
        assertTrue(queueController.contains("val updatedQueue = playbackQueue.removeAt(index)"))
        assertTrue(dialogController.contains("queue.hasMultipleItems"))
        assertTrue(queueController.contains("playbackQueue.canRemoveAt(index)"))
        assertTrue(dialogController.contains("showMenu()"))
        assertTrue(dialogController.contains("PlaybackQueueLabelFormatter.labels("))
        assertTrue(formatter.contains("object PlaybackQueueLabelFormatter"))
        assertTrue(formatter.contains("fun labels(queue: PlaybackQueue, nowPlayingLabel: String)"))
        assertFalse(playerActivity.contains("playbackQueue.items.size <= 1"))
        assertFalse(playerActivity.contains("playbackQueue.items.size > 1"))
        assertFalse(playerActivity.contains("item.uri.substringAfterLast('/')"))
    }

    /**
     * 测试函数 `overlayExposesPlaybackQueueButton`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `overlay Exposes Playback Queue Button` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun overlayExposesPlaybackQueueButton() {
        val overlay = File(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val controlsController = File(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoControlsGroupController.kt"
        ).readText()

        assertTrue(overlay.contains("var onPlaybackQueueRequested: (() -> Unit)? = null"))
        assertTrue(overlay.contains("private val queueButton = controlTextView()"))
        assertTrue(overlay.contains("requestPlaybackQueue = { onPlaybackQueueRequested?.invoke() }"))
        assertTrue(overlay.contains("controlsGroupController.setQueueControlsVisible(visible)"))
        assertFalse(overlay.contains("R.string.video_control_queue"))
        assertFalse(overlay.contains("queueButton.visibility = visibility"))
        assertTrue(controlsController.contains("private fun setupQueueButton()"))
        assertTrue(controlsController.contains("R.string.video_control_queue"))
        assertTrue(controlsController.contains("requestPlaybackQueue()"))
        assertTrue(controlsController.contains("queueButton.visibility = visibility"))
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
