package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback History Page Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PlaybackHistoryPageWiringContractTest {
    /**
     * 测试函数 `functionCenterPagesExposeNativePlaybackHistoryPage`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `function Center Pages Expose Native Playback History Page` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun functionCenterPagesExposeNativePlaybackHistoryPage() {
        val source = File(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()

        assertTrue(source.contains("PlaybackHistoryPage("))
        assertTrue(source.contains("playbackHistoryPage.show()"))
        assertTrue(source.contains("openPlaybackHistoryItem"))
        assertTrue(source.contains("FunctionCenterRootAction.PLAYBACK_HISTORY"))
        assertTrue(source.contains("FunctionCenterProfileAction.PLAYBACK_HISTORY"))
    }

    /**
     * 测试函数 `playbackHistoryPageListsClearsAndOpensNativeRecords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `playback History Page Lists Clears And Opens Native Records` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playbackHistoryPageListsClearsAndOpensNativeRecords() {
        val source = File(
            "src/main/java/com/example/videobrowser/functioncenter/PlaybackHistoryPage.kt"
        ).readText()

        assertTrue(source.contains("playbackHistoryRepository.records()"))
        assertTrue(source.contains("playbackHistoryRepository.clear()"))
        assertTrue(source.contains("openPlaybackHistoryItem(record)"))
        assertTrue(source.contains("R.string.title_playback_history"))
        assertTrue(source.contains("R.string.dialog_playback_history_empty"))
    }

    /**
     * 测试函数 `mainActivityPassesPlaybackHistoryRepositoryAndNativeOpenCallback`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Passes Playback History Repository And Native Open Callback` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityPassesPlaybackHistoryRepositoryAndNativeOpenCallback() {
        val mainActivity = File("src/main/java/com/example/videobrowser/MainActivity.kt").readText()
        val startupFeatureAssembly = File(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val persistenceAssembly = File(
            "src/main/java/com/example/videobrowser/storage/BrowserPersistenceAssemblyController.kt"
        ).readText()
        val functionCenterAssembly = File(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterAssemblyController.kt"
        ).readText()
        val pageToolEntryController = File(
            "src/main/java/com/example/videobrowser/browser/BrowserPageToolEntryController.kt"
        ).readText()

        assertTrue(persistenceAssembly.contains("playbackHistoryRepository = PlaybackHistoryRepository(preferenceStore)"))
        assertTrue(mainActivity.contains("BrowserStartupFeatureAssemblyController"))
        assertTrue(startupFeatureAssembly.contains("browserPersistence.playbackHistoryRepository"))
        assertTrue(functionCenterAssembly.contains("playbackHistoryRepository = playbackHistoryRepository"))
        assertTrue(functionCenterAssembly.contains("openPlaybackHistoryItem = browserPageToolEntryController::openPlaybackHistoryItem"))
        assertTrue(pageToolEntryController.contains("fun openPlaybackHistoryItem(progress: PlaybackProgress)"))
        assertTrue(pageToolEntryController.contains("openNativePlayer("))
        assertTrue(pageToolEntryController.contains("progress.mediaIdentity"))
    }

    /**
     * 测试函数 `playbackHistoryStringsExist`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `playback History Strings Exist` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playbackHistoryStringsExist() {
        val strings = File("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"title_playback_history\""))
        assertTrue(strings.contains("name=\"action_show_playback_history_summary\""))
        assertTrue(strings.contains("name=\"action_clear_playback_history_summary\""))
        assertTrue(strings.contains("name=\"dialog_playback_history_empty\""))
        assertTrue(strings.contains("name=\"dialog_clear_playback_history_message\""))
        assertTrue(strings.contains("name=\"toast_playback_history_cleared\""))
    }
}
