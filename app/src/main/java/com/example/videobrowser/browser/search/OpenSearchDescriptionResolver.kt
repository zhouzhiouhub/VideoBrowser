package com.example.videobrowser.browser.search

import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.WebSchemePolicy
import java.net.URL
import java.util.Locale

internal object OpenSearchDescriptionResolver {
    fun descriptionUrlFromHtml(pageUrl: String, html: String): String? {
        return HtmlTagAttributeParser.tags(html, "link")
            .firstNotNullOfOrNull { tag ->
                val relTokens = HtmlTagAttributeParser.relTokens(tag)
                val type = HtmlTagAttributeParser.attributeValue(tag, "type")
                    ?.lowercase(Locale.ROOT)
                val href = HtmlTagAttributeParser.attributeValue(tag, "href")
                if (
                    "search" in relTokens &&
                    type == OPENSEARCH_DESCRIPTION_TYPE &&
                    !href.isNullOrBlank()
                ) {
                    resolveHttpUrl(pageUrl, href)
                } else {
                    null
                }
            }
    }

    fun configFromXml(xml: String, displayUrlFallback: String? = null): SearchEngineConfig? {
        val template = HtmlTagAttributeParser.tags(xml, "Url")
            .firstNotNullOfOrNull { tag ->
                val type = HtmlTagAttributeParser.attributeValue(tag, "type")
                    ?.lowercase(Locale.ROOT)
                val templateValue = HtmlTagAttributeParser.attributeValue(tag, "template")
                if (type == "text/html") templateValue else null
            }
            ?: return null
        return SearchEngineConfigFactory.fromTemplate(
            name = shortNameFromXml(xml).orEmpty(),
            searchTemplate = template,
            displayUrlFallback = displayUrlFallback
        )
    }

    private fun shortNameFromXml(xml: String): String? {
        val match = SHORT_NAME_REGEX.find(xml) ?: return null
        return htmlText(match.groups[1]?.value.orEmpty())
            .trim()
            .takeIf { value -> value.isNotEmpty() }
    }

    private fun htmlText(value: String): String {
        return value
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
    }

    private fun resolveHttpUrl(pageUrl: String, href: String): String? {
        val resolved = runCatching { URL(URL(pageUrl), href).toString() }.getOrNull()
            ?: return null
        val uri = SafeUriParser.parse(resolved) ?: return null
        return resolved.takeIf { WebSchemePolicy.isHttpOrHttpsScheme(uri.scheme) }
    }

    private const val OPENSEARCH_DESCRIPTION_TYPE =
        "application/opensearchdescription+xml"
    private val SHORT_NAME_REGEX = Regex(
        "<ShortName\\b[^>]*>(.*?)</ShortName>",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
}
