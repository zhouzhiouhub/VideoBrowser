package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageLifecycleToolsContractTest {
    @Test
    fun `page lifecycle scheduling is owned by shared lifecycle module`() {
        val lifecycleScript = projectFile("src/main/assets/scripts/page_lifecycle_tools.js").readText()
        val runtimeScript = projectFile("src/main/assets/scripts/enhancer_runtime.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(lifecycleScript.contains("window.VideoBrowserPageLifecycleTools = tools"))
        assertTrue(lifecycleScript.contains("tools.runWithMutationSuppressed = tools.runWithMutationSuppressed || function (state, work)"))
        assertTrue(lifecycleScript.contains("tools.runWithOptionalMutationSuppression = tools.runWithOptionalMutationSuppression || function (options, work)"))
        assertTrue(lifecycleScript.contains("tools.installFullscreenEventHooks = tools.installFullscreenEventHooks || function (state, options)"))
        assertTrue(lifecycleScript.contains("tools.schedulePageWork = tools.schedulePageWork || function (state, options)"))
        assertTrue(lifecycleScript.contains("tools.disposePageFeatures = tools.disposePageFeatures || function (state, options, callbacks)"))
        assertTrue(lifecycleScript.contains("tools.startWorkers = tools.startWorkers || function (state, options)"))
        assertTrue(lifecycleScript.contains("const callbackTools = window.VideoBrowserCallbackTools;"))
        assertTrue(lifecycleScript.contains("callbackTools.call(callbacks, 'schedulePageWork');"))
        assertTrue(lifecycleScript.contains("targetState.observer = new MutationObserver(function ()"))
        assertTrue(lifecycleScript.contains("targetState.intervalId = window.setInterval(function ()"))
        assertTrue(lifecycleScript.contains("document.addEventListener('fullscreenchange', callbacks.syncDocumentFullscreenState);"))
        assertTrue(runtimeScript.contains("window.VideoBrowserEnhancerRuntime = runtime"))
        assertTrue(runtimeScript.contains("runtime.create = runtime.create || function (options)"))
        assertTrue(runtimeScript.contains("return pageLifecycleTools.runWithMutationSuppressed(state, work);"))
        assertTrue(runtimeScript.contains("return pageLifecycleTools.installFullscreenEventHooks(state, {"))
        assertTrue(runtimeScript.contains("return pageLifecycleTools.schedulePageWork(state, {"))
        assertTrue(runtimeScript.contains("return pageLifecycleTools.disposePageFeatures(state, options, {"))
        assertTrue(runtimeScript.contains("return pageLifecycleTools.startWorkers(state, {"))
        assertTrue(commonScript.contains("const pageLifecycleTools = window.VideoBrowserPageLifecycleTools"))
        assertTrue(commonScript.contains("const enhancerRuntime = window.VideoBrowserEnhancerRuntime"))
        assertTrue(commonScript.contains("const pageRuntime = enhancerRuntime.create({"))
        assertTrue(commonScript.contains("installFullscreenEventHooks: pageRuntime.installFullscreenEventHooks"))
        assertTrue(scriptLoader.contains("ENHANCER_RUNTIME_SCRIPT_ASSET"))
        assertTrue(scriptLoader.contains("PAGE_LIFECYCLE_TOOLS_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("PAGE_LIFECYCLE_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("VIDEO_CONTROL_TOOLS_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("PAGE_LIFECYCLE_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("PAGE_CLEANUP_COORDINATOR_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("PAGE_LIFECYCLE_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("ENHANCER_RUNTIME_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("PAGE_CLEANUP_COORDINATOR_SCRIPT_ASSET") <
                commonAssetList.indexOf("ENHANCER_RUNTIME_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("ENHANCER_RUNTIME_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("new MutationObserver(function ()"))
        assertFalse(commonScript.contains("window.setTimeout(runPageWork, delay)"))
        assertFalse(commonScript.contains("window.addEventListener('pagehide'"))
        assertFalse(commonScript.contains("state.observer.disconnect();"))
        assertFalse(commonScript.contains("return pageLifecycleTools.runWithMutationSuppressed(state, work);"))
        assertFalse(commonScript.contains("return pageLifecycleTools.installFullscreenEventHooks(state, {"))
        assertFalse(commonScript.contains("return pageLifecycleTools.schedulePageWork(state, {"))
        assertFalse(commonScript.contains("return pageLifecycleTools.disposePageFeatures(state, options, {"))
        assertFalse(commonScript.contains("return pageLifecycleTools.startWorkers(state, {"))
        assertFalse(runtimeScript.contains("new MutationObserver(function ()"))
        assertFalse(runtimeScript.contains("window.setTimeout(runPageWork, delay)"))
        assertFalse(runtimeScript.contains("window.addEventListener('pagehide'"))
        assertFalse(runtimeScript.contains("state.observer.disconnect();"))
        assertFalse(lifecycleScript.contains("function call(callbacks, name, value)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
