package com.example.videobrowser.adguard

import com.example.videobrowser.rules.DomainScope
import com.example.videobrowser.rules.ElementRule
import com.example.videobrowser.rules.ElementRuleType
import com.example.videobrowser.rules.RemoveParamRule
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.ScriptletParseResult
import com.example.videobrowser.rules.ScriptletRegistry
import com.example.videobrowser.rules.ScriptletRule
import com.example.videobrowser.rules.SkippedRule
import com.example.videobrowser.site.SiteHost
import java.util.Locale

class AdGuardRuleParser {
    fun parseSubscription(text: String, source: String): AdGuardParseResult {
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

    fun parseRequestRule(text: String, id: String, source: String): Rule? {
        return Rule.fromRequestRuleText(text = text, id = id, source = source)
    }

    fun parseRemoveParamRule(text: String, id: String, source: String): RemoveParamRule? {
        return RemoveParamRule.fromAdGuardRuleText(text = text, id = id, source = source)
    }

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

        val domainScope = parseDomains(trimmed.substring(0, markerIndex)) ?: return null
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

    fun parseScriptletRule(text: String, id: String, source: String): ScriptletRule? {
        return when (val result = ScriptletRegistry.parse(text, id, source)) {
            is ScriptletParseResult.Rule -> result.value
            else -> null
        }
    }

    private fun parseLine(line: String, source: String, lineNumber: Int): ParsedAdGuardLine {
        val trimmed = line.trim()
        if (isIgnored(trimmed)) {
            return ParsedAdGuardLine.Ignored
        }

        val id = "$source:$lineNumber"
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

    private fun isIgnored(trimmed: String): Boolean {
        return trimmed.isEmpty() ||
            trimmed.startsWith("!") ||
            trimmed.startsWith("# ") ||
            trimmed == "#"
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
        if (included.isEmpty() && excluded.isEmpty()) {
            return null
        }
        return DomainScope(
            includedDomains = included,
            excludedDomains = excluded
        )
    }

    private fun isValidDomain(domain: String): Boolean {
        return domain.all { char -> char.isLetterOrDigit() || char == '-' || char == '.' }
    }

    private fun isSafeSelector(selector: String): Boolean {
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
