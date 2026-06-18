package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 RuleFileLoader 可以拆开理解为“Rule File Loader”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import android.content.res.AssetManager
import java.io.File
import java.io.InputStream
import java.util.Properties

/**
 * 规则文件加载器。
 *
 * assets/rules 是随 App 内置的默认规则，cacheDirectory 是用户订阅下载后的缓存规则。
 * 加载时先读内置规则再读缓存规则，因此用户订阅可以在同一个 RuleEngine 中参与匹配。
 */
class RuleFileLoader(
    private val openAsset: (String) -> InputStream?,
    private val cacheDirectory: File? = null
) {
    private val ruleLineParser = RuleLineParser()
    private val cacheSourceLabel: String? by lazy { readCacheSourceLabel() }

    /**
     * 函数 `loadRequestRules`：启动或加载 `load Request Rules` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun loadRequestRules(): RuleLoadResult<Rule> {
        return loadRules(
            assetPath = REQUEST_RULES_ASSET,
            cacheFileName = REQUEST_RULES_CACHE_FILE,
            parser = ruleLineParser::parseRequestRule
        )
    }

    /**
     * 函数 `loadCssRules`：启动或加载 `load Css Rules` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun loadCssRules(): RuleLoadResult<ElementRule> {
        return loadRules(
            assetPath = CSS_RULES_ASSET,
            cacheFileName = CSS_RULES_CACHE_FILE,
            parser = { line, source, lineNumber ->
                ruleLineParser.parseCssRule(line, source, lineNumber)
            }
        )
    }

    /**
     * 函数 `loadDomRules`：启动或加载 `load Dom Rules` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun loadDomRules(): RuleLoadResult<ElementRule> {
        return loadRules(
            assetPath = DOM_RULES_ASSET,
            cacheFileName = DOM_RULES_CACHE_FILE,
            parser = { line, source, lineNumber ->
                ruleLineParser.parseDomRule(line, source, lineNumber)
            }
        )
    }

    /**
     * 函数 `loadScriptletRules`：启动或加载 `load Scriptlet Rules` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun loadScriptletRules(): RuleLoadResult<ScriptletRule> {
        return loadRules(
            assetPath = SCRIPTLET_RULES_ASSET,
            cacheFileName = SCRIPTLET_RULES_CACHE_FILE,
            parser = { line, source, lineNumber ->
                ruleLineParser.parseScriptletRule(line, source, lineNumber)
            }
        )
    }

    /**
     * 函数 `loadRemoveParamRules`：启动或加载 `load Remove Param Rules` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun loadRemoveParamRules(): RuleLoadResult<RemoveParamRule> {
        return loadRules(
            assetPath = REMOVE_PARAM_RULES_ASSET,
            cacheFileName = REMOVE_PARAM_RULES_CACHE_FILE,
            parser = { line, source, lineNumber ->
                ruleLineParser.parseRemoveParamRule(line, source, lineNumber)
            }
        )
    }

    /**
     * 函数 `loadRules`：启动或加载 `load Rules` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param assetPath 参数类型为 `String`，表示函数执行 `assetPath` 相关逻辑时需要读取或处理的输入。
     * @param cacheFileName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param parser 参数类型为 `(String, String, Int) -> ParsedRule<T>`，表示函数执行 `parser` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun <T> loadRules(
        assetPath: String,
        cacheFileName: String,
        parser: (String, String, Int) -> ParsedRule<T>
    ): RuleLoadResult<T> {
        // 同一种规则可能来自两个位置：安装包 assets 和本机缓存。
        // 两边解析结果会合并，跳过的规则也会保留下来，方便调试订阅质量。
        val rules = mutableListOf<T>()
        val skippedRules = mutableListOf<SkippedRule>()

        readRules(
            source = "asset:$assetPath",
            streamProvider = { openAsset(assetPath) },
            parser = parser,
            rules = rules,
            skippedRules = skippedRules
        )
        readRules(
            source = cacheSource(cacheFileName),
            streamProvider = { openCacheFile(cacheFileName) },
            parser = parser,
            rules = rules,
            skippedRules = skippedRules
        )

        return RuleLoadResult(rules = rules, skippedRules = skippedRules)
    }

    /**
     * 函数 `readRules`：封装 `read Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @param streamProvider 参数类型为 `() -> InputStream?`，表示函数执行 `streamProvider` 相关逻辑时需要读取或处理的输入。
     * @param parser 参数类型为 `(String, String, Int) -> ParsedRule<T>`，表示函数执行 `parser` 相关逻辑时需要读取或处理的输入。
     * @param rules 参数类型为 `MutableList<T>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param skippedRules 参数类型为 `MutableList<SkippedRule>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     */
    private fun <T> readRules(
        source: String,
        streamProvider: () -> InputStream?,
        parser: (String, String, Int) -> ParsedRule<T>,
        rules: MutableList<T>,
        skippedRules: MutableList<SkippedRule>
    ) {
        // 逐行解析可以让错误定位到具体 source:line，订阅里一条坏规则不会影响其他规则。
        val stream = runCatching { streamProvider() }.getOrNull() ?: return
        runCatching {
            stream.bufferedReader(Charsets.UTF_8).useLines { lines ->
                lines.forEachIndexed { index, line ->
                    when (val parsedRule = parser(line, source, index + 1)) {
                        is ParsedRule.Rule -> rules += parsedRule.value
                        is ParsedRule.Skipped -> skippedRules += parsedRule.skippedRule
                        ParsedRule.Ignored -> Unit
                    }
                }
            }
        }.onFailure { error ->
            skippedRules += SkippedRule(
                source = source,
                lineNumber = 0,
                text = "",
                reason = error.message ?: "failed to read rule file"
            )
        }
    }

    /**
     * 函数 `openCacheFile`：启动或加载 `open Cache File` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param fileName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun openCacheFile(fileName: String): InputStream? {
        return cacheDirectory
            ?.resolve(fileName)
            ?.takeIf { file -> file.isFile }
            ?.inputStream()
    }

    /**
     * 函数 `cacheSource`：封装 `cache Source` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param fileName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun cacheSource(fileName: String): String {
        val label = cacheSourceLabel
        return if (label.isNullOrBlank()) {
            "cache:$fileName"
        } else {
            "cache:$fileName:$label"
        }
    }

    /**
     * 函数 `readCacheSourceLabel`：封装 `read Cache Source Label` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun readCacheSourceLabel(): String? {
        val metadataFile = cacheDirectory
            ?.resolve(RULE_CACHE_METADATA_FILE)
            ?.takeIf { file -> file.isFile }
            ?: return null
        return runCatching {
            metadataFile.inputStream().use { input ->
                Properties().apply { load(input) }
            }.getProperty(METADATA_SOURCE_LABEL)
                ?.trim()
                ?.takeIf { label -> label.isNotEmpty() }
        }.getOrNull()
    }

    companion object {
        const val REQUEST_RULES_ASSET = "rules/request_rules.txt"
        const val CSS_RULES_ASSET = "rules/css_rules.txt"
        const val DOM_RULES_ASSET = "rules/dom_rules.txt"
        const val SCRIPTLET_RULES_ASSET = "rules/scriptlet_rules.txt"
        const val REMOVE_PARAM_RULES_ASSET = "rules/removeparam_rules.txt"
        const val REQUEST_RULES_CACHE_FILE = "request_rules.txt"
        const val CSS_RULES_CACHE_FILE = "css_rules.txt"
        const val DOM_RULES_CACHE_FILE = "dom_rules.txt"
        const val SCRIPTLET_RULES_CACHE_FILE = "scriptlet_rules.txt"
        const val REMOVE_PARAM_RULES_CACHE_FILE = "removeparam_rules.txt"
        const val RULE_CACHE_METADATA_FILE = "metadata.properties"
        const val METADATA_SOURCE_LABEL = "source_label"

        /**
         * 函数 `fromAssets`：封装 `from Assets` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param assets 参数类型为 `AssetManager`，表示函数执行 `assets` 相关逻辑时需要读取或处理的输入。
         * @param cacheDirectory 参数类型为 `File?`，表示函数执行 `cacheDirectory` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun fromAssets(assets: AssetManager, cacheDirectory: File? = null): RuleFileLoader {
            return RuleFileLoader(
                openAsset = { path -> assets.open(path) },
                cacheDirectory = cacheDirectory
            )
        }
    }
}

data class RuleLoadResult<T>(
    val rules: List<T>,
    val skippedRules: List<SkippedRule>
)

data class SkippedRule(
    val source: String,
    val lineNumber: Int,
    val text: String,
    val reason: String
)

internal sealed class ParsedRule<out T> {
    data class Rule<T>(val value: T) : ParsedRule<T>()
    data class Skipped(val skippedRule: SkippedRule) : ParsedRule<Nothing>()
    object Ignored : ParsedRule<Nothing>()
}
