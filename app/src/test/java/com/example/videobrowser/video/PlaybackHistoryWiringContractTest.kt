package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback History Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackHistoryWiringContractTest {
    /**
     * 测试函数 `playerActivityRestoresAndSavesPlaybackHistory`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Restores And Saves Playback History` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivityRestoresAndSavesPlaybackHistory() {
        val source = projectFile("src/main/java/com/example/videobrowser/video/PlayerActivity.kt")
            .readText()

        assertTrue(source.contains("PlaybackHistoryRepository"))
        assertTrue(source.contains("restorePlaybackHistory()"))
        assertTrue(source.contains("resumePositionFor(playbackHistoryIdentity())"))
        assertTrue(source.contains("playbackHistoryRepository.save("))
        assertTrue(source.contains("EXTRA_PRIVATE_BROWSING"))
    }

    /**
     * 测试函数 `nativePlayerIntentCarriesPrivateBrowsingFlag`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `native Player Intent Carries Private Browsing Flag` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun nativePlayerIntentCarriesPrivateBrowsingFlag() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val externalNavigator = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserExternalNavigator.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()

        assertTrue(playerActivity.contains("privateBrowsing: Boolean = false"))
        assertTrue(playerActivity.contains("putExtra(EXTRA_PRIVATE_BROWSING, privateBrowsing)"))
        assertTrue(externalNavigator.contains("privateBrowsing: Boolean = false"))
        assertTrue(externalNavigator.contains("privateBrowsing = privateBrowsing"))
        assertTrue(mainActivity.contains("privateBrowsing = isPrivateBrowsingEnabled()"))
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
