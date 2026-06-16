package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Native Playback Start From Beginning Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NativePlaybackStartFromBeginningContractTest {
    @Test
    fun playerActivitySkipsHistoryResumeWhenSettingIsEnabled() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("settingsManager.alwaysStartVideosFromBeginning()"))
        assertTrue(source.contains("if (settingsManager.alwaysStartVideosFromBeginning())"))
        assertTrue(source.contains("playbackPosition = 0L"))
    }

    @Test
    fun browserSettingsPageExposesStartFromBeginningSwitch() {
        val source = File(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()

        assertTrue(source.contains("R.string.setting_always_start_videos_from_beginning"))
        assertTrue(source.contains("R.string.setting_always_start_videos_from_beginning_summary"))
        assertTrue(source.contains("settingsManager.alwaysStartVideosFromBeginning()"))
        assertTrue(source.contains("settingsManager.setAlwaysStartVideosFromBeginning(enabled)"))
    }

    @Test
    fun settingStringsExist() {
        val strings = File("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"setting_always_start_videos_from_beginning\""))
        assertTrue(strings.contains("name=\"setting_always_start_videos_from_beginning_summary\""))
    }
}
