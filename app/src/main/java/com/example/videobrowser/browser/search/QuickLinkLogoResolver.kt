package com.example.videobrowser.browser.search

import com.example.videobrowser.utils.HostNameNormalizer
import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.WebSchemePolicy
import java.net.URL
import java.util.Locale

object QuickLinkLogoResolver {
    fun cacheKey(pageUrl: String): String? {
        return originFor(pageUrl)
    }

    fun logoUrlsFor(pageUrl: String): List<String> {
        val origin = originFor(pageUrl) ?: return emptyList()
        return listOf(
            "$origin/apple-touch-icon.png",
            "$origin/apple-touch-icon-precomposed.png",
            "$origin/favicon.png",
            "$origin/favicon.ico"
        )
    }

    fun logoUrlsFromHtml(pageUrl: String, html: String): List<String> {
        return HtmlTagAttributeParser.tags(html, "link")
            .filter(::isIconLinkTag)
            .mapNotNull { tag -> HtmlTagAttributeParser.attributeValue(tag, "href") }
            .mapNotNull { href -> resolveUrl(pageUrl, href) }
            .distinct()
            .toList()
    }

    fun fallbackBadgeText(name: String): String {
        return name.trim().take(2).ifBlank { "+" }
    }

    private fun isIconLinkTag(tag: String): Boolean {
        return HtmlTagAttributeParser.relTokens(tag)
            .any { value -> value == "icon" || value == "shortcut" || value == "apple-touch-icon" }
    }

    private fun resolveUrl(pageUrl: String, href: String): String? {
        val resolved = runCatching { URL(URL(pageUrl), href).toString() }.getOrNull()
            ?: return null
        val uri = SafeUriParser.parse(resolved) ?: return null
        return resolved.takeIf { WebSchemePolicy.isHttpOrHttpsScheme(uri.scheme) }
    }

    private fun originFor(pageUrl: String): String? {
        val uri = SafeUriParser.parse(pageUrl) ?: return null
        if (!WebSchemePolicy.isHttpOrHttpsScheme(uri.scheme)) {
            return null
        }
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
        val host = HostNameNormalizer.normalize(uri.host) ?: return null
        val port = uri.port.takeIf { value -> value >= 0 }?.let { value -> ":$value" }.orEmpty()
        return "$scheme://$host$port"
    }
}
