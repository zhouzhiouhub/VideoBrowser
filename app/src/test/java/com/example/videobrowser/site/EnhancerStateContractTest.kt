package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnhancerStateContractTest {
    @Test
    fun `enhancer runtime state is owned by shared state module`() {
        val stateScript = projectFile("src/main/assets/scripts/enhancer_state.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(stateScript.contains("window.VideoBrowserEnhancerState = manager"))
        assertTrue(stateScript.contains("manager.current = manager.current || function ()"))
        assertTrue(stateScript.contains("const state = window.__videobrowserState || {"))
        assertTrue(stateScript.contains("window.__videobrowserState = state;"))
        assertTrue(stateScript.contains("manager.normalize = manager.normalize || function (state)"))
        assertTrue(stateScript.contains("manager.ensureWeakSet = manager.ensureWeakSet || function (state, key)"))
        assertTrue(stateScript.contains("manager.ensureWeakMap = manager.ensureWeakMap || function (state, key)"))
        assertTrue(stateScript.contains("manager.ensureWeakSet(state, 'fullscreenHookedVideos');"))
        assertTrue(stateScript.contains("manager.ensureWeakSet(state, 'speedHookedVideos');"))
        assertTrue(stateScript.contains("manager.ensureWeakMap(state, 'bestQualityAttempts');"))
        assertTrue(stateScript.contains("state.fullscreenPlaybackSpeed = Number(state.fullscreenPlaybackSpeed || 1);"))
        assertTrue(commonScript.contains("const enhancerState = window.VideoBrowserEnhancerState;"))
        assertTrue(commonScript.contains("const state = enhancerState.current();"))
        assertTrue(scriptLoader.contains("ENHANCER_STATE_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("ENHANCER_STATE_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("const state = window.__videobrowserState || {"))
        assertFalse(commonScript.contains("window.__videobrowserState = state;"))
        assertFalse(commonScript.contains("state.fullscreenHookedVideos = new WeakSet();"))
        assertFalse(commonScript.contains("state.speedHookedVideos = new WeakSet();"))
        assertFalse(commonScript.contains("state.bestQualityAttempts = new WeakMap();"))
        assertFalse(commonScript.contains("state.fullscreenPlaybackSpeed = Number(state.fullscreenPlaybackSpeed || 1);"))
        assertFalse(stateScript.contains("function weakSetOrNew(value)"))
        assertFalse(stateScript.contains("function weakMapOrNew(value)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
