package com.example.videobrowser.site

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkipButtonToolsContractTest {
    @Test
    fun `skip button clicking is owned by shared skip button module`() {
        val skipScript = projectFile("src/main/assets/scripts/skip_button_tools.js").readText()
        val runtimeScript = projectFile("src/main/assets/scripts/enhancer_runtime.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(skipScript.contains("window.VideoBrowserSkipButtonTools = tools"))
        assertTrue(skipScript.contains("tools.defaultSelectors = tools.defaultSelectors || ["))
        assertTrue(skipScript.contains("'button[aria-label*=\"跳过\"]'"))
        assertTrue(skipScript.contains("tools.click = tools.click || function (selectors)"))
        assertTrue(skipScript.contains("domTools.queryAll(selector).forEach(function (button)"))
        assertTrue(commonScript.contains("const skipButtonTools = window.VideoBrowserSkipButtonTools"))
        assertTrue(runtimeScript.contains("skipButtonTools.click();"))
        assertTrue(scriptLoader.contains("SKIP_BUTTON_TOOLS_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("SKIP_BUTTON_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("const skipSelectors = ["))
        assertFalse(commonScript.contains("skipButtonTools.click();"))
        assertFalse(commonScript.contains("button[aria-label*=\"跳过\"]"))
        assertFalse(commonScript.contains("selector.indexOf('skip') !== -1"))
        assertFalse(skipScript.contains("document.querySelectorAll(selector)"))
        assertFalse(skipScript.contains("function queryAll(selector)"))
    }

}
