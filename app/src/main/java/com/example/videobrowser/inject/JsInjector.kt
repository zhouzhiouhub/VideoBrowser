package com.example.videobrowser.inject

import com.example.videobrowser.site.SiteAdapterRegistry
import com.example.videobrowser.rules.RuleEngine

data class PageFeatureConfig(
    val jsInjectionEnabled: Boolean = true,
    val cleanupEnabled: Boolean,
    val videoEnabled: Boolean,
    val cssSelectors: List<String> = emptyList(),
    val userCssSelectors: List<String> = emptyList(),
    val domSelectors: List<String> = emptyList(),
    val blockedUrlKeywords: List<String> = emptyList(),
    val scriptletWindowOpenBlockedKeywords: List<String> = emptyList(),
    val scriptletFetchBlockedKeywords: List<String> = emptyList(),
    val scriptletSkipButtonsEnabled: Boolean = false,
    val scriptletVideoControlsEnabled: Boolean = false
)

/**
 * 负责组装并注入页面增强脚本，避免 MainActivity 直接拼接 assets 脚本内容。
 */
class JsInjector(
    private val scriptLoader: ScriptLoader,
    private val evaluateJavascript: (String) -> Unit,
    private val siteAdapterRegistry: SiteAdapterRegistry = SiteAdapterRegistry.default(),
    private val ruleEngine: RuleEngine = RuleEngine(emptyList())
) {
    private val commonScript: String by lazy(LazyThreadSafetyMode.NONE) {
        scriptLoader.loadCommonScript()
    }
    private val siteScriptCache = mutableMapOf<String, String>()

    fun inject(config: PageFeatureConfig, pageUrl: String? = null) {
        if (!config.jsInjectionEnabled) {
            return
        }
        val effectiveConfig = config.copy(
            cssSelectors = (config.cssSelectors + ruleEngine.cssSelectorsFor(pageUrl)).distinct(),
            domSelectors = (config.domSelectors + ruleEngine.domSelectorsFor(pageUrl)).distinct(),
            blockedUrlKeywords = (
                config.blockedUrlKeywords + ruleEngine.urlContainsBlockPatternsFor(pageUrl)
            ).distinct(),
            scriptletWindowOpenBlockedKeywords = (
                config.scriptletWindowOpenBlockedKeywords +
                    ruleEngine.scriptletWindowOpenBlockedKeywordsFor(pageUrl)
            ).distinct(),
            scriptletFetchBlockedKeywords = (
                config.scriptletFetchBlockedKeywords +
                    ruleEngine.scriptletFetchBlockedKeywordsFor(pageUrl)
            ).distinct(),
            scriptletSkipButtonsEnabled = config.scriptletSkipButtonsEnabled ||
                ruleEngine.isScriptletSkipButtonsEnabledFor(pageUrl),
            scriptletVideoControlsEnabled = config.scriptletVideoControlsEnabled ||
                ruleEngine.isScriptletVideoControlsEnabledFor(pageUrl)
        )
        val commonScriptContent = commonScript
        val siteScripts = siteAdapterRegistry.matchingAdapters(pageUrl).flatMap { adapter ->
            adapter.scriptFiles().map { path ->
                SiteScript(
                    adapterId = adapter.profile.id,
                    path = path,
                    content = loadSiteScript(path)
                )
            }
        }.distinctBy { script -> script.path }
        evaluateJavascript(buildInjectionScript(commonScriptContent, effectiveConfig, siteScripts))
    }

    internal fun buildInjectionScript(config: PageFeatureConfig): String {
        return buildInjectionScript(commonScript, config, emptyList())
    }

    private fun loadSiteScript(path: String): String {
        return siteScriptCache.getOrPut(path) {
            scriptLoader.loadScript(path)
        }
    }

    internal companion object {
        const val COMMON_SCRIPT_INSTALLED_FLAG = "__VIDEOBROWSER_COMMON_SCRIPT_INSTALLED__"
        const val SITE_SCRIPT_FLAGS = "__VIDEOBROWSER_SITE_SCRIPT_FLAGS__"

        fun buildInjectionScript(
            commonScript: String,
            config: PageFeatureConfig,
            siteScripts: List<SiteScript> = emptyList()
        ): String {
            return buildString {
                appendLine("(function () {")
                append("  var config = ")
                append(config.toJsonLiteral())
                appendLine(";")
                appendLine("  window.__VIDEOBROWSER_CONFIG__ = config;")
                append("  if (!window.")
                append(COMMON_SCRIPT_INSTALLED_FLAG)
                appendLine(") {")
                appendLine(commonScript)
                append("    window.")
                append(COMMON_SCRIPT_INSTALLED_FLAG)
                appendLine(" = true;")
                appendLine("  }")
                if (siteScripts.isNotEmpty()) {
                    append("  window.")
                    append(SITE_SCRIPT_FLAGS)
                    append(" = window.")
                    append(SITE_SCRIPT_FLAGS)
                    appendLine(" || {};")
                    siteScripts.forEach { siteScript ->
                        append("  if (!window.")
                        append(SITE_SCRIPT_FLAGS)
                        append("[")
                        append(siteScript.path.toJsonStringLiteral())
                        appendLine("]) {")
                        appendLine(siteScript.content)
                        append("    window.")
                        append(SITE_SCRIPT_FLAGS)
                        append("[")
                        append(siteScript.path.toJsonStringLiteral())
                        appendLine("] = true;")
                        appendLine("  }")
                        appendLine(siteScript.buildApplyCall())
                    }
                }
                appendLine(
                    "  if (window.VideoBrowserEnhancer && " +
                        "typeof window.VideoBrowserEnhancer.apply === 'function') {"
                )
                appendLine("    window.VideoBrowserEnhancer.apply(config);")
                appendLine("  }")
                appendLine("})();")
            }
        }

        private fun PageFeatureConfig.toJsonLiteral(): String {
            return buildString {
                append("{\"cleanupEnabled\":")
                append(cleanupEnabled)
                append(",\"videoEnabled\":")
                append(videoEnabled)
                append(",\"cssSelectors\":")
                append(cssSelectors.toJsonArrayLiteral())
                append(",\"userCssSelectors\":")
                append(userCssSelectors.toJsonArrayLiteral())
                append(",\"domSelectors\":")
                append(domSelectors.toJsonArrayLiteral())
                append(",\"blockedUrlKeywords\":")
                append(blockedUrlKeywords.toJsonArrayLiteral())
                append(",\"scriptletWindowOpenBlockedKeywords\":")
                append(scriptletWindowOpenBlockedKeywords.toJsonArrayLiteral())
                append(",\"scriptletFetchBlockedKeywords\":")
                append(scriptletFetchBlockedKeywords.toJsonArrayLiteral())
                append(",\"scriptletSkipButtonsEnabled\":")
                append(scriptletSkipButtonsEnabled)
                append(",\"scriptletVideoControlsEnabled\":")
                append(scriptletVideoControlsEnabled)
                append("}")
            }
        }

        private fun SiteScript.buildApplyCall(): String {
            val adapterIdLiteral = adapterId.toJsonStringLiteral()
            return buildString {
                append("  if (window.VideoBrowserSiteAdapters && ")
                append("window.VideoBrowserSiteAdapters[")
                append(adapterIdLiteral)
                append("] && typeof window.VideoBrowserSiteAdapters[")
                append(adapterIdLiteral)
                appendLine("].apply === 'function') {")
                append("    window.VideoBrowserSiteAdapters[")
                append(adapterIdLiteral)
                appendLine("].apply(config);")
                appendLine("  }")
            }
        }

        private fun String.toJsonStringLiteral(): String {
            return buildString {
                append('"')
                this@toJsonStringLiteral.forEach { char ->
                    when (char) {
                        '\\' -> append("\\\\")
                        '"' -> append("\\\"")
                        '\n' -> append("\\n")
                        '\r' -> append("\\r")
                        '\t' -> append("\\t")
                        else -> {
                            if (char.code < 0x20) {
                                append("\\u")
                                append(char.code.toString(16).padStart(4, '0'))
                            } else {
                                append(char)
                            }
                        }
                    }
                }
                append('"')
            }
        }

        private fun List<String>.toJsonArrayLiteral(): String {
            return joinToString(prefix = "[", postfix = "]") { value ->
                value.toJsonStringLiteral()
            }
        }
    }
}

internal data class SiteScript(
    val adapterId: String,
    val path: String,
    val content: String
)
