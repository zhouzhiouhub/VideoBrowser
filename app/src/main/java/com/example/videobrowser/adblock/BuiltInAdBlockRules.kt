package com.example.videobrowser.adblock

/**
 * P5 阶段内置 URL 黑名单，只做轻量域名和 URL 片段匹配。
 */
object BuiltInAdBlockRules {
    fun matches(url: String, host: String?): Boolean {
        val normalizedUrl = url.trim().lowercase()
        if (normalizedUrl.isEmpty()) {
            return false
        }

        val normalizedHost = host.orEmpty()
            .trim()
            .trim('.')
            .lowercase()
        return matchesHost(normalizedHost) || matchesUrl(normalizedUrl)
    }

    private fun matchesHost(host: String): Boolean {
        if (host.isEmpty()) {
            return false
        }

        return BLOCKED_HOST_SUFFIXES.any { blockedHost ->
            host == blockedHost || host.endsWith(".$blockedHost")
        }
    }

    private fun matchesUrl(url: String): Boolean {
        return BLOCKED_URL_KEYWORDS.any { url.contains(it) }
    }

    private val BLOCKED_HOST_SUFFIXES = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "googleads.g.doubleclick.net",
        "ads.youtube.com",
        "ad.iqiyi.com",
        "adnxs.com",
        "adsystem.com",
        "taboola.com",
        "outbrain.com",
        "ads-twitter.com",
        "analytics.yahoo.com"
    )

    private val BLOCKED_URL_KEYWORDS = listOf(
        "/pagead/",
        "/advertisement/",
        "/adservice/",
        "/adserver/",
        "/advert/",
        "/ads/",
        "/adx/",
        "/ad.m3u8",
        "/vast",
        "/vmap",
        "/preroll",
        "/midroll"
    )
}
