package com.example.videobrowser.video

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
