package com.example.videobrowser.browser.search

import java.util.Locale

internal object HtmlTagAttributeParser {
    fun tags(html: String, tagName: String): Sequence<String> {
        val safeTagName = tagName.trim().takeIf { value ->
            value.matches(TAG_NAME_REGEX)
        } ?: return emptySequence()
        return Regex("<$safeTagName\\b[^>]*>", RegexOption.IGNORE_CASE)
            .findAll(html)
            .map { match -> match.value }
    }

    fun attributeValue(tag: String, name: String): String? {
        val safeName = name.trim().takeIf { value ->
            value.matches(ATTRIBUTE_NAME_REGEX)
        } ?: return null
        val pattern = Regex(
            "\\b${Regex.escape(safeName)}\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'=<>`]+))",
            RegexOption.IGNORE_CASE
        )
        val match = pattern.find(tag) ?: return null
        return (match.groups[1]?.value ?: match.groups[2]?.value ?: match.groups[3]?.value)
            ?.trim()
            ?.takeIf { value -> value.isNotEmpty() }
    }

    fun relTokens(tag: String): Set<String> {
        return attributeValue(tag, "rel")
            ?.lowercase(Locale.ROOT)
            ?.split(Regex("\\s+"))
            ?.filter { value -> value.isNotBlank() }
            ?.toSet()
            .orEmpty()
    }

    private val TAG_NAME_REGEX = Regex("[A-Za-z][A-Za-z0-9:-]*")
    private val ATTRIBUTE_NAME_REGEX = Regex("[A-Za-z_:][A-Za-z0-9_.:-]*")
}
