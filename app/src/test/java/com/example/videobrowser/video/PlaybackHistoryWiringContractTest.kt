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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
