package com.example.videobrowser.adblock

import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleEngine

/**
 * 内置请求级规则源。P6 后命中判断统一交给 RuleEngine。
 */
object BuiltInAdBlockRules {
    fun requestRules(): List<Rule> {
        return REQUEST_RULES
    }

    fun matches(url: String, host: String?): Boolean {
        return RULE_ENGINE.matchRequest(url = url, host = host).shouldBlock
    }

    private val BLOCKED_HOST_SUFFIXES = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "googleads.g.doubleclick.net",
        "ads.youtube.com",
        "ad.iqiyi.com",
        "imasdk.googleapis.com",
        "adnxs.com",
        "adsystem.com",
        "amazon-adsystem.com",
        "criteo.com",
        "criteo.net",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "adsrvr.org",
        "scorecardresearch.com",
        "moatads.com",
        "adform.net",
        "yieldmo.com",
        "bidswitch.net",
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
        "/gampad/ads",
        "/pubads_impl",
        "/adsystem/",
        "/admanager/",
        "/ad_creative/",
        "/ad_track/",
        "/ad_event/",
        "/ad_status",
        "/ad_policy",
        "/adx/",
        "/ad.m3u8",
        "/ima3.js",
        "/vast",
        "/vmap",
        "/preroll",
        "/midroll"
    )

    private val REQUEST_RULES = BLOCKED_HOST_SUFFIXES.map { host ->
        Rule.blockDomainContains(
            domain = host,
            id = "built-in:block:domain:$host"
        )
    } + BLOCKED_URL_KEYWORDS.map { keyword ->
        Rule.blockUrlContains(
            pattern = keyword,
            id = "built-in:block:url:$keyword"
        )
    }

    private val RULE_ENGINE = RuleEngine(REQUEST_RULES)
}
