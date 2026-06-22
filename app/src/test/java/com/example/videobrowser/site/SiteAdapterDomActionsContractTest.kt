package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteAdapterDomActionsContractTest {
    @Test
    fun `element hiding actions are shared between common script and site helpers`() {
        val domActionsScript = projectFile("src/main/assets/scripts/dom_actions.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val configuredCleanupScript = projectFile("src/main/assets/scripts/configured_cleanup.js").readText()
        val topPageCleanupScript = projectFile("src/main/assets/scripts/top_page_cleanup.js").readText()
        val helperScript = projectFile("src/main/assets/scripts/site_adapter_helpers.js").readText()

        assertTrue(domActionsScript.contains("window.VideoBrowserDomActions = actions"))
        assertTrue(domActionsScript.contains("actions.hideElement = actions.hideElement || function (element, options)"))
        assertTrue(domActionsScript.contains("actions.removeElement = actions.removeElement || function (element, options)"))
        assertTrue(configuredCleanupScript.contains("const domActions = window.VideoBrowserDomActions || {}"))
        assertTrue(configuredCleanupScript.contains("domActions.removeElement(element, {"))
        assertFalse(commonScript.contains("element.style.setProperty('display', 'none', 'important')"))
        assertTrue(topPageCleanupScript.contains("const domActions = window.VideoBrowserDomActions || {}"))
        assertTrue(topPageCleanupScript.contains("domActions.hideElement(element, {"))
        assertFalse(topPageCleanupScript.contains("element.style.setProperty('display', 'none', 'important')"))
        assertTrue(helperScript.contains("var domActions = window.VideoBrowserDomActions || {}"))
        assertTrue(helperScript.contains("domActions.hideElement(element, {"))
        assertFalse(helperScript.contains("element.style.setProperty('display', 'none', 'important')"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
