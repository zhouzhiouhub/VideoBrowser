package com.example.videobrowser.inject

data class PageFeatureConfig(
    val cleanupEnabled: Boolean,
    val videoEnabled: Boolean
)

/**
 * 负责组装并注入页面增强脚本，避免 MainActivity 直接拼接 assets 脚本内容。
 */
class JsInjector(
    private val scriptLoader: ScriptLoader,
    private val evaluateJavascript: (String) -> Unit
) {
    private val commonScript: String by lazy(LazyThreadSafetyMode.NONE) {
        scriptLoader.loadCommonScript()
    }

    fun inject(config: PageFeatureConfig) {
        evaluateJavascript(buildInjectionScript(commonScript, config))
    }

    internal fun buildInjectionScript(config: PageFeatureConfig): String {
        return buildInjectionScript(commonScript, config)
    }

    internal companion object {
        const val COMMON_SCRIPT_INSTALLED_FLAG = "__VIDEOBROWSER_COMMON_SCRIPT_INSTALLED__"

        fun buildInjectionScript(commonScript: String, config: PageFeatureConfig): String {
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
                appendLine("})();")
            }
        }

        private fun PageFeatureConfig.toJsonLiteral(): String {
            return "{\"cleanupEnabled\":$cleanupEnabled,\"videoEnabled\":$videoEnabled}"
        }
    }
}
