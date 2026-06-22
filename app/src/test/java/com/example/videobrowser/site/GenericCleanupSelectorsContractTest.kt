package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenericCleanupSelectorsContractTest {
    @Test
    fun `generic cleanup selectors are owned by shared selector module`() {
        val cleanupScript = projectFile("src/main/assets/scripts/generic_cleanup_selectors.js").readText()
        val configuredCleanupScript = projectFile("src/main/assets/scripts/configured_cleanup.js").readText()
        val coordinatorScript = projectFile("src/main/assets/scripts/page_cleanup_coordinator.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(cleanupScript.contains("window.VideoBrowserGenericCleanupSelectors = cleanup"))
        assertTrue(cleanupScript.contains("cleanup.adSelectors = cleanup.adSelectors || ["))
        assertTrue(cleanupScript.contains("'[href*=\"passport.baidu.com\"]'"))
        assertTrue(cleanupScript.contains("'[class*=\"app-download\"]'"))
        assertTrue(cleanupScript.contains("cleanup.defaultSelectors = cleanup.defaultSelectors || function ()"))
        assertTrue(cleanupScript.contains("cleanup.hideDefaultElements = cleanup.hideDefaultElements || function ()"))
        assertTrue(cleanupScript.contains("domTools.queryAll(selector).forEach(function (element)"))
        assertTrue(coordinatorScript.contains("const genericCleanupSelectors = window.VideoBrowserGenericCleanupSelectors || {}"))
        assertTrue(configuredCleanupScript.contains("genericCleanupSelectors.defaultSelectors()"))
        assertTrue(coordinatorScript.contains("genericCleanupSelectors.hideDefaultElements();"))
        assertTrue(scriptLoader.contains("GENERIC_CLEANUP_SELECTORS_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("GENERIC_CLEANUP_SELECTORS_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("const adSelectors = ["))
        assertFalse(commonScript.contains("const accountSelectors = ["))
        assertFalse(commonScript.contains("const cleanupSelectors = ["))
        assertFalse(commonScript.contains("adSelectors.concat(accountSelectors, cleanupSelectors)"))
        assertFalse(commonScript.contains("const genericCleanupSelectors = window.VideoBrowserGenericCleanupSelectors"))
        assertFalse(commonScript.contains("genericCleanupSelectors.hideDefaultElements();"))
        assertFalse(cleanupScript.contains("document.querySelectorAll(selector)"))
        assertFalse(cleanupScript.contains("function queryAll(selector)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
