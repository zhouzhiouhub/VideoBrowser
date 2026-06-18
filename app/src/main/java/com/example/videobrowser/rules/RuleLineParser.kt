package com.example.videobrowser.rules

import com.example.videobrowser.adguard.AdGuardRuleParser

internal class RuleLineParser(
    private val adGuardRuleParser: AdGuardRuleParser = AdGuardRuleParser()
) {
    fun parseRequestRule(line: String, source: String, lineNumber: Int): ParsedRule<Rule> {
        if (RuleLinePolicy.shouldIgnore(line)) {
            return ParsedRule.Ignored
        }
        val rule = Rule.fromRequestRuleText(
            text = line,
            id = "$source:$lineNumber",
            source = source
        )
        return if (rule != null) {
            ParsedRule.Rule(rule)
        } else {
            ParsedRule.Skipped(skipped(source, lineNumber, line, "unsupported request rule syntax"))
        }
    }

    fun parseCssRule(line: String, source: String, lineNumber: Int): ParsedRule<ElementRule> {
        if (RuleLinePolicy.shouldIgnore(line)) {
            return ParsedRule.Ignored
        }
        val trimmed = line.trim()
        if (RuleLinePolicy.isScriptletRuleLine(trimmed)) {
            return ParsedRule.Ignored
        }
        if (trimmed.contains("#?#")) {
            return ParsedRule.Skipped(skipped(source, lineNumber, line, "unsupported css exception syntax"))
        }

        val exceptionMarkerIndex = trimmed.indexOf("#@#")
        val hideMarkerIndex = trimmed.indexOf("##")
        val isException = exceptionMarkerIndex >= 0
        val markerIndex = if (isException) exceptionMarkerIndex else hideMarkerIndex
        val markerLength = if (isException) 3 else 2
        if (markerIndex < 0) {
            return ParsedRule.Skipped(skipped(source, lineNumber, line, "missing css rule marker"))
        }

        val domainScope = DomainScopeParser.parseCommaSeparated(
            trimmed.substring(0, markerIndex)
        ) ?: return ParsedRule.Skipped(
            skipped(source, lineNumber, line, "invalid css rule domain")
        )
        val selector = trimmed.substring(markerIndex + markerLength).trim()
        if (!RuleLinePolicy.isSafeSelector(selector)) {
            return ParsedRule.Skipped(skipped(source, lineNumber, line, "unsupported css selector"))
        }

        return ParsedRule.Rule(
            ElementRule(
                id = "$source:$lineNumber",
                selector = selector,
                type = if (isException) ElementRuleType.CSS_UNHIDE else ElementRuleType.CSS_HIDE,
                source = source,
                domains = domainScope.includedDomains,
                excludedDomains = domainScope.excludedDomains
            )
        )
    }

    fun parseScriptletRule(
        line: String,
        source: String,
        lineNumber: Int
    ): ParsedRule<ScriptletRule> {
        return when (val result = ScriptletRegistry.parse(line, "$source:$lineNumber", source)) {
            ScriptletParseResult.Ignored -> ParsedRule.Ignored
            is ScriptletParseResult.Rule -> ParsedRule.Rule(result.value)
            is ScriptletParseResult.Skipped -> {
                ParsedRule.Skipped(skipped(source, lineNumber, line, result.reason))
            }
        }
    }

    fun parseRemoveParamRule(
        line: String,
        source: String,
        lineNumber: Int
    ): ParsedRule<RemoveParamRule> {
        if (RuleLinePolicy.shouldIgnore(line)) {
            return ParsedRule.Ignored
        }
        val rule = adGuardRuleParser.parseRemoveParamRule(line, "$source:$lineNumber", source)
        return if (rule != null) {
            ParsedRule.Rule(rule)
        } else {
            ParsedRule.Skipped(skipped(source, lineNumber, line, "unsupported removeparam rule syntax"))
        }
    }

    fun parseDomRule(line: String, source: String, lineNumber: Int): ParsedRule<ElementRule> {
        if (RuleLinePolicy.shouldIgnore(line)) {
            return ParsedRule.Ignored
        }
        val trimmed = line.trim()
        if (!trimmed.startsWith(DOM_REMOVE_PREFIX, ignoreCase = true)) {
            return ParsedRule.Skipped(skipped(source, lineNumber, line, "missing dom remove prefix"))
        }
        val selector = trimmed.substring(DOM_REMOVE_PREFIX.length).trim()
        if (!RuleLinePolicy.isSafeSelector(selector)) {
            return ParsedRule.Skipped(skipped(source, lineNumber, line, "unsupported dom selector"))
        }

        return ParsedRule.Rule(
            ElementRule(
                id = "$source:$lineNumber",
                selector = selector,
                type = ElementRuleType.DOM_REMOVE,
                source = source
            )
        )
    }

    private fun skipped(source: String, lineNumber: Int, text: String, reason: String): SkippedRule {
        return SkippedRule(
            source = source,
            lineNumber = lineNumber,
            text = text.trim(),
            reason = reason
        )
    }

    private companion object {
        const val DOM_REMOVE_PREFIX = "remove:"
    }
}
