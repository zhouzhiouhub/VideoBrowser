package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 ScriptletRegistry 可以拆开理解为“Scriptlet Registry”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import java.util.Locale

object ScriptletRegistry {
    const val REASON_UNSUPPORTED_SYNTAX = "unsupported scriptlet syntax"
    const val REASON_UNSUPPORTED_SCRIPTLET = "unsupported scriptlet"
    const val REASON_INVALID_ARGUMENTS = "invalid scriptlet arguments"
    const val REASON_INVALID_DOMAIN = "invalid scriptlet domain"
    const val REASON_RAW_SCRIPTLET_JAVASCRIPT = "raw scriptlet javascript not allowed"
    const val HOOK_WINDOW_OPEN_BLOCK_KEYWORD = "window-open-block-keyword"
    const val HOOK_FETCH_BLOCK_KEYWORD = "fetch-block-keyword"
    const val HOOK_CLICK_SKIP_BUTTONS = "click-skip-buttons"
    const val HOOK_ENABLE_VIDEO_CONTROLS = "enable-video-controls"

    /**
     * 函数 `parse`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun parse(
        text: String,
        id: String,
        source: String
    ): ScriptletParseResult {
        val trimmed = text.trim()
        if (shouldIgnoreRuleLine(trimmed)) {
            return ScriptletParseResult.Ignored
        }

        val syntax = parseSyntax(trimmed)
            ?: return if (trimmed.contains(RAW_SCRIPTLET_MARKER)) {
                ScriptletParseResult.Skipped(REASON_RAW_SCRIPTLET_JAVASCRIPT)
            } else {
                ScriptletParseResult.Skipped(REASON_UNSUPPORTED_SYNTAX)
            }
        val domainScope = DomainScopeParser.parseCommaSeparated(
            syntax.domainText,
            requireDomain = false
        )
            ?: return ScriptletParseResult.Skipped(REASON_INVALID_DOMAIN)
        val arguments = splitArguments(syntax.argumentsText)
            ?: return ScriptletParseResult.Skipped(REASON_INVALID_ARGUMENTS)
        if (arguments.isEmpty()) {
            return ScriptletParseResult.Skipped(REASON_UNSUPPORTED_SYNTAX)
        }

        val name = normalizeArgument(arguments.first()).lowercase(Locale.US)
        val scriptletArguments = arguments.drop(1).map { argument ->
            normalizeArgument(argument)
        }
        return when (validate(name, scriptletArguments)) {
            ScriptletValidation.Valid -> ScriptletParseResult.Rule(
                ScriptletRule(
                    id = id,
                    name = name,
                    arguments = scriptletArguments,
                    source = source,
                    domainScope = domainScope
                )
            )
            ScriptletValidation.Unsupported -> {
                ScriptletParseResult.Skipped(REASON_UNSUPPORTED_SCRIPTLET)
            }
            ScriptletValidation.InvalidArguments -> {
                ScriptletParseResult.Skipped(REASON_INVALID_ARGUMENTS)
            }
        }
    }

    /**
     * 函数 `validate`：封装 `validate` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param arguments 参数类型为 `List<String>`，表示函数执行 `arguments` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun validate(name: String, arguments: List<String>): ScriptletValidation {
        val normalizedName = name.trim().lowercase(Locale.US)
        val spec = SUPPORTED_SCRIPTLETS[normalizedName] ?: return ScriptletValidation.Unsupported
        if (arguments.size != spec.argumentCount) {
            return ScriptletValidation.InvalidArguments
        }
        if (!arguments.all(spec.argumentValidator)) {
            return ScriptletValidation.InvalidArguments
        }
        return ScriptletValidation.Valid
    }

    /**
     * 函数 `parseSyntax`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun parseSyntax(text: String): ScriptletSyntax? {
        findScriptletBody(text, UBO_SCRIPTLET_MARKER)?.let { return it }
        findScriptletBody(text, ADGUARD_SCRIPTLET_MARKER)?.let { return it }
        return null
    }

    /**
     * 函数 `findScriptletBody`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @param marker 参数类型为 `String`，表示函数执行 `marker` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun findScriptletBody(text: String, marker: String): ScriptletSyntax? {
        val markerIndex = text.indexOf(marker)
        if (markerIndex < 0 || !text.endsWith(")")) {
            return null
        }
        return ScriptletSyntax(
            domainText = text.substring(0, markerIndex),
            argumentsText = text.substring(
                startIndex = markerIndex + marker.length,
                endIndex = text.length - 1
            )
        )
    }

    /**
     * 函数 `splitArguments`：封装 `split Arguments` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun splitArguments(text: String): List<String>? {
        val arguments = mutableListOf<String>()
        val current = StringBuilder()
        var quote: Char? = null
        var escaped = false
        text.forEach { char ->
            when {
                escaped -> {
                    current.append(char)
                    escaped = false
                }
                char == '\\' && quote != null -> {
                    escaped = true
                }
                quote != null -> {
                    if (char == quote) {
                        quote = null
                    } else {
                        current.append(char)
                    }
                }
                char == '\'' || char == '"' -> {
                    quote = char
                }
                char == ',' -> {
                    arguments += current.toString().trim()
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        if (quote != null || escaped) {
            return null
        }
        arguments += current.toString().trim()
        return arguments.filter { argument -> argument.isNotEmpty() }
    }

    /**
     * 函数 `normalizeArgument`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param argument 参数类型为 `String`，表示函数执行 `argument` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeArgument(argument: String): String {
        return argument.trim()
    }

    /**
     * 函数 `shouldIgnoreRuleLine`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param trimmed 参数类型为 `String`，表示函数执行 `trimmed` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun shouldIgnoreRuleLine(trimmed: String): Boolean {
        return trimmed.isEmpty() ||
            trimmed.startsWith("!") ||
            trimmed.startsWith("# ") ||
            trimmed == "#"
    }

    /**
     * 函数 `isSafeKeyword`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param argument 参数类型为 `String`，表示函数执行 `argument` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isSafeKeyword(argument: String): Boolean {
        val value = argument.trim()
        if (value.length !in 3..100) {
            return false
        }
        if (value.contains("javascript:", ignoreCase = true)) {
            return false
        }
        return value.none { char ->
            char.isISOControl() || char == '<' || char == '>'
        }
    }

    private const val UBO_SCRIPTLET_MARKER = "##+js("
    private const val ADGUARD_SCRIPTLET_MARKER = "#%#//scriptlet("
    private const val RAW_SCRIPTLET_MARKER = "#%#"

    private val SUPPORTED_SCRIPTLETS = mapOf(
        HOOK_WINDOW_OPEN_BLOCK_KEYWORD to ScriptletSpec(
            argumentCount = 1,
            argumentValidator = ::isSafeKeyword
        ),
        HOOK_FETCH_BLOCK_KEYWORD to ScriptletSpec(
            argumentCount = 1,
            argumentValidator = ::isSafeKeyword
        ),
        HOOK_CLICK_SKIP_BUTTONS to ScriptletSpec(
            argumentCount = 0,
            argumentValidator = { true }
        ),
        HOOK_ENABLE_VIDEO_CONTROLS to ScriptletSpec(
            argumentCount = 0,
            argumentValidator = { true }
        )
    )
}

sealed class ScriptletParseResult {
    data class Rule(val value: ScriptletRule) : ScriptletParseResult()
    data class Skipped(val reason: String) : ScriptletParseResult()
    object Ignored : ScriptletParseResult()
}

enum class ScriptletValidation {
    Valid,
    Unsupported,
    InvalidArguments
}

private data class ScriptletSpec(
    val argumentCount: Int,
    val argumentValidator: (String) -> Boolean
)

private data class ScriptletSyntax(
    val domainText: String,
    val argumentsText: String
)
