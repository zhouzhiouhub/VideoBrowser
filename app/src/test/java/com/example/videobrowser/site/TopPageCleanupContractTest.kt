package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TopPageCleanupContractTest {
    @Test
    fun `top page cleanup is owned by shared cleanup module`() {
        val cleanupScript = projectFile("src/main/assets/scripts/top_page_cleanup.js").readText()
        val coordinatorScript = projectFile("src/main/assets/scripts/page_cleanup_coordinator.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(cleanupScript.contains("window.VideoBrowserTopPageCleanup = cleanup"))
        assertTrue(cleanupScript.contains("cleanup.removeAccountBars = cleanup.removeAccountBars || function ()"))
        assertTrue(cleanupScript.contains("cleanup.removeNoiseBlocks = cleanup.removeNoiseBlocks || function ()"))
        assertTrue(cleanupScript.contains("cleanup.isSearchProviderHomePage = cleanup.isSearchProviderHomePage || function ()"))
        assertTrue(cleanupScript.contains("return domTools.queryAll(selector);"))
        assertTrue(coordinatorScript.contains("const topPageCleanup = window.VideoBrowserTopPageCleanup || {}"))
        assertTrue(coordinatorScript.contains("topPageCleanup.removeAccountBars();"))
        assertTrue(coordinatorScript.contains("topPageCleanup.removeNoiseBlocks();"))
        assertTrue(scriptLoader.contains("TOP_PAGE_CLEANUP_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("TOP_PAGE_CLEANUP_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("function removeTopAccountBars()"))
        assertFalse(commonScript.contains("function removeTopNoiseBlocks()"))
        assertFalse(commonScript.contains("function isSearchProviderHomePage()"))
        assertFalse(commonScript.contains("const topPageCleanup = window.VideoBrowserTopPageCleanup"))
        assertFalse(commonScript.contains("topPageCleanup.removeAccountBars();"))
        assertFalse(commonScript.contains("topPageCleanup.removeNoiseBlocks();"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
