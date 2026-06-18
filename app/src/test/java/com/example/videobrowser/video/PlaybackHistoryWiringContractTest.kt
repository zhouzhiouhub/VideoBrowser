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
        assertTrue(source.contains("source = PlaybackHistorySource.NATIVE_MEDIA"))
        assertTrue(source.contains("intentReader.isPrivateBrowsing()"))
    }

    @Test
    fun webViewPlaybackTimelineRecordsPlaybackHistory() {
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val startupController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupController.kt"
        ).readText()
        val startupFeatureAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserStartupFeatureAssemblyController.kt"
        ).readText()
        val nativeBridgeController = projectFile(
            "src/main/java/com/example/videobrowser/browser/VideoBrowserNativeBridgeController.kt"
        ).readText()
        val webRecorder = projectFile(
            "src/main/java/com/example/videobrowser/video/WebPlaybackHistoryRecorder.kt"
        ).readText()
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val playbackScript = projectFile("src/main/assets/scripts/video_playback_tools.js").readText()
        val nativeBridgeScript = projectFile("src/main/assets/scripts/native_bridge.js").readText()

        assertTrue(mainActivity.contains("BrowserActivityFeatureAssemblyController"))
        assertTrue(startupFeatureAssembly.contains("BrowserStartupControllerAssembly"))
        assertTrue(startupController.contains("nativeBridgeController.createNativeBridge()"))
        assertTrue(startupFeatureAssembly.contains("browserPersistence.webPlaybackHistoryRecorder"))
        assertTrue(nativeBridgeController.contains("updatePlaybackTimeline = ::updateWebViewPlaybackTimeline"))
        assertTrue(nativeBridgeController.contains("webPlaybackHistoryRecorder.record(positionMs, durationMs)"))
        assertTrue(webRecorder.contains("source = PlaybackHistorySource.WEB_PAGE"))
        assertTrue(webRecorder.contains("private const val SAVE_THROTTLE_MS = 5_000L"))
        assertTrue(webRecorder.contains("playbackHistoryRepository.save("))
        assertTrue(script.contains("reportPlaybackTimeline(video);"))
        assertTrue(script.contains("videoPlaybackTools.reportTimeline(target);"))
        assertTrue(playbackScript.contains("nativeBridge.updatePlaybackTimeline("))
        assertTrue(
            nativeBridgeScript.contains(
                "bridgeTools.updatePlaybackTimeline = bridgeTools.updatePlaybackTimeline || function (positionMs, durationMs)"
            )
        )
        assertTrue(nativeBridgeScript.contains("bridgeTools.callNative('updatePlaybackTimeline', [positionMs, durationMs])"))
    }

    @Test
    fun playbackHistoryOpensWebPageRecordsInWebView() {
        val pageToolEntryController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageToolEntryController.kt"
        )
            .readText()

        assertTrue(pageToolEntryController.contains("progress.source == PlaybackHistorySource.WEB_PAGE"))
        assertTrue(pageToolEntryController.contains("loadUrl(progress.mediaIdentity)"))
        assertTrue(pageToolEntryController.contains("openNativePlayer("))
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
        val nativePlayerEntryController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerEntryController.kt"
        ).readText()
        val navigationAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserNavigationAssemblyController.kt"
        )
            .readText()

        assertTrue(playerActivity.contains("privateBrowsing: Boolean = false"))
        assertTrue(playerActivity.contains("putExtra(PlayerIntentExtras.PRIVATE_BROWSING, privateBrowsing)"))
        assertTrue(externalNavigator.contains("privateBrowsing: Boolean = false"))
        assertTrue(externalNavigator.contains("privateBrowsing = privateBrowsing"))
        assertTrue(nativePlayerEntryController.contains("class NativePlayerEntryController"))
        assertTrue(nativePlayerEntryController.contains("privateBrowsing = isPrivateBrowsingEnabled()"))
        assertTrue(navigationAssembly.contains("val nativePlayerEntryController = NativePlayerEntryController("))
        assertTrue(
            navigationAssembly.contains("isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled")
        )
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
