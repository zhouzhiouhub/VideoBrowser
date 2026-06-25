package com.example.videobrowser.browser.search

import com.example.videobrowser.utils.SafeUriParser
import java.util.Locale

object CustomSearchEngineInputResolver {
    fun resolve(
        input: String,
        knownProviders: Collection<SearchProvider> = SearchProviders.defaults
    ): SearchEngineConfig? {
        val normalizedInput = input.trim()
        if (normalizedInput.isEmpty()) {
            return null
        }
        knownProviders.firstOrNull { provider ->
            provider.matchesDisplayOrHomeUrl(normalizedInput)
        }?.let { provider -> return provider.config }

        return SearchEngineConfigFactory.fromTemplate(
            name = "",
            searchTemplate = normalizedInput,
            hidePageSearchBox = false
        )
    }

    private fun SearchProvider.matchesDisplayOrHomeUrl(input: String): Boolean {
        val normalizedInput = canonicalPageUrl(input) ?: return false
        return normalizedInput == canonicalPageUrl(displayUrl) ||
            normalizedInput == canonicalPageUrl(homeUrl)
    }

    private fun canonicalPageUrl(value: String): String? {
        val uri = SafeUriParser.parse(value) ?: return null
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
        if (scheme != "http" && scheme != "https") {
            return null
        }
        val host = uri.host?.trim()?.trimEnd('.')?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        val path = uri.rawPath.orEmpty().ifEmpty { "/" }.trimEnd('/').ifEmpty { "/" }
        val port = uri.port.takeIf { it >= 0 }?.let { ":$it" }.orEmpty()
        return "$scheme://$host$port$path"
    }
}
