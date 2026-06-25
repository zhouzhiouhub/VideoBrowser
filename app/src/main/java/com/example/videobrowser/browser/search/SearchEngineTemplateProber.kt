package com.example.videobrowser.browser.search

import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.WebUrlNormalizer

enum class SearchEngineTemplateProbeSource {
    OPENSEARCH,
    FORM
}

data class SearchEngineTemplateProbeResult(
    val config: SearchEngineConfig,
    val source: SearchEngineTemplateProbeSource
)

/**
 * Coordinates automatic custom search engine template discovery.
 *
 * It first follows OpenSearch metadata from the home page, then falls back to
 * static GET form analysis. Call it off the UI thread when using the default fetcher.
 */
class SearchEngineTemplateProber(
    private val fetchText: (url: String, maxBytes: Int) -> String?
) {
    constructor(fetcher: SearchEngineProbeFetcher = SearchEngineProbeFetcher()) : this(fetcher::fetchText)

    fun probe(homeUrl: String, name: String = ""): SearchEngineTemplateProbeResult? {
        val normalizedHomeUrl = normalizeProbeUrl(homeUrl) ?: return null
        val html = fetchText(normalizedHomeUrl, MAX_HOME_HTML_BYTES) ?: return null
        val openSearchResult = probeOpenSearch(normalizedHomeUrl, html, name)
        if (openSearchResult != null) {
            return openSearchResult
        }
        return SearchFormTemplateAnalyzer.configFromHtml(
            pageUrl = normalizedHomeUrl,
            html = html,
            name = name
        )?.let { config ->
            SearchEngineTemplateProbeResult(
                config = config,
                source = SearchEngineTemplateProbeSource.FORM
            )
        }
    }

    private fun probeOpenSearch(
        homeUrl: String,
        html: String,
        name: String
    ): SearchEngineTemplateProbeResult? {
        val descriptionUrl = OpenSearchDescriptionResolver.descriptionUrlFromHtml(homeUrl, html)
            ?: return null
        val xml = fetchText(descriptionUrl, MAX_OPENSEARCH_XML_BYTES) ?: return null
        val config = OpenSearchDescriptionResolver.configFromXml(
            xml = xml,
            displayUrlFallback = homeUrl
        ) ?: return null
        return SearchEngineTemplateProbeResult(
            config = config.copy(name = config.name.ifBlank { name.trim() }),
            source = SearchEngineTemplateProbeSource.OPENSEARCH
        )
    }

    companion object {
        const val MAX_HOME_HTML_BYTES = 512 * 1024
        const val MAX_OPENSEARCH_XML_BYTES = 128 * 1024

        fun normalizeProbeUrl(url: String): String? {
            val normalized = WebUrlNormalizer.normalizeHttpOrHttpsUrl(url) ?: return null
            val uri = SafeUriParser.parse(normalized) ?: return null
            if (!uri.userInfo.isNullOrBlank()) {
                return null
            }
            return normalized
        }
    }
}
