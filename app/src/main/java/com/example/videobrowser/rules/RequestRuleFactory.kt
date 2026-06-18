package com.example.videobrowser.rules

import com.example.videobrowser.browser.ResourceType
import java.util.Locale

internal object RequestRuleFactory {
    fun blockUrlContains(
        pattern: String,
        id: String,
        source: String
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
        id: String,
        source: String
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
        id: String,
        source: String
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
        id: String,
        source: String
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
        id: String?,
        source: String
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
        if (action == RuleAction.ALLOW && options.redirectResourceName != null) {
            return null
        }
        val generatedId = id ?: "${action.name.lowercase(Locale.US)}:" +
            "${type.name.lowercase(Locale.US)}:$pattern"
        return requestRule(
            id = generatedId,
            pattern = pattern,
            type = type,
            action = action,
            source = source,
            domainScope = options.domainScope,
            thirdParty = options.thirdParty,
            resourceTypes = options.resourceTypes,
            redirectResourceName = options.redirectResourceName
        )
    }

    fun buildUrlPatternRegex(pattern: String): Regex? {
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

    private fun requestRule(
        id: String,
        pattern: String,
        type: RuleType,
        action: RuleAction,
        source: String,
        domainScope: DomainScope = DomainScope.Empty,
        thirdParty: Boolean? = null,
        resourceTypes: Set<ResourceType> = emptySet(),
        redirectResourceName: String? = null
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
            thirdParty = thirdParty,
            resourceTypes = resourceTypes,
            redirectResourceName = redirectResourceName
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
        val resourceTypes = mutableSetOf<ResourceType>()
        var redirectResourceName: String? = null
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
                    lower.startsWith("redirect=") -> {
                        redirectResourceName = parseRedirectResourceName(option.substringAfter("="))
                            ?: return null
                    }
                    RESOURCE_TYPE_OPTIONS.containsKey(lower) -> {
                        resourceTypes.addAll(RESOURCE_TYPE_OPTIONS.getValue(lower))
                    }
                    lower.startsWith("~") &&
                        RESOURCE_TYPE_OPTIONS.containsKey(lower.removePrefix("~")) -> {
                        return null
                    }
                    else -> return null
                }
            }
        return RequestRuleOptions(
            domainScope = domainScope,
            thirdParty = thirdParty,
            resourceTypes = resourceTypes.toSet(),
            redirectResourceName = redirectResourceName
        )
    }

    private fun parseRedirectResourceName(text: String): String? {
        val normalized = text.trim().lowercase(Locale.US)
        return normalized.takeIf { resourceName ->
            resourceName in SUPPORTED_REDIRECT_RESOURCES
        }
    }

    private fun parseOptionDomainScope(text: String): DomainScope? {
        return DomainScopeParser.parsePipeSeparated(text)
    }

    private val RESOURCE_TYPE_OPTIONS = mapOf(
        "document" to setOf(ResourceType.DOCUMENT),
        "script" to setOf(ResourceType.SCRIPT),
        "image" to setOf(ResourceType.IMAGE),
        "stylesheet" to setOf(ResourceType.STYLESHEET),
        "css" to setOf(ResourceType.STYLESHEET),
        "media" to setOf(ResourceType.MEDIA),
        "font" to setOf(ResourceType.FONT),
        "object" to setOf(ResourceType.OTHER),
        "subdocument" to setOf(ResourceType.DOCUMENT),
        "frame" to setOf(ResourceType.DOCUMENT),
        "xmlhttprequest" to setOf(ResourceType.XHR, ResourceType.FETCH),
        "xhr" to setOf(ResourceType.XHR, ResourceType.FETCH),
        "fetch" to setOf(ResourceType.FETCH),
        "ping" to setOf(ResourceType.FETCH),
        "beacon" to setOf(ResourceType.FETCH),
        "other" to setOf(ResourceType.OTHER)
    )

    private val SUPPORTED_REDIRECT_RESOURCES = setOf(
        "noopjs",
        "noopcss",
        "nooptext"
    )
}

private data class RequestRuleOptions(
    val domainScope: DomainScope = DomainScope.Empty,
    val thirdParty: Boolean? = null,
    val resourceTypes: Set<ResourceType> = emptySet(),
    val redirectResourceName: String? = null
)
