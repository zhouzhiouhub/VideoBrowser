package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallbackToolsContractTest {
    @Test
    fun `callback invocation is owned by shared callback module`() {
        val callbackScript = projectFile("src/main/assets/scripts/callback_tools.js").readText()
        val lifecycleScript = projectFile("src/main/assets/scripts/page_lifecycle_tools.js").readText()
        val fullscreenScript = projectFile("src/main/assets/scripts/video_fullscreen_tools.js").readText()
        val playbackScript = projectFile("src/main/assets/scripts/video_playback_tools.js").readText()
        val enhancementScript = projectFile("src/main/assets/scripts/video_enhancement_tools.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(callbackScript.contains("window.VideoBrowserCallbackTools = tools"))
        assertTrue(callbackScript.contains("tools.call = tools.call || function (callbacks, name, value)"))
        assertTrue(callbackScript.contains("return callbacks[name](value);"))
        assertTrue(scriptLoader.contains("CALLBACK_TOOLS_SCRIPT_ASSET"))
        assertTrue(commonAssetList.indexOf("CALLBACK_TOOLS_SCRIPT_ASSET") >= 0)
        assertTrue(
            commonAssetList.indexOf("CALLBACK_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("VIDEO_FULLSCREEN_TOOLS_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("CALLBACK_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("VIDEO_ENHANCEMENT_TOOLS_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("CALLBACK_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("VIDEO_PLAYBACK_TOOLS_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("CALLBACK_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("PAGE_LIFECYCLE_TOOLS_SCRIPT_ASSET")
        )

        listOf(lifecycleScript, fullscreenScript, playbackScript, enhancementScript).forEach { script ->
            assertTrue(script.contains("const callbackTools = window.VideoBrowserCallbackTools;"))
            assertFalse(script.contains("function call(callbacks, name"))
        }
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
