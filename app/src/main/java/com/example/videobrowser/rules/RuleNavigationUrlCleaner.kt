package com.example.videobrowser.rules

import com.example.videobrowser.browser.ResourceType
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.WebSchemePolicy
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal object RuleNavigationUrlCleaner {
    fun clean(
        url: String,
        pageUrl: String?,
        removeParamCapabilities: List<RuleCapability.RemoveParam>,
        ruleMatcher: RuleMatcher
    ): String {
        // 导航清理只处理 http/https URL，其他 scheme 不应该被当成普通网页参数改写。
        val uri = SafeUriParser.parse(url) ?: return url
        if (!WebSchemePolicy.isHttpOrHttpsScheme(uri.scheme)) {
            return url
        }
        val rawQuery = uri.rawQuery ?: return url
        if (rawQuery.isEmpty()) {
            return url
        }

        val requestHost = SiteHost.fromUrl(url)
        val pageHost = SiteHost.fromUrl(pageUrl)
        val parametersToRemove = removeParamCapabilities
            .filter { capability ->
                ruleMatcher.matches(
                    rule = capability.rule.toRequestMatcherRule(),
                    url = url,
                    host = requestHost,
                    pageHost = pageHost,
                    resourceType = ResourceType.DOCUMENT
                )
            }
            .map { capability -> capability.rule.parameterName }
            .toSet()
        if (parametersToRemove.isEmpty()) {
            return url
        }

        val queryParts = rawQuery.split("&")
        val keptQueryParts = queryParts.filterNot { part ->
            decodedQueryName(part.substringBefore("=")) in parametersToRemove
        }
        if (keptQueryParts.size == queryParts.size) {
            return url
        }
        return renderUriWithQuery(uri, keptQueryParts.joinToString("&").takeIf { it.isNotEmpty() })
    }

    private fun decodedQueryName(rawName: String): String {
        return runCatching {
            URLDecoder.decode(rawName, StandardCharsets.UTF_8.name())
        }.getOrDefault(rawName)
    }

    private fun renderUriWithQuery(uri: URI, rawQuery: String?): String {
        return buildString {
            append(uri.scheme)
            append(":")
            uri.rawAuthority?.let { authority ->
                append("//")
                append(authority)
            }
            append(uri.rawPath.orEmpty())
            rawQuery?.let { query ->
                append("?")
                append(query)
            }
            uri.rawFragment?.let { fragment ->
                append("#")
                append(fragment)
            }
        }
    }
}
