package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptletHooksContractTest {
    @Test
    fun `scriptlet URL hooks are owned by shared module`() {
        val scriptletHooksScript = projectFile("src/main/assets/scripts/scriptlet_hooks.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()

        assertTrue(scriptletHooksScript.contains("window.VideoBrowserScriptletHooks = hooks"))
        assertTrue(scriptletHooksScript.contains("hooks.defaultBlockedKeywords = hooks.defaultBlockedKeywords || ["))
        assertTrue(scriptletHooksScript.contains("hooks.configKeywordList = hooks.configKeywordList || function (config, fieldName)"))
        assertTrue(scriptletHooksScript.contains("hooks.shouldBlockUrlAgainstKeywords = hooks.shouldBlockUrlAgainstKeywords || function (value, keywords)"))
        assertTrue(scriptletHooksScript.contains("window.open = function (url)"))
        assertTrue(scriptletHooksScript.contains("window.fetch = function ()"))
        assertTrue(scriptletHooksScript.contains("callbacks.installFullscreenEventHooks()"))
        assertTrue(commonScript.contains("const scriptletHooks = window.VideoBrowserScriptletHooks"))
        assertTrue(commonScript.contains("scriptletHooks.install(state, {"))
        assertFalse(commonScript.contains("const blockedKeywords = ["))
        assertFalse(commonScript.contains("const originalOpen = window.open"))
        assertFalse(commonScript.contains("const originalFetch = window.fetch"))
        assertFalse(commonScript.contains("function configKeywordList(fieldName)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
