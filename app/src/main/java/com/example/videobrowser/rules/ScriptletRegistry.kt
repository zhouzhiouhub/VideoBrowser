package com.example.videobrowser.rules

import com.example.videobrowser.site.SiteHost
import java.util.Locale

object ScriptletRegistry {
    const val REASON_UNSUPPORTED_SYNTAX = "unsupported scriptlet syntax"
    const val REASON_UNSUPPORTED_SCRIPTLET = "unsupported scriptlet"
    const val REASON_INVALID_ARGUMENTS = "invalid scriptlet arguments"
    const val REASON_INVALID_DOMAIN = "invalid scriptlet domain"
    const val REASON_RAW_SCRIPTLET_JAVASCRIPT = "raw scriptlet javascript not allowed"

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
        val domainScope = parseDomains(syntax.domainText)
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

    private fun parseSyntax(text: String): ScriptletSyntax? {
        findScriptletBody(text, UBO_SCRIPTLET_MARKER)?.let { return it }
        findScriptletBody(text, ADGUARD_SCRIPTLET_MARKER)?.let { return it }
        return null
    }

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

    private fun normalizeArgument(argument: String): String {
        return argument.trim()
    }

    private fun parseDomains(value: String): DomainScope? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return DomainScope.Empty
        }

        val included = mutableSetOf<String>()
        val excluded = mutableSetOf<String>()
        trimmed.split(",")
            .map { domain -> domain.trim() }
            .filter { domain -> domain.isNotEmpty() }
            .forEach { rawDomain ->
                val isExcluded = rawDomain.startsWith("~")
                val normalized = SiteHost.normalize(rawDomain.removePrefix("~")) ?: return null
                if (!isValidDomain(normalized)) {
                    return null
                }
                if (isExcluded) {
                    excluded += normalized
                } else {
                    included += normalized
                }
            }
        return DomainScope(
            includedDomains = included,
            excludedDomains = excluded
        )
    }

    private fun isValidDomain(domain: String): Boolean {
        return domain.isNotBlank() &&
            domain.all { char -> char.isLetterOrDigit() || char == '-' || char == '.' }
    }

    private fun shouldIgnoreRuleLine(trimmed: String): Boolean {
        return trimmed.isEmpty() ||
            trimmed.startsWith("!") ||
            trimmed.startsWith("# ") ||
            trimmed == "#"
    }

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
        "window-open-block-keyword" to ScriptletSpec(
            argumentCount = 1,
            argumentValidator = ::isSafeKeyword
        ),
        "fetch-block-keyword" to ScriptletSpec(
            argumentCount = 1,
            argumentValidator = ::isSafeKeyword
        ),
        "click-skip-buttons" to ScriptletSpec(
            argumentCount = 0,
            argumentValidator = { true }
        ),
        "enable-video-controls" to ScriptletSpec(
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
