package com.example.videobrowser.inject

/**
 * 初学者阅读提示：
 * 这个文件属于“页面脚本注入模块”。
 * 文件名 JsInjector 可以拆开理解为“Js Injector”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取内置 JavaScript，按当前站点和设置组合注入脚本，让页面净化和视频增强生效。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.site.SiteAdapterRegistry
import com.example.videobrowser.rules.RuleEngine

data class PageFeatureConfig(
    val jsInjectionEnabled: Boolean = true,
    val cleanupEnabled: Boolean,
    val videoEnabled: Boolean,
    val builtInSearchResultPage: Boolean = false,
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

    /**
     * 函数 `inject`：封装 `inject` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param config 参数类型为 `PageFeatureConfig`，表示本次操作的配置集合，函数会按这些开关和参数调整行为。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    fun inject(config: PageFeatureConfig, pageUrl: String? = null) {
        if (!config.jsInjectionEnabled) {
            return
        }
        // effectiveConfig 合并“用户设置”和“规则引擎计算结果”，后面的 JS 只需要读取一个 config。
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
        val matchingAdapters = siteAdapterRegistry.matchingAdapters(pageUrl)
        val siteDependencyScripts = if (matchingAdapters.isEmpty()) {
            emptyList()
        } else {
            listOf(
                ScriptAsset(
                    path = ScriptLoader.SITE_ADAPTER_HELPERS_SCRIPT_ASSET,
                    content = loadSiteScript(ScriptLoader.SITE_ADAPTER_HELPERS_SCRIPT_ASSET)
                )
            )
        }
        // 不同站点可能需要额外脚本；相同脚本 path 去重，避免重复注入。
        val siteScripts = matchingAdapters.flatMap { adapter ->
            adapter.scriptFiles().map { path ->
                SiteScript(
                    adapterId = adapter.profile.id,
                    path = path,
                    content = loadSiteScript(path)
                )
            }
        }.distinctBy { script -> script.path }
        evaluateJavascript(
            buildInjectionScript(
                commonScript = commonScriptContent,
                config = effectiveConfig,
                siteDependencyScripts = siteDependencyScripts,
                siteScripts = siteScripts
            )
        )
    }

    /**
     * 函数 `buildInjectionScript`：创建 `build Injection Script` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param config 参数类型为 `PageFeatureConfig`，表示本次操作的配置集合，函数会按这些开关和参数调整行为。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    internal fun buildInjectionScript(config: PageFeatureConfig): String {
        return buildInjectionScript(commonScript, config)
    }

    /**
     * 函数 `loadSiteScript`：启动或加载 `load Site Script` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun loadSiteScript(path: String): String {
        return siteScriptCache.getOrPut(path) {
            scriptLoader.loadScript(path)
        }
    }

    internal companion object {
        const val COMMON_SCRIPT_INSTALLED_FLAG = "__VIDEOBROWSER_COMMON_SCRIPT_INSTALLED__"
        const val SITE_SCRIPT_FLAGS = "__VIDEOBROWSER_SITE_SCRIPT_FLAGS__"

        /**
         * 函数 `buildInjectionScript`：创建 `build Injection Script` 需要的对象、视图或配置，并返回给后续流程使用。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param commonScript 参数类型为 `String`，表示函数执行 `commonScript` 相关逻辑时需要读取或处理的输入。
         * @param config 参数类型为 `PageFeatureConfig`，表示本次操作的配置集合，函数会按这些开关和参数调整行为。
         * @param siteDependencyScripts 参数类型为 `List<ScriptAsset>`，表示站点脚本运行前必须存在、但自身不触发 adapter apply 的共享依赖脚本。
         * @param siteScripts 参数类型为 `List<SiteScript>`，表示函数执行 `siteScripts` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun buildInjectionScript(
            commonScript: String,
            config: PageFeatureConfig,
            siteDependencyScripts: List<ScriptAsset> = emptyList(),
            siteScripts: List<SiteScript> = emptyList()
        ): String {
            // 这里生成一个自执行函数，确保变量只存在于这一段脚本内部，不污染网页全局作用域。
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
                if (siteDependencyScripts.isNotEmpty() || siteScripts.isNotEmpty()) {
                    append("  window.")
                    append(SITE_SCRIPT_FLAGS)
                    append(" = window.")
                    append(SITE_SCRIPT_FLAGS)
                    appendLine(" || {};")
                    siteDependencyScripts.forEach { dependencyScript ->
                        appendScriptGuard(dependencyScript)
                    }
                    siteScripts.forEach { siteScript ->
                        appendScriptGuard(siteScript)
                    }
                    siteScripts.distinctBy { siteScript -> siteScript.adapterId }.forEach { siteScript ->
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

        /**
         * 函数 `appendScriptGuard`：把一个脚本放进页面注入脚本，并保证同一路径只安装一次。
         *
         * 站点共享依赖和站点专属脚本使用同一个 guard map，但只有 SiteScript 会在后续触发
         * adapter.apply(config)，避免共享 helper 被误认为站点入口。
         */
        private fun StringBuilder.appendScriptGuard(script: ScriptAsset) {
            append("  if (!window.")
            append(SITE_SCRIPT_FLAGS)
            append("[")
            append(script.path.toJsonStringLiteral())
            appendLine("]) {")
            appendLine(script.content)
            append("    window.")
            append(SITE_SCRIPT_FLAGS)
            append("[")
            append(script.path.toJsonStringLiteral())
            appendLine("] = true;")
            appendLine("  }")
        }

        /**
         * 函数 `toJsonLiteral`：封装 `to Json Literal` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun PageFeatureConfig.toJsonLiteral(): String {
            return buildString {
                append("{\"cleanupEnabled\":")
                append(cleanupEnabled)
                append(",\"videoEnabled\":")
                append(videoEnabled)
                append(",\"builtInSearchResultPage\":")
                append(builtInSearchResultPage)
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

        /**
         * 函数 `buildApplyCall`：创建 `build Apply Call` 需要的对象、视图或配置，并返回给后续流程使用。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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

        /**
         * 函数 `toJsonStringLiteral`：封装 `to Json String Literal` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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

        /**
         * 函数 `List`：封装 `List` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun List<String>.toJsonArrayLiteral(): String {
            return joinToString(prefix = "[", postfix = "]") { value ->
                value.toJsonStringLiteral()
            }
        }
    }
}

internal open class ScriptAsset(
    val path: String,
    val content: String
)

internal class SiteScript(
    val adapterId: String,
    path: String,
    content: String
) : ScriptAsset(path, content)
