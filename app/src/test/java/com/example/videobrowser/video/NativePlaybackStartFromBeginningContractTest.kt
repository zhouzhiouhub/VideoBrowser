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
    /**
     * 测试函数 `playerActivitySkipsHistoryResumeWhenSettingIsEnabled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Skips History Resume When Setting Is Enabled` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivitySkipsHistoryResumeWhenSettingIsEnabled() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("settingsManager.alwaysStartVideosFromBeginning()"))
        assertTrue(source.contains("if (settingsManager.alwaysStartVideosFromBeginning())"))
        assertTrue(source.contains("playbackPosition = 0L"))
    }

    /**
     * 测试函数 `browserSettingsPageExposesStartFromBeginningSwitch`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Settings Page Exposes Start From Beginning Switch` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `settingStringsExist`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `setting Strings Exist` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun settingStringsExist() {
        val strings = File("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"setting_always_start_videos_from_beginning\""))
        assertTrue(strings.contains("name=\"setting_always_start_videos_from_beginning_summary\""))
    }
}
