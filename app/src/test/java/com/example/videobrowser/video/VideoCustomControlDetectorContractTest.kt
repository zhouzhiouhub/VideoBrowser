package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoCustomControlDetectorContractTest {
    @Test
    fun `custom player control detection is owned by shared detector module`() {
        val detectorScript = projectFile("src/main/assets/scripts/video_custom_control_detector.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(detectorScript.contains("window.VideoBrowserVideoCustomControlDetector = detector"))
        assertTrue(detectorScript.contains("detector.hasControls = detector.hasControls || function (video)"))
        assertTrue(detectorScript.contains("detector.rootFor = detector.rootFor || function (video)"))
        assertTrue(detectorScript.contains("'.xgplayer-controls'"))
        assertTrue(detectorScript.contains("'.dplayer-controller'"))
        assertTrue(detectorScript.contains("'.art-controls'"))
        assertTrue(detectorScript.contains("'.vjs-control-bar'"))
        assertTrue(detectorScript.contains("'[class*=\"player-control\"]'"))
        assertTrue(detectorScript.contains("geometry.rectsOverlap(rect, geometry.expandedRect(videoRect, 12))"))
        assertTrue(detectorScript.contains("return selectorTools.queryAllWithin(root, selector);"))
        assertTrue(commonScript.contains("const customControlDetector = window.VideoBrowserVideoCustomControlDetector"))
        assertTrue(commonScript.contains("customControlDetector.hasControls(video)"))
        assertTrue(scriptLoader.contains("VIDEO_CUSTOM_CONTROL_DETECTOR_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("VIDEO_CUSTOM_CONTROL_DETECTOR_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("const customPlayerControlSelectors = ["))
        assertFalse(commonScript.contains("function hasLikelyCustomPlayerControls(video)"))
        assertFalse(commonScript.contains("function customPlayerRootFor(video)"))
        assertFalse(commonScript.contains("function isLikelyCustomControlElement(element, video)"))
        assertFalse(commonScript.contains("function isLikelyMediaControlElement(element, video)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
