package com.example.videobrowser.browser.search

internal object BuiltInSearchProviders {
    val defaults: List<SearchProvider> = listOf(
        fromConfig(
            id = "sogou",
            badge = "搜",
            homeUrl = "https://m.sogou.com/",
            config = SearchEngineConfig(
                name = "搜狗",
                displayUrl = "https://www.sogou.com",
                searchTemplate = "https://www.sogou.com/web?query={keyword}",
                queryParam = "query",
                domains = listOf("www.sogou.com", "m.sogou.com", "sogou.com"),
                resultPathRules = listOf("/web", "/web/searchList.jsp"),
                hideCss = listOf(
                    "form[action*=\"/web\"]",
                    "[role=\"search\"]"
                ),
                hidePageSearchBox = true
            ),
            addressBarSearchUrlPrefixes = listOf(
                "https://www.sogou.com/web?query=",
                "https://m.sogou.com/web/searchList.jsp?s_from=pcsearch&keyword="
            ),
            accentColor = 0xFF13B56B.toInt()
        ),
        fromConfig(
            id = "so",
            badge = "360",
            homeUrl = "https://m.so.com/",
            config = SearchEngineConfig(
                name = "360搜索",
                displayUrl = "https://m.so.com",
                searchTemplate = "https://m.so.com/s?q={keyword}",
                queryParam = "q",
                domains = listOf("m.so.com", "www.so.com", "so.com"),
                resultPathRules = listOf("/s"),
                hideCss = listOf(
                    "form[action*=\"/s\"]",
                    "[role=\"search\"]",
                    ".search-form"
                ),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF20A052.toInt()
        ),
        fromConfig(
            id = "quark",
            badge = "夸",
            homeUrl = "https://quark.sm.cn/",
            config = SearchEngineConfig(
                name = "夸克搜索",
                displayUrl = "https://quark.sm.cn",
                searchTemplate = "https://quark.sm.cn/s?q={keyword}",
                queryParam = "q",
                domains = listOf("quark.sm.cn"),
                resultPathRules = listOf("/s"),
                hideCss = listOf("form[action*=\"/s\"]", "[role=\"search\"]"),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF2F6FED.toInt()
        ),
        fromConfig(
            id = "uc",
            badge = "UC",
            homeUrl = "https://so.m.sm.cn/",
            config = SearchEngineConfig(
                name = "UC",
                displayUrl = "https://so.m.sm.cn",
                searchTemplate = "https://so.m.sm.cn/s?q={keyword}",
                queryParam = "q",
                domains = listOf("so.m.sm.cn"),
                resultPathRules = listOf("/s"),
                hideCss = listOf("form[action*=\"/s\"]", "[role=\"search\"]"),
                hidePageSearchBox = true
            ),
            accentColor = 0xFFF28C20.toInt()
        ),
        fromConfig(
            id = "baidu",
            badge = "百",
            homeUrl = "https://m.baidu.com/",
            config = SearchEngineConfig(
                name = "百度",
                displayUrl = "https://m.baidu.com",
                searchTemplate = "https://m.baidu.com/s?word={keyword}",
                queryParam = "word",
                domains = listOf("m.baidu.com"),
                resultPathRules = listOf("/s"),
                hideCss = listOf(
                    "form[action*=\"/s\"]",
                    "[role=\"search\"]",
                    "#index-form"
                ),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF315EFB.toInt()
        ),
        fromConfig(
            id = "baidu_desktop",
            badge = "百",
            homeUrl = "https://www.baidu.com/",
            config = SearchEngineConfig(
                name = "百度桌面端",
                displayUrl = "https://www.baidu.com",
                searchTemplate = "https://www.baidu.com/s?wd={keyword}",
                queryParam = "wd",
                domains = listOf("www.baidu.com", "baidu.com"),
                resultPathRules = listOf("/s"),
                hideCss = listOf("form[action*=\"/s\"]", "[role=\"search\"]"),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF315EFB.toInt()
        ),
        fromConfig(
            id = "edge",
            badge = "B",
            homeUrl = "https://www.bing.com/",
            config = SearchEngineConfig(
                name = "必应",
                displayUrl = "https://www.bing.com",
                searchTemplate = "https://www.bing.com/search?q={keyword}",
                queryParam = "q",
                domains = listOf("www.bing.com", "bing.com"),
                resultPathRules = listOf("/search"),
                hideCss = listOf("form[action*=\"/search\"]", "[role=\"search\"]"),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF12837A.toInt()
        ),
        fromConfig(
            id = "google",
            badge = "G",
            homeUrl = "https://www.google.com/",
            config = SearchEngineConfig(
                name = "Google",
                displayUrl = "https://www.google.com",
                searchTemplate = "https://www.google.com/search?q={keyword}",
                queryParam = "q",
                domains = listOf("www.google.com", "google.com"),
                resultPathRules = listOf("/search"),
                hideCss = listOf("form[action*=\"/search\"]", "[role=\"search\"]"),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF4285F4.toInt()
        ),
        fromConfig(
            id = "douyin",
            badge = "抖",
            homeUrl = "https://so.douyin.com/",
            config = SearchEngineConfig(
                name = "抖音搜索",
                displayUrl = "https://so.douyin.com",
                searchTemplate = "https://so.douyin.com/s?keyword={keyword}",
                queryParam = "keyword",
                domains = listOf("so.douyin.com"),
                resultPathRules = listOf("/s"),
                hideCss = listOf("form[action*=\"/s\"]", "[role=\"search\"]"),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF111111.toInt()
        ),
        fromConfig(
            id = "duckduckgo",
            badge = "D",
            homeUrl = "https://duckduckgo.com/",
            config = SearchEngineConfig(
                name = "DuckDuckGo",
                displayUrl = "https://duckduckgo.com",
                searchTemplate = "https://duckduckgo.com/?q={keyword}",
                queryParam = "q",
                domains = listOf("duckduckgo.com", "www.duckduckgo.com"),
                resultPathRules = listOf("/"),
                hideCss = listOf("form[action=\"/\"]", "[role=\"search\"]"),
                hidePageSearchBox = true
            ),
            accentColor = 0xFFDE5833.toInt()
        ),
        fromConfig(
            id = "bilibili",
            badge = "B",
            homeUrl = "https://search.bilibili.com/",
            config = SearchEngineConfig(
                name = "B站搜索",
                displayUrl = "https://search.bilibili.com",
                searchTemplate = "https://search.bilibili.com/all?keyword={keyword}",
                queryParam = "keyword",
                domains = listOf("search.bilibili.com"),
                resultPathRules = listOf("/all"),
                hideCss = listOf("form[action*=\"/all\"]", "[role=\"search\"]"),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF00A1D6.toInt()
        ),
        fromConfig(
            id = "zhihu",
            badge = "知",
            homeUrl = "https://www.zhihu.com/",
            config = SearchEngineConfig(
                name = "知乎搜索",
                displayUrl = "https://www.zhihu.com",
                searchTemplate = "https://www.zhihu.com/search?type=content&q={keyword}",
                queryParam = "q",
                domains = listOf("www.zhihu.com", "zhihu.com"),
                resultPathRules = listOf("/search"),
                hideCss = listOf("form[action*=\"/search\"]", "[role=\"search\"]"),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF1772F6.toInt()
        ),
        fromConfig(
            id = "weibo",
            badge = "微",
            homeUrl = "https://s.weibo.com/",
            config = SearchEngineConfig(
                name = "微博搜索",
                displayUrl = "https://s.weibo.com",
                searchTemplate = "https://s.weibo.com/weibo?q={keyword}",
                queryParam = "q",
                domains = listOf("s.weibo.com"),
                resultPathRules = listOf("/weibo"),
                hideCss = listOf("form[action*=\"/weibo\"]", "[role=\"search\"]"),
                hidePageSearchBox = true
            ),
            accentColor = 0xFFE6162D.toInt()
        )
    )

    private fun fromConfig(
        id: String,
        badge: String,
        homeUrl: String,
        config: SearchEngineConfig,
        addressBarSearchUrlPrefixes: List<String> = listOf(
            config.searchTemplate.substringBefore("{keyword}")
        ),
        accentColor: Int
    ): SearchProvider {
        return SearchProvider(
            id = id,
            name = config.name,
            badge = badge,
            homeUrl = homeUrl,
            searchUrlPrefix = config.searchTemplate.substringBefore("{keyword}"),
            displayUrl = config.displayUrl,
            searchTemplate = config.searchTemplate,
            queryParam = config.queryParam,
            domains = config.domains,
            resultPathRules = config.resultPathRules,
            hideCss = config.hideCss,
            hidePageSearchBox = config.hidePageSearchBox,
            addressBarSearchUrlPrefixes = addressBarSearchUrlPrefixes,
            extraJs = config.extraJs,
            enabled = config.enabled,
            accentColor = accentColor
        )
    }
}
