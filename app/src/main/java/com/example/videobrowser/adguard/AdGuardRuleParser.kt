package com.example.videobrowser.adguard

/**
 * 初学者阅读提示：
 * 这个文件属于“AdGuard 规则解析模块”。
 * 文件名 AdGuardRuleParser 可以拆开理解为“Ad Guard Rule Parser”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把外部订阅里的 AdGuard 风格规则拆分成项目内部可理解的规则文本。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.rules.DomainScopeParser
import com.example.videobrowser.rules.ElementRule
import com.example.videobrowser.rules.ElementRuleType
import com.example.videobrowser.rules.RemoveParamRule
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.ScriptletParseResult
import com.example.videobrowser.rules.ScriptletRegistry
import com.example.videobrowser.rules.ScriptletRule
import com.example.videobrowser.rules.SkippedRule
import java.util.Locale

/**
 * AdGuard 订阅解析器。
 *
 * 订阅文本里可能混有请求拦截、元素隐藏、安全脚本、移除参数和注释。
 * 这个类逐行识别规则类型，把项目支持的部分转成内部模型，不支持的部分记录到 skippedRules。
 */
class AdGuardRuleParser {
    /**
     * 函数 `parseSubscription`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun parseSubscription(text: String, source: String): AdGuardParseResult {
        // 每类规则既保存解析后的对象，也保存原始行，方便后续缓存到对应规则文件。
        val requestRules = mutableListOf<Rule>()
        val elementRules = mutableListOf<ElementRule>()
        val scriptletRules = mutableListOf<ScriptletRule>()
        val removeParamRules = mutableListOf<RemoveParamRule>()
        val skippedRules = mutableListOf<SkippedRule>()
        val requestRuleLines = mutableListOf<String>()
        val cssRuleLines = mutableListOf<String>()
        val scriptletRuleLines = mutableListOf<String>()
        val removeParamRuleLines = mutableListOf<String>()

        text.lineSequence().forEachIndexed { index, line ->
            val lineNumber = index + 1
            when (val parsed = parseLine(line, source, lineNumber)) {
                is ParsedAdGuardLine.Request -> {
                    requestRules += parsed.rule
                    requestRuleLines += line.trim()
                }
                is ParsedAdGuardLine.Element -> {
                    elementRules += parsed.rule
                    cssRuleLines += line.trim()
                }
                is ParsedAdGuardLine.Scriptlet -> {
                    scriptletRules += parsed.rule
                    scriptletRuleLines += line.trim()
                }
                is ParsedAdGuardLine.RemoveParam -> {
                    removeParamRules += parsed.rule
                    removeParamRuleLines += line.trim()
                }
                is ParsedAdGuardLine.Skipped -> skippedRules += parsed.skippedRule
                ParsedAdGuardLine.Ignored -> Unit
            }
        }

        return AdGuardParseResult(
            requestRules = requestRules,
            elementRules = elementRules,
            scriptletRules = scriptletRules,
            removeParamRules = removeParamRules,
            skippedRules = skippedRules,
            requestRuleLines = requestRuleLines,
            cssRuleLines = cssRuleLines,
            scriptletRuleLines = scriptletRuleLines,
            removeParamRuleLines = removeParamRuleLines
        )
    }

    /**
     * 函数 `parseRequestRule`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun parseRequestRule(text: String, id: String, source: String): Rule? {
        return Rule.fromRequestRuleText(text = text, id = id, source = source)
    }

    /**
     * 函数 `parseRemoveParamRule`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun parseRemoveParamRule(text: String, id: String, source: String): RemoveParamRule? {
        return RemoveParamRule.fromAdGuardRuleText(text = text, id = id, source = source)
    }

    /**
     * 函数 `parseElementRule`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun parseElementRule(text: String, id: String, source: String): ElementRule? {
        val trimmed = text.trim()
        if (isIgnored(trimmed) || isScriptletRuleLine(trimmed) || trimmed.contains("#?#")) {
            return null
        }

        val exceptionMarkerIndex = trimmed.indexOf("#@#")
        val hideMarkerIndex = trimmed.indexOf("##")
        val isException = exceptionMarkerIndex >= 0
        val markerIndex = if (isException) exceptionMarkerIndex else hideMarkerIndex
        val markerLength = if (isException) 3 else 2
        if (markerIndex < 0) {
            return null
        }

        val domainScope = DomainScopeParser.parseCommaSeparated(trimmed.substring(0, markerIndex)) ?: return null
        val selector = trimmed.substring(markerIndex + markerLength).trim()
        if (!isSafeSelector(selector)) {
            return null
        }

        return ElementRule(
            id = id,
            selector = selector,
            type = if (isException) ElementRuleType.CSS_UNHIDE else ElementRuleType.CSS_HIDE,
            source = source,
            domains = domainScope.includedDomains,
            excludedDomains = domainScope.excludedDomains
        )
    }

    /**
     * 函数 `parseScriptletRule`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun parseScriptletRule(text: String, id: String, source: String): ScriptletRule? {
        return when (val result = ScriptletRegistry.parse(text, id, source)) {
            is ScriptletParseResult.Rule -> result.value
            else -> null
        }
    }

    /**
     * 函数 `parseLine`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param line 参数类型为 `String`，表示函数执行 `line` 相关逻辑时需要读取或处理的输入。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @param lineNumber 参数类型为 `Int`，表示函数执行 `lineNumber` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun parseLine(line: String, source: String, lineNumber: Int): ParsedAdGuardLine {
        val trimmed = line.trim()
        if (isIgnored(trimmed)) {
            return ParsedAdGuardLine.Ignored
        }

        val id = "$source:$lineNumber"
        // 解析顺序很重要：先识别更特殊的规则，再退回到普通请求规则。
        if (trimmed.contains("\$removeparam=", ignoreCase = true)) {
            return parseRemoveParamRule(trimmed, id, source)?.let(ParsedAdGuardLine::RemoveParam)
                ?: skipped(source, lineNumber, line)
        }
        if (isScriptletRuleLine(trimmed)) {
            return when (val result = ScriptletRegistry.parse(trimmed, id, source)) {
                ScriptletParseResult.Ignored -> ParsedAdGuardLine.Ignored
                is ScriptletParseResult.Rule -> ParsedAdGuardLine.Scriptlet(result.value)
                is ScriptletParseResult.Skipped -> ParsedAdGuardLine.Skipped(
                    SkippedRule(source, lineNumber, trimmed, result.reason)
                )
            }
        }
        if (trimmed.contains("##") || trimmed.contains("#@#") || trimmed.contains("#?#")) {
            return parseElementRule(trimmed, id, source)?.let(ParsedAdGuardLine::Element)
                ?: skipped(source, lineNumber, line)
        }
        return parseRequestRule(trimmed, id, source)?.let(ParsedAdGuardLine::Request)
            ?: skipped(source, lineNumber, line)
    }

    /**
     * 函数 `skipped`：封装 `skipped` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @param lineNumber 参数类型为 `Int`，表示函数执行 `lineNumber` 相关逻辑时需要读取或处理的输入。
     * @param line 参数类型为 `String`，表示函数执行 `line` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun skipped(source: String, lineNumber: Int, line: String): ParsedAdGuardLine.Skipped {
        return ParsedAdGuardLine.Skipped(
            SkippedRule(
                source = source,
                lineNumber = lineNumber,
                text = line.trim(),
                reason = "unsupported rule syntax"
            )
        )
    }

    /**
     * 函数 `isIgnored`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param trimmed 参数类型为 `String`，表示函数执行 `trimmed` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isIgnored(trimmed: String): Boolean {
        return trimmed.isEmpty() ||
            trimmed.startsWith("!") ||
            trimmed.startsWith("# ") ||
            trimmed == "#"
    }

    /**
     * 函数 `isSafeSelector`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param selector 参数类型为 `String`，表示函数执行 `selector` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isSafeSelector(selector: String): Boolean {
        // CSS selector 会被注入网页执行，所以只接受简单、安全、长度可控的 selector。
        val value = selector.trim()
        if (value.isEmpty() || value.length > MAX_SELECTOR_LENGTH) {
            return false
        }
        if (value.any { char -> char == '{' || char == '}' || char == ';' || char == '<' || char == '>' }) {
            return false
        }
        val lowered = value.lowercase(Locale.US)
        return !UNSUPPORTED_SELECTOR_TOKENS.any { token -> lowered.contains(token) }
    }

    /**
     * 函数 `isScriptletRuleLine`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param trimmed 参数类型为 `String`，表示函数执行 `trimmed` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isScriptletRuleLine(trimmed: String): Boolean {
        return trimmed.contains("##+js(") ||
            trimmed.contains("#%#")
    }

    private sealed class ParsedAdGuardLine {
        data class Request(val rule: Rule) : ParsedAdGuardLine()
        data class Element(val rule: ElementRule) : ParsedAdGuardLine()
        data class Scriptlet(val rule: ScriptletRule) : ParsedAdGuardLine()
        data class RemoveParam(val rule: RemoveParamRule) : ParsedAdGuardLine()
        data class Skipped(val skippedRule: SkippedRule) : ParsedAdGuardLine()
        object Ignored : ParsedAdGuardLine()
    }

    private companion object {
        const val MAX_SELECTOR_LENGTH = 200
        val UNSUPPORTED_SELECTOR_TOKENS = listOf(
            ":has(",
            ":contains(",
            ":matches(",
            ":xpath(",
            "javascript:",
            "expression("
        )
    }
}

data class AdGuardParseResult(
    val requestRules: List<Rule> = emptyList(),
    val elementRules: List<ElementRule> = emptyList(),
    val scriptletRules: List<ScriptletRule> = emptyList(),
    val removeParamRules: List<RemoveParamRule> = emptyList(),
    val skippedRules: List<SkippedRule> = emptyList(),
    val requestRuleLines: List<String> = emptyList(),
    val cssRuleLines: List<String> = emptyList(),
    val scriptletRuleLines: List<String> = emptyList(),
    val removeParamRuleLines: List<String> = emptyList()
)
