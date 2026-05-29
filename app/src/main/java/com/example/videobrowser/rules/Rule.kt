package com.example.videobrowser.rules

import java.util.Locale

/**
 * 请求级规则模型。P6 只承接 URL 包含、域名边界匹配和简单白名单。
 */
data class Rule(
    val id: String,
    val pattern: String,
    val type: RuleType,
    val action: RuleAction,
    val source: String = SOURCE_BUILT_IN,
    val domainScope: DomainScope = DomainScope.Empty,
    val thirdParty: Boolean? = null
) {
    init {
        require(id.isNotBlank()) { "Rule id must not be blank." }
        require(pattern.trim().isNotEmpty()) { "Rule pattern must not be blank." }
        require(action != RuleAction.NONE) { "Rule action must be ALLOW or BLOCK." }
    }

    val normalizedPattern: String = pattern.trim().lowercase(Locale.US)
    val normalizedPatternRegex: Regex? = if (type == RuleType.URL_PATTERN) {
        buildUrlPatternRegex(pattern)
    } else {
        null
    }

    companion object {
        const val SOURCE_BUILT_IN = "built-in"

        fun blockUrlContains(
            pattern: String,
            id: String = "block:url:$pattern",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return requestRule(
                id = id,
                pattern = pattern,
                type = RuleType.URL_CONTAINS,
                action = RuleAction.BLOCK,
                source = source
            )
        }

        fun blockDomainContains(
            domain: String,
            id: String = "block:domain:$domain",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return requestRule(
                id = id,
                pattern = domain,
                type = RuleType.DOMAIN_CONTAINS,
                action = RuleAction.BLOCK,
                source = source
            )
        }

        fun allowUrlContains(
            pattern: String,
            id: String = "allow:url:$pattern",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return requestRule(
                id = id,
                pattern = pattern,
                type = RuleType.URL_CONTAINS,
                action = RuleAction.ALLOW,
                source = source
            )
        }

        fun allowDomainContains(
            domain: String,
            id: String = "allow:domain:$domain",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return requestRule(
                id = id,
                pattern = domain,
                type = RuleType.DOMAIN_CONTAINS,
                action = RuleAction.ALLOW,
                source = source
            )
        }

        fun fromRequestRuleText(
            text: String,
            id: String? = null,
            source: String = SOURCE_BUILT_IN
        ): Rule? {
            val trimmed = text.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("!") || trimmed.startsWith("#")) {
                return null
            }

            val action = if (trimmed.startsWith("@@")) {
                RuleAction.ALLOW
            } else {
                RuleAction.BLOCK
            }
            val rawBody = if (action == RuleAction.ALLOW) {
                trimmed.removePrefix("@@").trim()
            } else {
                trimmed
            }
            if (rawBody.isBlank()) {
                return null
            }

            val (body, optionsText) = splitRequestOptions(rawBody)
            if (body.isBlank() || hasUnsupportedRequestSyntax(body)) {
                return null
            }
            val options = parseRequestOptions(optionsText) ?: return null

            val type = requestRuleTypeFor(body)
            val domainPattern = parsePureDomainRule(body)
            val pattern = domainPattern ?: body
            val generatedId = id ?: "${action.name.lowercase(Locale.US)}:" +
                "${type.name.lowercase(Locale.US)}:$pattern"
            return requestRule(
                id = generatedId,
                pattern = pattern,
                type = type,
                action = action,
                source = source,
                domainScope = options.domainScope,
                thirdParty = options.thirdParty
            )
        }

        private fun requestRule(
            id: String,
            pattern: String,
            type: RuleType,
            action: RuleAction,
            source: String,
            domainScope: DomainScope = DomainScope.Empty,
            thirdParty: Boolean? = null
        ): Rule {
            val normalizedPattern = when (type) {
                RuleType.URL_CONTAINS -> pattern.trim()
                RuleType.URL_PATTERN -> pattern.trim()
                RuleType.DOMAIN_CONTAINS -> pattern.trim().trim('.')
            }
            return Rule(
                id = id,
                pattern = normalizedPattern,
                type = type,
                action = action,
                source = source,
                domainScope = domainScope,
                thirdParty = thirdParty
            )
        }

        private fun requestRuleTypeFor(text: String): RuleType {
            parsePureDomainRule(text)?.let {
                return RuleType.DOMAIN_CONTAINS
            }
            return if (hasPatternSyntax(text)) {
                RuleType.URL_PATTERN
            } else {
                RuleType.URL_CONTAINS
            }
        }

        private fun parsePureDomainRule(text: String): String? {
            if (!text.startsWith("||") || !text.endsWith("^")) {
                return null
            }

            val domain = text
                .removePrefix("||")
                .removeSuffix("^")
                .trim()
                .trim('.')
            if (domain.isBlank()) {
                return null
            }

            val hasOnlyDomainCharacters = domain.all { char ->
                char.isLetterOrDigit() || char == '-' || char == '.'
            }
            return domain.takeIf { hasOnlyDomainCharacters }
        }

        private fun hasUnsupportedRequestSyntax(text: String): Boolean {
            return text.contains("##") ||
                text.contains("#@#") ||
                text.contains("#%#")
        }

        private fun hasPatternSyntax(text: String): Boolean {
            return text.contains('*') ||
                text.contains('^') ||
                text.startsWith("|") ||
                text.endsWith("|")
        }

        private fun splitRequestOptions(text: String): Pair<String, String?> {
            val optionsIndex = text.indexOf('$')
            if (optionsIndex < 0) {
                return text.trim() to null
            }
            return text.substring(0, optionsIndex).trim() to
                text.substring(optionsIndex + 1).trim().takeIf { it.isNotEmpty() }
        }

        private fun parseRequestOptions(text: String?): RequestRuleOptions? {
            if (text.isNullOrBlank()) {
                return RequestRuleOptions()
            }

            var domainScope = DomainScope.Empty
            var thirdParty: Boolean? = null
            text.split(',')
                .map { option -> option.trim() }
                .filter { option -> option.isNotEmpty() }
                .forEach { option ->
                    val lower = option.lowercase(Locale.US)
                    when {
                        lower == "third-party" || lower == "3p" -> thirdParty = true
                        lower == "~third-party" || lower == "1p" || lower == "first-party" -> {
                            thirdParty = false
                        }
                        lower.startsWith("domain=") -> {
                            domainScope = parseOptionDomainScope(option.substringAfter("="))
                                ?: return null
                        }
                        lower in IGNORED_RESOURCE_TYPE_OPTIONS -> Unit
                        else -> return null
                    }
                }
            return RequestRuleOptions(domainScope = domainScope, thirdParty = thirdParty)
        }

        private fun parseOptionDomainScope(text: String): DomainScope? {
            val included = mutableSetOf<String>()
            val excluded = mutableSetOf<String>()
            text.split('|')
                .map { domain -> domain.trim() }
                .filter { domain -> domain.isNotEmpty() }
                .forEach { rawDomain ->
                    val isExcluded = rawDomain.startsWith("~")
                    val normalized = rawDomain
                        .removePrefix("~")
                        .trim()
                        .trim('.')
                        .lowercase(Locale.US)
                    if (!isValidDomainPattern(normalized)) {
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

        private fun isValidDomainPattern(domain: String): Boolean {
            return domain.isNotBlank() &&
                domain.all { char -> char.isLetterOrDigit() || char == '-' || char == '.' }
        }

        private fun buildUrlPatternRegex(pattern: String): Regex? {
            val source = pattern.trim().lowercase(Locale.US)
            if (source.isEmpty()) {
                return null
            }

            val builder = StringBuilder()
            var index = 0
            if (source.startsWith("||")) {
                builder.append("^[a-z][a-z0-9+.-]*://(?:[^/?#]+\\.)?")
                index = 2
            } else if (source.startsWith("|")) {
                builder.append("^")
                index = 1
            }

            val endAnchored = source.endsWith("|") && source.length > index
            val end = if (endAnchored) source.length - 1 else source.length
            while (index < end) {
                when (val char = source[index]) {
                    '*' -> builder.append(".*")
                    '^' -> builder.append("(?:[^A-Za-z0-9_\\-.%]|$)")
                    else -> builder.append(Regex.escape(char.toString()))
                }
                index += 1
            }
            if (endAnchored) {
                builder.append("$")
            }

            return runCatching { Regex(builder.toString(), RegexOption.IGNORE_CASE) }.getOrNull()
        }

        private val IGNORED_RESOURCE_TYPE_OPTIONS = setOf(
            "script",
            "image",
            "stylesheet",
            "media",
            "font",
            "object",
            "subdocument",
            "frame",
            "xmlhttprequest",
            "xhr",
            "ping",
            "beacon",
            "other"
        )
    }
}

enum class RuleType {
    URL_CONTAINS,
    URL_PATTERN,
    DOMAIN_CONTAINS
}

enum class RuleAction {
    ALLOW,
    BLOCK,
    NONE
}

private data class RequestRuleOptions(
    val domainScope: DomainScope = DomainScope.Empty,
    val thirdParty: Boolean? = null
)
