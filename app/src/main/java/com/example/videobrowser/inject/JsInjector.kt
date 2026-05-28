package com.example.videobrowser.inject

import com.example.videobrowser.site.SiteAdapterRegistry

data class PageFeatureConfig(
    val cleanupEnabled: Boolean,
    val videoEnabled: Boolean
)

/**
 * 负责组装并注入页面增强脚本，避免 MainActivity 直接拼接 assets 脚本内容。
 */
class JsInjector(
    private val scriptLoader: ScriptLoader,
    private val evaluateJavascript: (String) -> Unit,
    private val siteAdapterRegistry: SiteAdapterRegistry = SiteAdapterRegistry.default()
) {
    private val commonScript: String by lazy(LazyThreadSafetyMode.NONE) {
        scriptLoader.loadCommonScript()
    }
    private val siteScriptCache = mutableMapOf<String, String>()

    fun inject(config: PageFeatureConfig, pageUrl: String? = null) {
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
        evaluateJavascript(buildInjectionScript(commonScriptContent, config, siteScripts))
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
                appendLine(
                    "  if (window.VideoBrowserEnhancer && " +
                        "typeof window.VideoBrowserEnhancer.apply === 'function') {"
                )
                appendLine("    window.VideoBrowserEnhancer.apply(config);")
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
                appendLine("})();")
            }
        }

        private fun PageFeatureConfig.toJsonLiteral(): String {
            return "{\"cleanupEnabled\":$cleanupEnabled,\"videoEnabled\":$videoEnabled}"
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
    }
}

internal data class SiteScript(
    val adapterId: String,
    val path: String,
    val content: String
)
