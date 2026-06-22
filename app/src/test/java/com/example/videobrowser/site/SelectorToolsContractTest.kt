package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectorToolsContractTest {
    @Test
    fun `selector helpers are owned by shared selector module`() {
        val selectorToolsScript = projectFile("src/main/assets/scripts/selector_tools.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val configuredCleanupScript = projectFile("src/main/assets/scripts/configured_cleanup.js").readText()
        val elementPickerScript = projectFile("src/main/assets/scripts/element_picker.js").readText()

        assertTrue(selectorToolsScript.contains("window.VideoBrowserSelectorTools = selectorTools"))
        assertTrue(selectorToolsScript.contains("selectorTools.isSafeSelector = selectorTools.isSafeSelector || function (selector)"))
        assertTrue(selectorToolsScript.contains("selectorTools.safeSelectorList = selectorTools.safeSelectorList || function (value)"))
        assertTrue(selectorToolsScript.contains("selectorTools.queryAll = selectorTools.queryAll || function (selector)"))
        assertTrue(selectorToolsScript.contains("selectorTools.cssIdentifier = selectorTools.cssIdentifier || function (value)"))
        assertTrue(configuredCleanupScript.contains("const selectorTools = window.VideoBrowserSelectorTools || {}"))
        assertTrue(configuredCleanupScript.contains("return selectorTools.safeSelectorList(value);"))
        assertTrue(configuredCleanupScript.contains("selectorTools.queryAll(selector).forEach(function (element)"))
        assertTrue(elementPickerScript.contains("selectorTools.cssIdentifier(id)"))
        assertTrue(elementPickerScript.contains("selectorTools.cssIdentifier(className)"))
        assertTrue(elementPickerScript.contains("const matches = selectorTools.queryAll(selector);"))
        assertFalse(commonScript.contains("if (!selector || selector.length > 200) return false;"))
        assertFalse(commonScript.contains("document.querySelectorAll(selector)"))
        assertFalse(commonScript.contains("window.CSS.escape(String(value))"))
        assertFalse(elementPickerScript.contains("window.CSS.escape(String(value))"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
