package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteAdapterDomToolsContractTest {
    @Test
    fun `safe dom queries are shared between common script and site helpers`() {
        val domToolsScript = projectFile("src/main/assets/scripts/dom_tools.js").readText()
        val selectorToolsScript = projectFile("src/main/assets/scripts/selector_tools.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val helperScript = projectFile("src/main/assets/scripts/site_adapter_helpers.js").readText()

        assertTrue(domToolsScript.contains("window.VideoBrowserDomTools = domTools"))
        assertTrue(domToolsScript.contains("domTools.queryAll = domTools.queryAll || function (selector)"))
        assertTrue(domToolsScript.contains("domTools.queryAllWithin = domTools.queryAllWithin || function (root, selector)"))
        assertTrue(commonScript.contains("const domTools = window.VideoBrowserDomTools"))
        assertFalse(commonScript.contains("domTools.queryAll = domTools.queryAll || function (selector)"))
        assertFalse(commonScript.contains("domTools.queryAllWithin = domTools.queryAllWithin || function (root, selector)"))
        assertTrue(selectorToolsScript.contains("const domTools = window.VideoBrowserDomTools || {}"))
        assertTrue(selectorToolsScript.contains("return domTools.queryAll(selector);"))
        assertTrue(selectorToolsScript.contains("return domTools.queryAllWithin(root, selector);"))
        assertTrue(commonScript.contains("return selectorTools.queryAll(selector);"))
        assertTrue(commonScript.contains("return selectorTools.queryAllWithin(root, selector);"))
        assertTrue(helperScript.contains("var domTools = window.VideoBrowserDomTools || {}"))
        assertTrue(helperScript.contains("return domTools.queryAll ? domTools.queryAll(selector) : [];"))
        assertFalse(helperScript.contains("document.querySelectorAll(selector)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
