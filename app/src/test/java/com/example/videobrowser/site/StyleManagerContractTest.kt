package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StyleManagerContractTest {
    @Test
    fun `css hide rule injection is owned by style manager module`() {
        val styleManagerScript = projectFile("src/main/assets/scripts/style_manager.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()

        assertTrue(styleManagerScript.contains("window.VideoBrowserStyleManager = styleManager"))
        assertTrue(styleManagerScript.contains("styleManager.styleId = styleManager.styleId || '__videobrowser_css_filter__'"))
        assertTrue(styleManagerScript.contains("styleManager.injectHideRules = styleManager.injectHideRules || function (selectors)"))
        assertTrue(styleManagerScript.contains("styleManager.remove = styleManager.remove || function ()"))
        assertTrue(styleManagerScript.contains("document.createElement('style')"))
        assertTrue(styleManagerScript.contains("display:none!important;visibility:hidden!important;opacity:0!important;pointer-events:none!important;"))
        assertTrue(commonScript.contains("const styleManager = window.VideoBrowserStyleManager"))
        assertTrue(commonScript.contains("styleManager.injectHideRules(selectors);"))
        assertTrue(commonScript.contains("styleManager.remove();"))
        assertFalse(commonScript.contains("__videobrowser_css_filter__"))
        assertFalse(commonScript.contains("document.createElement('style')"))
        assertFalse(commonScript.contains("display:none!important;visibility:hidden!important;opacity:0!important;pointer-events:none!important;"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
