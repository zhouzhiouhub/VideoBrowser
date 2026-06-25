package com.example.videobrowser.browser.search

import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.Utf8UrlCodec
import com.example.videobrowser.utils.WebSchemePolicy
import java.net.URL
import java.util.Locale

internal object SearchFormTemplateAnalyzer {
    fun configFromHtml(
        pageUrl: String,
        html: String,
        name: String = ""
    ): SearchEngineConfig? {
        return FORM_REGEX.findAll(html)
            .firstNotNullOfOrNull { match ->
                val formHtml = match.value
                val formTag = formHtml.substringBefore(">", missingDelimiterValue = formHtml)
                if (!isGetForm(formTag)) {
                    return@firstNotNullOfOrNull null
                }
                val queryParam = searchInputName(formHtml) ?: return@firstNotNullOfOrNull null
                val actionUrl = formActionUrl(pageUrl, formTag) ?: return@firstNotNullOfOrNull null
                val template = appendKeywordParameter(actionUrl, queryParam) ?: return@firstNotNullOfOrNull null
                SearchEngineConfigFactory.fromTemplate(
                    name = name,
                    searchTemplate = template,
                    displayUrlFallback = pageUrl
                )
            }
    }

    private fun isGetForm(formTag: String): Boolean {
        val method = HtmlTagAttributeParser.attributeValue(formTag, "method")
            ?.lowercase(Locale.ROOT)
            ?: return true
        return method == "get"
    }

    private fun searchInputName(formHtml: String): String? {
        return HtmlTagAttributeParser.tags(formHtml, "input")
            .firstNotNullOfOrNull { tag ->
                val type = HtmlTagAttributeParser.attributeValue(tag, "type")
                    ?.lowercase(Locale.ROOT)
                    .orEmpty()
                if (type in IGNORED_INPUT_TYPES) {
                    return@firstNotNullOfOrNull null
                }
                HtmlTagAttributeParser.attributeValue(tag, "name")
                    ?.takeIf { name -> name.lowercase(Locale.ROOT) in SEARCH_QUERY_PARAMS }
            }
    }

    private fun formActionUrl(pageUrl: String, formTag: String): String? {
        val action = HtmlTagAttributeParser.attributeValue(formTag, "action")
            ?.takeIf { value -> value.isNotBlank() }
            ?: pageUrl
        val resolved = runCatching { URL(URL(pageUrl), action).toString() }.getOrNull()
            ?: return null
        val uri = SafeUriParser.parse(resolved) ?: return null
        return resolved.takeIf { WebSchemePolicy.isHttpOrHttpsScheme(uri.scheme) }
    }

    private fun appendKeywordParameter(actionUrl: String, queryParam: String): String? {
        val uri = SafeUriParser.parse(actionUrl) ?: return null
        val rawFragment = uri.rawFragment?.let { fragment -> "#$fragment" }.orEmpty()
        val baseWithoutFragment = actionUrl.substringBefore("#")
        val separator = when {
            baseWithoutFragment.endsWith("?") || baseWithoutFragment.endsWith("&") -> ""
            baseWithoutFragment.contains("?") -> "&"
            else -> "?"
        }
        val encodedName = Utf8UrlCodec.encodeFormComponent(queryParam)
        return "$baseWithoutFragment$separator$encodedName={keyword}$rawFragment"
    }

    private val FORM_REGEX = Regex(
        "<form\\b[^>]*>.*?</form>",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    private val SEARCH_QUERY_PARAMS = setOf(
        "q",
        "wd",
        "word",
        "query",
        "keyword",
        "search_query",
        "text"
    )
    private val IGNORED_INPUT_TYPES = setOf("hidden", "submit", "button", "reset", "image")
}
