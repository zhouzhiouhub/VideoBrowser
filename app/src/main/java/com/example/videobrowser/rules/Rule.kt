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
    val source: String = SOURCE_BUILT_IN
) {
    init {
        require(id.isNotBlank()) { "Rule id must not be blank." }
        require(pattern.trim().isNotEmpty()) { "Rule pattern must not be blank." }
        require(action != RuleAction.NONE) { "Rule action must be ALLOW or BLOCK." }
    }

    val normalizedPattern: String = pattern.trim().lowercase(Locale.US)

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
            val body = if (action == RuleAction.ALLOW) {
                trimmed.removePrefix("@@").trim()
            } else {
                trimmed
            }
            if (body.isBlank() || hasUnsupportedRequestSyntax(body)) {
                return null
            }

            val domainPattern = parsePureDomainRule(body)
            val type = if (domainPattern != null) {
                RuleType.DOMAIN_CONTAINS
            } else {
                RuleType.URL_CONTAINS
            }
            val pattern = domainPattern ?: body
            val generatedId = id ?: "${action.name.lowercase(Locale.US)}:" +
                "${type.name.lowercase(Locale.US)}:$pattern"
            return requestRule(
                id = generatedId,
                pattern = pattern,
                type = type,
                action = action,
                source = source
            )
        }

        private fun requestRule(
            id: String,
            pattern: String,
            type: RuleType,
            action: RuleAction,
            source: String
        ): Rule {
            val normalizedPattern = when (type) {
                RuleType.URL_CONTAINS -> pattern.trim()
                RuleType.DOMAIN_CONTAINS -> pattern.trim().trim('.')
            }
            return Rule(
                id = id,
                pattern = normalizedPattern,
                type = type,
                action = action,
                source = source
            )
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
            return text.contains('$') ||
                text.contains("##") ||
                text.contains("#@#") ||
                text.contains("#%#") ||
                text.contains("*")
        }
    }
}

enum class RuleType {
    URL_CONTAINS,
    DOMAIN_CONTAINS
}

enum class RuleAction {
    ALLOW,
    BLOCK,
    NONE
}
