package com.example.videobrowser.browser.search

import com.example.videobrowser.utils.HostNameNormalizer
import com.example.videobrowser.utils.UrlUtils

/**
 * Owns recognition of search result pages created by the app's configured
 * search providers.
 */
class BuiltInSearchResultPagePolicy(
    private val providers: () -> Collection<SearchProvider>
) {
    constructor(providers: Collection<SearchProvider>) : this({ providers })

    fun isBuiltInSearchResultUrl(url: String?): Boolean {
        return searchQueryFromUrl(url) != null
    }

    fun searchQueryFromUrl(url: String?): String? {
        return matchingSearchResultProvider(url)?.let { provider ->
            SearchEngineUrlTools.queryFromUrl(provider.config, url)
                ?: provider.addressBarSearchUrlPrefixes.firstNotNullOfOrNull { searchUrlPrefix ->
                    UrlUtils.searchQueryFromUrl(url?.trim().orEmpty(), searchUrlPrefix)
                }
        }
    }

    fun searchPageHideCssForUrl(url: String?): List<String> {
        val provider = matchingSearchResultProvider(url) ?: return emptyList()
        return provider.hideCss.takeIf { provider.hidePageSearchBox } ?: emptyList()
    }

    fun isSearchResultResourceUrl(pageUrl: String?, resourceUrl: String?): Boolean {
        val provider = matchingSearchResultProvider(pageUrl) ?: return false
        val requestHost = HostNameNormalizer.fromUrl(resourceUrl) ?: return false
        val pageHost = HostNameNormalizer.fromUrl(pageUrl)
        return HostNameNormalizer.matchesDomainOrSubdomain(requestHost, pageHost) ||
            provider.domains.any { domain ->
                HostNameNormalizer.matchesDomainOrSubdomain(requestHost, domain)
            }
    }

    private fun matchingSearchResultProvider(url: String?): SearchProvider? {
        val normalizedUrl = url?.trim().orEmpty()
        if (normalizedUrl.isBlank()) {
            return null
        }
        return providers().firstOrNull { provider ->
            SearchEngineUrlTools.queryFromUrl(provider.config, normalizedUrl) != null ||
                provider.addressBarSearchUrlPrefixes.any { searchUrlPrefix ->
                    UrlUtils.searchQueryFromUrl(normalizedUrl, searchUrlPrefix) != null
                }
            }
    }
}
