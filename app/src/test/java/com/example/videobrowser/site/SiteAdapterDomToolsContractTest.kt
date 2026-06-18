package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteAdapterDomToolsContractTest {
    @Test
    fun `safe dom queries are shared between common script and site helpers`() {
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val helperScript = projectFile("src/main/assets/scripts/site_adapter_helpers.js").readText()

        assertTrue(commonScript.contains("window.VideoBrowserDomTools = domTools"))
        assertTrue(commonScript.contains("domTools.queryAll = domTools.queryAll || function (selector)"))
        assertTrue(commonScript.contains("domTools.queryAllWithin = domTools.queryAllWithin || function (root, selector)"))
        assertTrue(commonScript.contains("return domTools.queryAll(selector);"))
        assertTrue(commonScript.contains("return domTools.queryAllWithin(root, selector);"))
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
