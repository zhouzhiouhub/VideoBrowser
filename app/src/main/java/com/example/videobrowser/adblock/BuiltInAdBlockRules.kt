package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 BuiltInAdBlockRules 可以拆开理解为“Built In Ad Block Rules”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleEngine

/**
 * 内置请求级规则源。P6 后命中判断统一交给 RuleEngine。
 */
object BuiltInAdBlockRules {
    /**
     * 函数 `requestRules`：处理 `request Rules` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun requestRules(): List<Rule> {
        return REQUEST_RULES
    }

    /**
     * 函数 `matches`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
