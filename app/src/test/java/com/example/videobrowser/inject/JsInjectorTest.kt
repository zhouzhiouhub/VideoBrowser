package com.example.videobrowser.inject

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsInjectorTest {
    @Test
    fun inject_evaluatesCommonScriptWithFeatureConfig() {
        val evaluatedScripts = mutableListOf<String>()
        val injector = JsInjector(
            scriptLoader = scriptLoaderFor(COMMON_SCRIPT),
            evaluateJavascript = { script -> evaluatedScripts += script }
        )

        injector.inject(
            PageFeatureConfig(
                cleanupEnabled = true,
                videoEnabled = false
            )
        )

        val script = evaluatedScripts.single()
        assertTrue(script.contains("var config = {\"cleanupEnabled\":true,\"videoEnabled\":false};"))
        assertTrue(script.contains("if (!window.__VIDEOBROWSER_COMMON_SCRIPT_INSTALLED__) {"))
        assertTrue(script.contains(COMMON_SCRIPT))
        assertTrue(script.contains("window.__VIDEOBROWSER_COMMON_SCRIPT_INSTALLED__ = true;"))
        assertTrue(script.contains("window.VideoBrowserEnhancer.apply(config);"))
    }

    @Test
    fun inject_reusesLoadedCommonScriptAcrossRepeatedCalls() {
        var loadCount = 0
        val evaluatedScripts = mutableListOf<String>()
        val injector = JsInjector(
            scriptLoader = ScriptLoader {
                loadCount += 1
                ByteArrayInputStream(COMMON_SCRIPT.toByteArray(Charsets.UTF_8))
            },
            evaluateJavascript = { script -> evaluatedScripts += script }
        )

        injector.inject(PageFeatureConfig(cleanupEnabled = true, videoEnabled = true))
        injector.inject(PageFeatureConfig(cleanupEnabled = false, videoEnabled = true))

        assertEquals(1, loadCount)
        assertEquals(2, evaluatedScripts.size)
        assertTrue(evaluatedScripts[1].contains("var config = {\"cleanupEnabled\":false,\"videoEnabled\":true};"))
    }

    private fun scriptLoaderFor(script: String): ScriptLoader {
        return ScriptLoader {
            ByteArrayInputStream(script.toByteArray(Charsets.UTF_8))
        }
    }

    private companion object {
        private const val COMMON_SCRIPT =
            "window.VideoBrowserEnhancer={apply:function(config){window.__applied=config;}};"
    }
}
