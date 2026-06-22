package com.example.videobrowser.inject

/**
 * 测试阅读提示：
 * 这个测试文件验证“Js Injector Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.site.SiteAdapterRegistry
import com.example.videobrowser.rules.ElementRule
import com.example.videobrowser.rules.ElementRuleType
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.rules.ScriptletRule
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JsInjectorTest {
    /**
     * 测试函数 `inject_evaluatesCommonScriptWithFeatureConfig`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject evaluates Common Script With Feature Config` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
                    "\"cssSelectors\":[],\"userCssSelectors\":[],\"domSelectors\":[]," +
                    "\"blockedUrlKeywords\":[],\"scriptletWindowOpenBlockedKeywords\":[]," +
                    "\"scriptletFetchBlockedKeywords\":[],\"scriptletSkipButtonsEnabled\":false," +
                    "\"scriptletVideoControlsEnabled\":false};"
            )
        )
        assertTrue(script.contains("if (!window.__VIDEOBROWSER_COMMON_SCRIPT_INSTALLED__) {"))
        assertTrue(script.contains(COMMON_SCRIPT))
        assertTrue(script.contains("window.__VIDEOBROWSER_COMMON_SCRIPT_INSTALLED__ = true;"))
        assertTrue(script.contains("window.VideoBrowserEnhancer.apply(config);"))
    }

    /**
     * 测试函数 `inject_reusesLoadedCommonScriptAcrossRepeatedCalls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject reuses Loaded Common Script Across Repeated Calls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

        assertEquals(ScriptLoader.COMMON_SCRIPT_ASSETS.size, loadCount)
        assertEquals(2, evaluatedScripts.size)
        assertTrue(
            evaluatedScripts[1].contains(
                "var config = {\"cleanupEnabled\":false,\"videoEnabled\":true," +
                    "\"cssSelectors\":[],\"userCssSelectors\":[],\"domSelectors\":[]," +
                    "\"blockedUrlKeywords\":[],\"scriptletWindowOpenBlockedKeywords\":[]," +
                    "\"scriptletFetchBlockedKeywords\":[],\"scriptletSkipButtonsEnabled\":false," +
                    "\"scriptletVideoControlsEnabled\":false};"
            )
        )
    }

    /**
     * 测试函数 `inject_skipsCommonScriptWhenJsInjectionDisabled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject skips Common Script When Js Injection Disabled` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `inject_loadsOnlyMatchingSiteScriptForPageUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject loads Only Matching Site Script For Page Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
            ScriptLoader.COMMON_SCRIPT_ASSETS + listOf(
                ScriptLoader.SITE_ADAPTER_HELPERS_SCRIPT_ASSET,
                "scripts/youtube.js"
            ),
            requestedPaths
        )
        assertTrue(script.contains("window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__"))
        assertTrue(script.contains("window.__siteHelpersLoaded = true;"))
        assertTrue(script.contains("window.__siteYoutubeLoaded = true;"))
        assertTrue(script.indexOf("window.__siteHelpersLoaded = true;") < script.indexOf("window.__siteYoutubeLoaded = true;"))
        assertTrue(script.contains("window.VideoBrowserSiteAdapters[\"youtube\"].apply(config);"))
        assertEquals(
            1,
            script.split("window.VideoBrowserSiteAdapters[\"youtube\"].apply(config);").size - 1
        )
        assertFalse(script.contains("__siteBilibiliLoaded"))
    }

    /**
     * 测试函数 `inject_skipsSiteScriptsForUnmatchedPageUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject skips Site Scripts For Unmatched Page Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
        assertEquals(ScriptLoader.COMMON_SCRIPT_ASSETS, requestedPaths)
        assertFalse(script.contains("window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__"))
        assertFalse(script.contains("__siteYoutubeLoaded"))
    }

    /**
     * 测试函数 `inject_wrapsSiteScriptsWithRepeatGuard`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject wraps Site Scripts With Repeat Guard` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
        assertTrue(
            script.contains(
                "if (!window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__[\"scripts/site_adapter_helpers.js\"]) {"
            )
        )
        assertTrue(script.contains("if (!window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__[\"scripts/bilibili_overlay_cleanup.js\"]) {"))
        assertTrue(script.contains("if (!window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__[\"scripts/bilibili_browser_choice_cleanup.js\"]) {"))
        assertTrue(script.contains("if (!window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__[\"scripts/bilibili.js\"]) {"))
        assertTrue(script.indexOf("scripts/bilibili_overlay_cleanup.js") < script.indexOf("scripts/bilibili.js"))
        assertTrue(script.indexOf("scripts/bilibili_browser_choice_cleanup.js") < script.indexOf("scripts/bilibili.js"))
        assertTrue(script.contains("window.__VIDEOBROWSER_SITE_SCRIPT_FLAGS__[\"scripts/bilibili.js\"] = true;"))
        assertTrue(script.contains("window.VideoBrowserSiteAdapters[\"bilibili\"].apply(config);"))
        assertEquals(
            1,
            script.split("window.VideoBrowserSiteAdapters[\"bilibili\"].apply(config);").size - 1
        )
    }

    /**
     * 测试函数 `inject_registersSiteAdaptersBeforeCommonEnhancerAppliesConfig`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject registers Site Adapters Before Common Enhancer Applies Config` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun inject_registersSiteAdaptersBeforeCommonEnhancerAppliesConfig() {
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
        val siteApplyIndex = script.indexOf("window.VideoBrowserSiteAdapters[\"bilibili\"].apply(config);")
        val commonApplyIndex = script.indexOf("window.VideoBrowserEnhancer.apply(config);")

        assertTrue(siteApplyIndex >= 0)
        assertTrue(commonApplyIndex >= 0)
        assertTrue(siteApplyIndex < commonApplyIndex)
    }

    /**
     * 测试函数 `inject_addsElementRuleSelectorsToFeatureConfig`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject adds Element Rule Selectors To Feature Config` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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
        assertTrue(script.contains("\"userCssSelectors\":[]"))
        assertTrue(script.contains("\"domSelectors\":[\".popup-ad\"]"))
        assertTrue(script.contains("\"blockedUrlKeywords\":[\"/pagead/\"]"))
    }

    /**
     * 测试函数 `inject_addsScriptletHookConfigWithoutRemovingSiteScripts`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject adds Scriptlet Hook Config Without Removing Site Scripts` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun inject_addsScriptletHookConfigWithoutRemovingSiteScripts() {
        val evaluatedScripts = mutableListOf<String>()
        val injector = JsInjector(
            scriptLoader = scriptLoaderForSiteScripts(),
            evaluateJavascript = { script -> evaluatedScripts += script },
            siteAdapterRegistry = SiteAdapterRegistry.default(),
            ruleEngine = RuleEngine(
                rules = emptyList(),
                scriptletRules = listOf(
                    ScriptletRule(
                        id = "hook:open",
                        name = "window-open-block-keyword",
                        arguments = listOf("/popup-ad/")
                    ),
                    ScriptletRule(
                        id = "hook:fetch",
                        name = "fetch-block-keyword",
                        arguments = listOf("/pagead/")
                    ),
                    ScriptletRule(
                        id = "hook:skip",
                        name = "click-skip-buttons"
                    ),
                    ScriptletRule(
                        id = "hook:controls",
                        name = "enable-video-controls"
                    )
                )
            )
        )

        injector.inject(
            PageFeatureConfig(cleanupEnabled = true, videoEnabled = true),
            pageUrl = "https://m.youtube.com/watch?v=1"
        )

        val script = evaluatedScripts.single()
        assertTrue(script.contains("\"scriptletWindowOpenBlockedKeywords\":[\"/popup-ad/\"]"))
        assertTrue(script.contains("\"scriptletFetchBlockedKeywords\":[\"/pagead/\"]"))
        assertTrue(script.contains("\"scriptletSkipButtonsEnabled\":true"))
        assertTrue(script.contains("\"scriptletVideoControlsEnabled\":true"))
        assertTrue(script.contains("window.VideoBrowserSiteAdapters[\"youtube\"].apply(config);"))
    }

    /**
     * 测试函数 `inject_addsUserCssSelectorsToFeatureConfig`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `inject adds User Css Selectors To Feature Config` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun inject_addsUserCssSelectorsToFeatureConfig() {
        val evaluatedScripts = mutableListOf<String>()
        val injector = JsInjector(
            scriptLoader = scriptLoaderFor(COMMON_SCRIPT),
            evaluateJavascript = { script -> evaluatedScripts += script }
        )

        injector.inject(
            PageFeatureConfig(
                cleanupEnabled = false,
                videoEnabled = false,
                userCssSelectors = listOf(".picked-ad")
            )
        )

        val script = evaluatedScripts.single()
        assertTrue(script.contains("\"userCssSelectors\":[\".picked-ad\"]"))
    }

    /**
     * 测试函数 `scriptLoaderFor`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `script Loader For` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param script 参数类型为 `String`，表示函数执行 `script` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun scriptLoaderFor(script: String): ScriptLoader {
        return ScriptLoader {
            ByteArrayInputStream(script.toByteArray(Charsets.UTF_8))
        }
    }

    /**
     * 测试函数 `scriptLoaderForSiteScripts`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `script Loader For Site Scripts` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun scriptLoaderForSiteScripts(): ScriptLoader {
        return ScriptLoader { path ->
            ByteArrayInputStream(scriptContentFor(path).toByteArray(Charsets.UTF_8))
        }
    }

    /**
     * 测试函数 `scriptContentFor`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `script Content For` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun scriptContentFor(path: String): String {
        return when (path) {
            ScriptLoader.GEOMETRY_SCRIPT_ASSET -> "window.__geometryLoaded = true;"
            ScriptLoader.DOM_TOOLS_SCRIPT_ASSET -> "window.__domToolsLoaded = true;"
            ScriptLoader.DOM_ACTIONS_SCRIPT_ASSET -> "window.__domActionsLoaded = true;"
            ScriptLoader.SELECTOR_TOOLS_SCRIPT_ASSET -> "window.__selectorToolsLoaded = true;"
            ScriptLoader.GENERIC_CLEANUP_SELECTORS_SCRIPT_ASSET -> "window.__genericCleanupSelectorsLoaded = true;"
            ScriptLoader.GENERATED_AD_CLEANUP_SCRIPT_ASSET -> "window.__generatedAdCleanupLoaded = true;"
            ScriptLoader.GENERIC_AD_OVERLAY_CLEANUP_SCRIPT_ASSET -> "window.__genericAdOverlayCleanupLoaded = true;"
            ScriptLoader.TOP_PAGE_CLEANUP_SCRIPT_ASSET -> "window.__topPageCleanupLoaded = true;"
            ScriptLoader.SEARCH_RESULT_CLEANUP_SCRIPT_ASSET -> "window.__searchResultCleanupLoaded = true;"
            ScriptLoader.SKIP_BUTTON_TOOLS_SCRIPT_ASSET -> "window.__skipButtonToolsLoaded = true;"
            ScriptLoader.NATIVE_BRIDGE_SCRIPT_ASSET -> "window.__nativeBridgeLoaded = true;"
            ScriptLoader.VIDEO_CONTROL_TOOLS_SCRIPT_ASSET -> "window.__videoControlToolsLoaded = true;"
            ScriptLoader.VIDEO_QUERY_TOOLS_SCRIPT_ASSET -> "window.__videoQueryToolsLoaded = true;"
            ScriptLoader.SITE_VIDEO_CAPABILITY_BROKER_SCRIPT_ASSET -> "window.__siteVideoCapabilityBrokerLoaded = true;"
            ScriptLoader.VIDEO_CUSTOM_CONTROL_DETECTOR_SCRIPT_ASSET -> "window.__videoCustomControlDetectorLoaded = true;"
            ScriptLoader.VIDEO_FULLSCREEN_TOOLS_SCRIPT_ASSET -> "window.__videoFullscreenToolsLoaded = true;"
            ScriptLoader.VIDEO_WAKE_TOOLS_SCRIPT_ASSET -> "window.__videoWakeToolsLoaded = true;"
            ScriptLoader.VIDEO_ENHANCEMENT_TOOLS_SCRIPT_ASSET -> "window.__videoEnhancementToolsLoaded = true;"
            ScriptLoader.VIDEO_PLAYBACK_TOOLS_SCRIPT_ASSET -> "window.__videoPlaybackToolsLoaded = true;"
            ScriptLoader.ELEMENT_PICKER_SCRIPT_ASSET -> "window.__elementPickerLoaded = true;"
            ScriptLoader.SCRIPTLET_HOOKS_SCRIPT_ASSET -> "window.__scriptletHooksLoaded = true;"
            ScriptLoader.STYLE_MANAGER_SCRIPT_ASSET -> "window.__styleManagerLoaded = true;"
            ScriptLoader.CONFIGURED_CLEANUP_SCRIPT_ASSET -> "window.__configuredCleanupLoaded = true;"
            ScriptLoader.PAGE_LIFECYCLE_TOOLS_SCRIPT_ASSET -> "window.__pageLifecycleToolsLoaded = true;"
            ScriptLoader.COMMON_SCRIPT_ASSET -> COMMON_SCRIPT
            ScriptLoader.SITE_ADAPTER_HELPERS_SCRIPT_ASSET -> "window.__siteHelpersLoaded = true;"
            "scripts/youtube.js" -> "window.__siteYoutubeLoaded = true;"
            "scripts/bilibili_overlay_cleanup.js" -> "window.__siteBilibiliOverlayCleanupLoaded = true;"
            "scripts/bilibili_browser_choice_cleanup.js" -> "window.__siteBilibiliBrowserChoiceCleanupLoaded = true;"
            "scripts/bilibili.js" -> "window.__siteBilibiliLoaded = true;"
            else -> error("Unexpected script path: $path")
        }
    }

    private companion object {
        private const val COMMON_SCRIPT =
            "window.VideoBrowserEnhancer={apply:function(config){window.__applied=config;}};"
    }
}
