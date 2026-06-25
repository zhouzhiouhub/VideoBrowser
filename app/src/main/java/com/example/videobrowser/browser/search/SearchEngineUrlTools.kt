package com.example.videobrowser.browser.search

import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.TextWhitespaceNormalizer
import com.example.videobrowser.utils.Utf8UrlCodec
import java.net.URI
import java.util.Locale

object SearchEngineUrlTools {
    private val placeholders = listOf("{keyword}", "%s", "{searchTerms}")
    private val commonQueryParams = listOf(
        "q",
        "wd",
        "word",
        "query",
        "keyword",
        "search_query",
        "text"
    )

    fun normalizeTemplate(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val placeholder = placeholders.firstOrNull { token -> trimmed.contains(token) }
            ?: return null
        val uri = SafeUriParser.parse(trimmed.replace(placeholder, TEMPLATE_PARSE_VALUE))
            ?: return null
        if (!isHttpUriWithHost(uri)) {
            return null
        }
        return trimmed.replace(placeholder, "{keyword}")
    }

    fun buildSearchUrl(config: SearchEngineConfig, keyword: String): String? {
        return buildSearchUrl(config.searchTemplate, keyword)
    }

    fun buildSearchUrl(searchTemplate: String, keyword: String): String? {
        val query = TextWhitespaceNormalizer.collapse(keyword)
        if (query.isEmpty()) {
            return null
        }
        val encodedQuery = Utf8UrlCodec.encodeFormComponent(query)
        val normalizedTemplate = normalizeTemplate(searchTemplate)
            ?: templateFromPrefix(searchTemplate)
            ?: return null
        return normalizedTemplate.replace("{keyword}", encodedQuery)
    }

    fun queryFromUrl(config: SearchEngineConfig, url: String?): String? {
        if (!config.enabled) {
            return null
        }
        val currentUri = SafeUriParser.parse(url) ?: return null
        val templateUri = parseTemplateUri(config.searchTemplate) ?: return null
        if (!isHttpUriWithHost(currentUri) || !isHttpUriWithHost(templateUri)) {
            return null
        }
        if (!config.matchesHost(currentUri.host)) {
            return null
        }
        if (!config.matchesResultPath(currentUri, templateUri)) {
            return null
        }
        return config.queryParameterCandidates()
            .firstNotNullOfOrNull { queryParam ->
                rawQueryParameter(currentUri.rawQuery, queryParam)
                    ?.let(Utf8UrlCodec::decodeFormComponent)
                    ?.let(TextWhitespaceNormalizer::collapse)
                    ?.takeIf { it.isNotEmpty() }
            }
    }

    fun queryParamFromTemplate(searchTemplate: String): String? {
        val uri = parseTemplateUri(searchTemplate) ?: return null
        return uri.rawQuery
            ?.split("&")
            ?.firstNotNullOfOrNull { part ->
                val rawName = part.substringBefore("=")
                val rawValue = part.substringAfter("=", missingDelimiterValue = "")
                val decodedName = Utf8UrlCodec.decodeFormComponent(rawName)
                if (rawValue == TEMPLATE_PARSE_VALUE) decodedName else null
            }
            ?.takeIf { it.isNotBlank() }
    }

    fun domainsFromTemplate(searchTemplate: String): List<String> {
        val uri = parseTemplateUri(searchTemplate) ?: return emptyList()
        return listOfNotNull(uri.host?.normalizedHost())
    }

    fun displayUrlFromTemplate(searchTemplate: String): String? {
        val uri = parseTemplateUri(searchTemplate) ?: return null
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
        val host = uri.host?.normalizedHost() ?: return null
        return "$scheme://$host"
    }

    fun resultPathRulesFromTemplate(searchTemplate: String): List<String> {
        val uri = parseTemplateUri(searchTemplate) ?: return emptyList()
        return listOf(normalizedPath(uri))
    }

    fun templateFromPrefix(searchUrlPrefix: String): String? {
        val trimmed = searchUrlPrefix.trim()
        if (placeholders.any { token -> trimmed.contains(token) }) {
            return normalizeTemplate(trimmed)
        }
        val uri = SafeUriParser.parse(trimmed) ?: return null
        if (!isHttpUriWithHost(uri)) {
            return null
        }
        return "$trimmed{keyword}"
    }

    private fun SearchEngineConfig.matchesHost(host: String?): Boolean {
        val normalizedHost = host.normalizedHost() ?: return false
        return domains.any { domain ->
            val normalizedDomain = domain.normalizedHost() ?: return@any false
            normalizedHost == normalizedDomain ||
                normalizedHost.endsWith(".$normalizedDomain")
        }
    }

    private fun SearchEngineConfig.matchesResultPath(
        currentUri: URI,
        templateUri: URI
    ): Boolean {
        val currentPath = normalizedPath(currentUri)
        val configuredPaths = resultPathRules
            .mapNotNull { pathRule -> normalizeConfiguredPath(pathRule) }
            .distinct()
        return if (configuredPaths.isEmpty()) {
            currentPath == normalizedPath(templateUri)
        } else {
            currentPath in configuredPaths
        }
    }

    private fun SearchEngineConfig.queryParameterCandidates(): List<String> {
        return (listOf(queryParam) + commonQueryParams)
            .map { value -> value.trim() }
            .filter { value -> value.isNotEmpty() }
            .distinct()
    }

    private fun rawQueryParameter(rawQuery: String?, queryParameterName: String): String? {
        if (queryParameterName.isBlank()) {
            return null
        }
        return rawQuery
            ?.split("&")
            ?.firstNotNullOfOrNull { part ->
                val rawName = part.substringBefore("=")
                val decodedName = Utf8UrlCodec.decodeFormComponent(rawName)
                if (decodedName == queryParameterName && part.contains("=")) {
                    part.substringAfter("=")
                } else {
                    null
                }
            }
    }

    private fun isHttpUriWithHost(uri: URI): Boolean {
        return (uri.scheme.equals("http", ignoreCase = true) ||
            uri.scheme.equals("https", ignoreCase = true)) &&
            !uri.host.isNullOrBlank()
    }

    private fun parseTemplateUri(searchTemplate: String): URI? {
        val normalizedTemplate = normalizeTemplate(searchTemplate) ?: return null
        return SafeUriParser.parse(normalizedTemplate.replace("{keyword}", TEMPLATE_PARSE_VALUE))
    }

    private fun normalizedPath(uri: URI): String {
        return uri.rawPath.orEmpty().ifEmpty { "/" }.trimEnd('/').ifEmpty { "/" }
    }

    private fun normalizeConfiguredPath(pathRule: String): String? {
        val trimmed = pathRule.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        return trimmed
            .substringBefore("?")
            .substringBefore("#")
            .let { path -> if (path.startsWith("/")) path else "/$path" }
            .trimEnd('/')
            .ifEmpty { "/" }
    }

    private fun String?.normalizedHost(): String? {
        return this
            ?.trim()
            ?.trimEnd('.')
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotEmpty() }
    }

    private const val TEMPLATE_PARSE_VALUE = "videobrowser_keyword"
}
