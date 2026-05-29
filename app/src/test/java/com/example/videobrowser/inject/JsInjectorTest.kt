package com.example.videobrowser.inject

import com.example.videobrowser.site.SiteAdapterRegistry
import com.example.videobrowser.rules.ElementRule
import com.example.videobrowser.rules.ElementRuleType
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleEngine
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertTrue(
            script.contains(
                "var config = {\"cleanupEnabled\":true,\"videoEnabled\":false," +
                    "\"cssSelectors\":[],\"domSelectors\":[],\"blockedUrlKeywords\":[]};"
            )
        )
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
        assertTrue(
            evaluatedScripts[1].contains(
                "var config = {\"cleanupEnabled\":false,\"videoEnabled\":true," +
                    "\"cssSelectors\":[],\"domSelectors\":[],\"blockedUrlKeywords\":[]};"
            )
        )
    }

    @Test
    fun inject_skipsCommonScriptWhenJsInjectionDisabled() {
        var loadCount = 0
        val evaluatedScripts = mutableListOf<String>()
        val injector = JsInjector(
            scriptLoader = ScriptLoader {
                loadCount += 1
                ByteArrayInputStream(COMMON_SCRIPT.toByteArray(Charsets.UTF_8))
            },
            evaluateJavascript = { script -> evaluatedScripts += script }
        )

        injector.inject(
            PageFeatureConfig(
                jsInjectionEnabled = false,
                cleanupEnabled = true,
                videoEnabled = true
            )
        )

        assertEquals(0, loadCount)
        assertTrue(evaluatedScripts.isEmpty())
    }

    @Test
    fun inject_loadsOnlyMatchingSiteScriptForPageUrl() {
        val requestedPaths = mutableListOf<String>()
        val evaluatedScripts = mutableListOf<String>()
        val injector = JsInjector(
            scriptLoader = ScriptLoader { path ->
                requestedPaths += path
                ByteArrayInputStream(scriptContentFor(path).toByteArray(Charsets.UTF_8))
            },
            evaluateJavascript = { script -> evaluatedScripts += script },
            siteAdapterRegistry = SiteAdapterRegistry.default()
        )

        injector.inject(
            PageFeatureConfig(cleanupEnabled = true, videoEnabled = true),
            pageUrl = "https://m.youtube.com/watch?v=1"
        )

        val script = evaluatedScripts.single()
        assertEquals(
            listOf(ScriptLoader.COMMON_SCRIPT_ASSET, "scripts/youtube.js"),
            requestedPaths
        )
        assertTrue(script.contains("window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__"))
        assertTrue(script.contains("window.__siteYoutubeLoaded = true;"))
        assertTrue(script.contains("window.VideoBrowserSiteAdapters[\"youtube\"].apply(config);"))
        assertFalse(script.contains("__siteBilibiliLoaded"))
    }

    @Test
    fun inject_skipsSiteScriptsForUnmatchedPageUrl() {
        val requestedPaths = mutableListOf<String>()
        val evaluatedScripts = mutableListOf<String>()
        val injector = JsInjector(
            scriptLoader = ScriptLoader { path ->
                requestedPaths += path
                ByteArrayInputStream(scriptContentFor(path).toByteArray(Charsets.UTF_8))
            },
            evaluateJavascript = { script -> evaluatedScripts += script },
            siteAdapterRegistry = SiteAdapterRegistry.default()
        )

        injector.inject(
            PageFeatureConfig(cleanupEnabled = true, videoEnabled = true),
            pageUrl = "https://example.com/"
        )

        val script = evaluatedScripts.single()
        assertEquals(listOf(ScriptLoader.COMMON_SCRIPT_ASSET), requestedPaths)
        assertFalse(script.contains("window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__"))
        assertFalse(script.contains("__siteYoutubeLoaded"))
    }

    @Test
    fun inject_wrapsSiteScriptsWithRepeatGuard() {
        val evaluatedScripts = mutableListOf<String>()
        val injector = JsInjector(
            scriptLoader = scriptLoaderForSiteScripts(),
            evaluateJavascript = { script -> evaluatedScripts += script },
            siteAdapterRegistry = SiteAdapterRegistry.default()
        )

        injector.inject(
            PageFeatureConfig(cleanupEnabled = true, videoEnabled = true),
            pageUrl = "https://www.bilibili.com/video/BV1"
        )

        val script = evaluatedScripts.single()
        assertTrue(script.contains("if (!window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__[\"scripts/bilibili.js\"]) {"))
        assertTrue(script.contains("window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__[\"scripts/bilibili.js\"] = true;"))
        assertTrue(script.contains("window.VideoBrowserSiteAdapters[\"bilibili\"].apply(config);"))
    }

    @Test
    fun inject_addsElementRuleSelectorsToFeatureConfig() {
        val evaluatedScripts = mutableListOf<String>()
        val injector = JsInjector(
            scriptLoader = scriptLoaderFor(COMMON_SCRIPT),
            evaluateJavascript = { script -> evaluatedScripts += script },
            ruleEngine = RuleEngine(
                rules = listOf(Rule.blockUrlContains("/pagead/")),
                elementRules = listOf(
                    ElementRule(
                        id = "css:1",
                        selector = ".ad-banner",
                        type = ElementRuleType.CSS_HIDE
                    ),
                    ElementRule(
                        id = "dom:1",
                        selector = ".popup-ad",
                        type = ElementRuleType.DOM_REMOVE
                    )
                )
            )
        )

        injector.inject(
            PageFeatureConfig(cleanupEnabled = true, videoEnabled = true),
            pageUrl = "https://example.com/"
        )

        val script = evaluatedScripts.single()
        assertTrue(script.contains("\"cssSelectors\":[\".ad-banner\"]"))
        assertTrue(script.contains("\"domSelectors\":[\".popup-ad\"]"))
        assertTrue(script.contains("\"blockedUrlKeywords\":[\"/pagead/\"]"))
    }

    private fun scriptLoaderFor(script: String): ScriptLoader {
        return ScriptLoader {
            ByteArrayInputStream(script.toByteArray(Charsets.UTF_8))
        }
    }

    private fun scriptLoaderForSiteScripts(): ScriptLoader {
        return ScriptLoader { path ->
            ByteArrayInputStream(scriptContentFor(path).toByteArray(Charsets.UTF_8))
        }
    }

    private fun scriptContentFor(path: String): String {
        return when (path) {
            ScriptLoader.COMMON_SCRIPT_ASSET -> COMMON_SCRIPT
            "scripts/youtube.js" -> "window.__siteYoutubeLoaded = true;"
            "scripts/bilibili.js" -> "window.__siteBilibiliLoaded = true;"
            else -> error("Unexpected script path: $path")
        }
    }

    private companion object {
        private const val COMMON_SCRIPT =
            "window.VideoBrowserEnhancer={apply:function(config){window.__applied=config;}};"
    }
}
