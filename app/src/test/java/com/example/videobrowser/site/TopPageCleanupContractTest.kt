package com.example.videobrowser.site

import com.example.videobrowser.testutil.projectFile

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
        assertTrue(cleanupScript.contains("const geometry = window.VideoBrowserGeometry || {}"))
        assertTrue(cleanupScript.contains("const selectorTools = window.VideoBrowserSelectorTools || {}"))
        assertTrue(cleanupScript.contains("domTools.queryAll("))
        assertTrue(cleanupScript.contains("domTools.queryAllWithin(element, 'a,button,[role=\"button\"],svg,i')"))
        assertTrue(cleanupScript.contains("domTools.queryAllWithin(element, 'a,button,img,svg')"))
        assertTrue(cleanupScript.contains("const rect = geometry.safeRect(element);"))
        assertTrue(cleanupScript.contains("const text = selectorTools.compactText(element.innerText || element.textContent);"))
        assertTrue(cleanupScript.contains("domActions.hideElement(element, {"))
        assertTrue(coordinatorScript.contains("const topPageCleanup = window.VideoBrowserTopPageCleanup || {}"))
        assertTrue(coordinatorScript.contains("topPageCleanup.removeAccountBars();"))
        assertTrue(coordinatorScript.contains("topPageCleanup.removeNoiseBlocks();"))
        assertTrue(scriptLoader.contains("TOP_PAGE_CLEANUP_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("SELECTOR_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("TOP_PAGE_CLEANUP_SCRIPT_ASSET")
        )
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
        assertFalse(cleanupScript.contains("function hideElement(element, reason)"))
        assertFalse(cleanupScript.contains("function queryAll(selector)"))
        assertFalse(cleanupScript.contains("replace(/\\s+/g, '')"))
        assertFalse(cleanupScript.contains("getBoundingClientRect()"))
        assertFalse(cleanupScript.contains("querySelectorAll("))
    }

}
