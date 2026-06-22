package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Local Playback Queue Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalPlaybackQueueWiringContractTest {
    /**
     * 测试函数 `localDirectoryOpenBuildsAndPassesSiblingPlaybackQueue`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `local Directory Open Builds And Passes Sibling Playback Queue` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun localDirectoryOpenBuildsAndPassesSiblingPlaybackQueue() {
        val localFiles = projectFile(
            "src/main/java/com/example/videobrowser/localfiles/LocalFilesController.kt"
        ).readText()
        val localOpenRequestBuilder = projectFile(
            "src/main/java/com/example/videobrowser/localfiles/LocalDocumentOpenRequestBuilder.kt"
        ).readText()
        val pageActions = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText()
        val navigator = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserExternalNavigator.kt"
        ).readText()

        assertTrue(localOpenRequestBuilder.contains("LocalPlaybackQueueBuilder.fromDocuments"))
        assertTrue(localFiles.contains("LocalDocumentOpenRequestBuilder.from("))
        assertTrue(localFiles.contains("playbackQueue"))
        assertTrue(pageActions.contains("playbackQueue: PlaybackQueue? = null"))
        assertTrue(navigator.contains("playbackQueue: PlaybackQueue? = null"))
        assertTrue(navigator.contains("playbackQueue = playbackQueue"))
    }

    /**
     * 测试函数 `playerActivityRestoresQueueFromIntentAndExposesQueueControls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Restores Queue From Intent And Exposes Queue Controls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivityRestoresQueueFromIntentAndExposesQueueControls() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val intentReader = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerIntentReader.kt"
        ).readText()
        val intentExtras = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerIntentExtras.kt"
        ).readText()
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val repeatModeConverter = projectFile(
            "src/main/java/com/example/videobrowser/video/PlaybackRepeatModeMedia3Converter.kt"
        ).readText()
        val repeatModeController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerRepeatModeController.kt"
        ).readText()
        val savedState = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerSavedState.kt"
        ).readText()
        val queueController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerQueueController.kt"
        ).readText()

        assertTrue(playerActivity.contains("intentReader.playbackQueue()"))
        assertTrue(intentReader.contains("PlaybackQueueJsonCodec::decode"))
        assertTrue(intentExtras.contains("PLAYBACK_QUEUE"))
        assertTrue(playerActivity.contains("PlaybackCommand.Previous"))
        assertTrue(playerActivity.contains("PlaybackCommand.Next"))
        assertTrue(playerActivity.contains("PlaybackCommand.ToggleRepeat"))
        assertTrue(playerActivity.contains("NativePlayerQueueController("))
        assertTrue(queueController.contains("fun playPreviousMedia()"))
        assertTrue(queueController.contains("fun playNextMedia()"))
        assertTrue(queueController.contains("playbackQueue.previous().currentIndex"))
        assertTrue(queueController.contains("playbackQueue.next().currentIndex"))
        assertTrue(playerActivity.contains("NativePlayerRepeatModeController("))
        assertTrue(repeatModeController.contains("PlaybackRepeatModeMedia3Converter.toPlayerRepeatMode"))
        assertTrue(repeatModeController.contains("fun cycle(queue: PlaybackQueue): PlaybackQueue"))
        assertTrue(repeatModeController.contains("return queue.copy(repeatMode = repeatMode)"))
        assertTrue(repeatModeConverter.contains("Player.REPEAT_MODE_ONE"))
        assertTrue(playerActivity.contains("NativePlayerSavedState.restore("))
        assertTrue(savedState.contains("STATE_REPEAT_MODE"))
        assertTrue(savedState.contains("outState.putString(STATE_REPEAT_MODE, sessionState.repeatMode.name)"))
        assertTrue(overlay.contains("var onPreviousMediaRequested: (() -> Unit)? = null"))
        assertTrue(overlay.contains("var onNextMediaRequested: (() -> Unit)? = null"))
        assertTrue(overlay.contains("var onRepeatModeRequested: (() -> PlaybackRepeatMode)? = null"))
        assertTrue(overlay.contains("setQueueControlsVisible"))
    }

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
