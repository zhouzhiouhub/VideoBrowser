package com.example.videobrowser.site

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfiguredCleanupContractTest {
    @Test
    fun `configured cleanup owns rule selector style and dom cleanup`() {
        val cleanupScript = projectFile("src/main/assets/scripts/configured_cleanup.js").readText()
        val coordinatorScript = projectFile("src/main/assets/scripts/page_cleanup_coordinator.js").readText()
        val runtimeScript = projectFile("src/main/assets/scripts/enhancer_runtime.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(cleanupScript.contains("window.VideoBrowserConfiguredCleanup = cleanup"))
        assertTrue(cleanupScript.contains("cleanup.cssSelectors = cleanup.cssSelectors || function (state)"))
        assertTrue(cleanupScript.contains("cleanup.userCssSelectors = cleanup.userCssSelectors || function (state)"))
        assertTrue(cleanupScript.contains("cleanup.domSelectors = cleanup.domSelectors || function (state)"))
        assertTrue(cleanupScript.contains("cleanup.injectStyle = cleanup.injectStyle || function (state, options)"))
        assertTrue(cleanupScript.contains("cleanup.removeDomElements = cleanup.removeDomElements || function (state)"))
        assertTrue(cleanupScript.contains("return selectorTools.safeSelectorList(value);"))
        assertTrue(cleanupScript.contains("selectorTools.queryAll(selector).forEach(function (element)"))
        assertTrue(cleanupScript.contains("domActions.removeElement(element, {"))
        assertTrue(cleanupScript.contains("styleManager.injectHideRules(selectors);"))
        assertTrue(coordinatorScript.contains("const configuredCleanup = window.VideoBrowserConfiguredCleanup || {}"))
        assertTrue(coordinatorScript.contains("configuredCleanup.injectStyle(state, {"))
        assertTrue(coordinatorScript.contains("configuredCleanup.removeDomElements(state);"))
        assertTrue(coordinatorScript.contains("configuredCleanup.hasUserCssSelectors(state)"))
        assertTrue(coordinatorScript.contains("configuredCleanup.removeStyle();"))
        assertTrue(coordinatorScript.contains("const pageLifecycleTools = window.VideoBrowserPageLifecycleTools;"))
        assertTrue(coordinatorScript.contains("pageLifecycleTools.runWithOptionalMutationSuppression(config, function ()"))
        assertTrue(commonScript.contains("const pageCleanupCoordinator = window.VideoBrowserPageCleanupCoordinator"))
        assertTrue(runtimeScript.contains("pageCleanupCoordinator.applyDisabledState(state);"))
        assertTrue(scriptLoader.contains("CONFIGURED_CLEANUP_SCRIPT_ASSET"))
        assertTrue(scriptLoader.contains("PAGE_CLEANUP_COORDINATOR_SCRIPT_ASSET"))
        assertTrue(scriptLoader.contains("PAGE_LIFECYCLE_TOOLS_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("PAGE_LIFECYCLE_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("PAGE_CLEANUP_COORDINATOR_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("CONFIGURED_CLEANUP_SCRIPT_ASSET") <
                commonAssetList.indexOf("PAGE_CLEANUP_COORDINATOR_SCRIPT_ASSET")
        )
        assertTrue(
            commonAssetList.indexOf("PAGE_CLEANUP_COORDINATOR_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("const configuredCleanup = window.VideoBrowserConfiguredCleanup"))
        assertFalse(commonScript.contains("pageCleanupCoordinator.applyDisabledState(state);"))
        assertFalse(commonScript.contains("configuredCleanup.injectStyle(state, {"))
        assertFalse(commonScript.contains("configuredCleanup.removeDomElements(state);"))
        assertFalse(commonScript.contains("configuredCleanup.hasUserCssSelectors(state)"))
        assertFalse(commonScript.contains("configuredCleanup.removeStyle();"))
        assertFalse(commonScript.contains("function externalCssSelectors()"))
        assertFalse(commonScript.contains("function userCssSelectors()"))
        assertFalse(commonScript.contains("function externalDomSelectors()"))
        assertFalse(commonScript.contains("function safeSelectorList(value)"))
        assertFalse(commonScript.contains("function removeConfiguredDomElements()"))
        assertFalse(coordinatorScript.contains("function runWithMutationSuppressed(config, work)"))
    }

}
