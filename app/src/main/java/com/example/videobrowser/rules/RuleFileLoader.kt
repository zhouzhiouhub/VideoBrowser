package com.example.videobrowser.rules

import android.content.res.AssetManager
import com.example.videobrowser.site.SiteHost
import java.io.File
import java.io.InputStream
import java.util.Locale

class RuleFileLoader(
    private val openAsset: (String) -> InputStream?,
    private val cacheDirectory: File? = null
) {
    fun loadRequestRules(): RuleLoadResult<Rule> {
        return loadRules(
            assetPath = REQUEST_RULES_ASSET,
            cacheFileName = REQUEST_RULES_CACHE_FILE,
            parser = ::parseRequestRule
        )
    }

    fun loadCssRules(): RuleLoadResult<ElementRule> {
        return loadRules(
            assetPath = CSS_RULES_ASSET,
            cacheFileName = CSS_RULES_CACHE_FILE,
            parser = { line, source, lineNumber ->
                parseCssRule(line, source, lineNumber)
            }
        )
    }

    fun loadDomRules(): RuleLoadResult<ElementRule> {
        return loadRules(
            assetPath = DOM_RULES_ASSET,
            cacheFileName = DOM_RULES_CACHE_FILE,
            parser = { line, source, lineNumber ->
                parseDomRule(line, source, lineNumber)
            }
        )
    }

    fun loadScriptletRules(): RuleLoadResult<ScriptletRule> {
        return loadRules(
            assetPath = SCRIPTLET_RULES_ASSET,
            cacheFileName = SCRIPTLET_RULES_CACHE_FILE,
            parser = { line, source, lineNumber ->
                parseScriptletRule(line, source, lineNumber)
            }
        )
    }

    private fun <T> loadRules(
        assetPath: String,
        cacheFileName: String,
        parser: (String, String, Int) -> ParsedRule<T>
    ): RuleLoadResult<T> {
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
            source = "cache:$cacheFileName",
            streamProvider = { openCacheFile(cacheFileName) },
            parser = parser,
            rules = rules,
            skippedRules = skippedRules
        )

        return RuleLoadResult(rules = rules, skippedRules = skippedRules)
    }

    private fun <T> readRules(
        source: String,
        streamProvider: () -> InputStream?,
        parser: (String, String, Int) -> ParsedRule<T>,
        rules: MutableList<T>,
        skippedRules: MutableList<SkippedRule>
    ) {
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

    private fun openCacheFile(fileName: String): InputStream? {
        return cacheDirectory
            ?.resolve(fileName)
            ?.takeIf { file -> file.isFile }
            ?.inputStream()
    }

    private fun parseRequestRule(line: String, source: String, lineNumber: Int): ParsedRule<Rule> {
        if (shouldIgnoreRuleLine(line)) {
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

    private fun parseCssRule(line: String, source: String, lineNumber: Int): ParsedRule<ElementRule> {
        if (shouldIgnoreRuleLine(line)) {
            return ParsedRule.Ignored
        }
        val trimmed = line.trim()
        if (isScriptletRuleLine(trimmed)) {
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

        val domainScope = parseDomains(trimmed.substring(0, markerIndex)) ?: return ParsedRule.Skipped(
            skipped(source, lineNumber, line, "invalid css rule domain")
        )
        val selector = trimmed.substring(markerIndex + markerLength).trim()
        if (!isSafeSelector(selector)) {
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

    private fun parseScriptletRule(
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

    private fun parseDomRule(line: String, source: String, lineNumber: Int): ParsedRule<ElementRule> {
        if (shouldIgnoreRuleLine(line)) {
            return ParsedRule.Ignored
        }
        val trimmed = line.trim()
        if (!trimmed.startsWith(DOM_REMOVE_PREFIX, ignoreCase = true)) {
            return ParsedRule.Skipped(skipped(source, lineNumber, line, "missing dom remove prefix"))
        }
        val selector = trimmed.substring(DOM_REMOVE_PREFIX.length).trim()
        if (!isSafeSelector(selector)) {
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

    private fun shouldIgnoreRuleLine(line: String): Boolean {
        val trimmed = line.trim()
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

    private fun skipped(source: String, lineNumber: Int, text: String, reason: String): SkippedRule {
        return SkippedRule(
            source = source,
            lineNumber = lineNumber,
            text = text.trim(),
            reason = reason
        )
    }

    companion object {
        const val REQUEST_RULES_ASSET = "rules/request_rules.txt"
        const val CSS_RULES_ASSET = "rules/css_rules.txt"
        const val DOM_RULES_ASSET = "rules/dom_rules.txt"
        const val SCRIPTLET_RULES_ASSET = "rules/scriptlet_rules.txt"
        const val REQUEST_RULES_CACHE_FILE = "request_rules.txt"
        const val CSS_RULES_CACHE_FILE = "css_rules.txt"
        const val DOM_RULES_CACHE_FILE = "dom_rules.txt"
        const val SCRIPTLET_RULES_CACHE_FILE = "scriptlet_rules.txt"
        private const val DOM_REMOVE_PREFIX = "remove:"
        private const val MAX_SELECTOR_LENGTH = 200

        private val UNSUPPORTED_SELECTOR_TOKENS = listOf(
            ":has(",
            ":contains(",
            ":matches(",
            ":xpath(",
            "javascript:",
            "expression("
        )

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

private sealed class ParsedRule<out T> {
    data class Rule<T>(val value: T) : ParsedRule<T>()
    data class Skipped(val skippedRule: SkippedRule) : ParsedRule<Nothing>()
    object Ignored : ParsedRule<Nothing>()
}
