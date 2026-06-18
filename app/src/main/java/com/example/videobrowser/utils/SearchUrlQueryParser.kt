package com.example.videobrowser.utils

import java.net.URI

internal object SearchUrlQueryParser {
    fun searchQueryFromUrl(url: String, searchUrlPrefix: String): String? {
        val prefixUri = SafeUriParser.parse(searchUrlPrefix) ?: return null
        val currentUri = SafeUriParser.parse(url) ?: return null
        if (!isSameSearchEndpoint(currentUri, prefixUri)) {
            return null
        }

        val queryParameterName = searchQueryParameterName(prefixUri) ?: return null
        val rawValue = rawQueryParameter(currentUri.rawQuery, queryParameterName) ?: return null
        return Utf8UrlCodec.decodeFormComponent(rawValue)
            ?.replace(WHITESPACE_SEQUENCE, " ")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun isSameSearchEndpoint(currentUri: URI, prefixUri: URI): Boolean {
        return currentUri.scheme.equals(prefixUri.scheme, ignoreCase = true) &&
            currentUri.host.equals(prefixUri.host, ignoreCase = true) &&
            normalizedUriPath(currentUri) == normalizedUriPath(prefixUri)
    }

    private fun normalizedUriPath(uri: URI): String {
        return uri.rawPath.orEmpty().ifEmpty { "/" }.trimEnd('/')
    }

    private fun searchQueryParameterName(prefixUri: URI): String? {
        val rawQuery = prefixUri.rawQuery ?: return null
        return rawQuery
            .split("&")
            .lastOrNull()
            ?.substringBefore("=")
            ?.takeIf { it.isNotBlank() }
            ?.let { Utf8UrlCodec.decodeFormComponent(it) }
    }

    private fun rawQueryParameter(rawQuery: String?, queryParameterName: String): String? {
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

    private val WHITESPACE_SEQUENCE = Regex("\\s+")
}
