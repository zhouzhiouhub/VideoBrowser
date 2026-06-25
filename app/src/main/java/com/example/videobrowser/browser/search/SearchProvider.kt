package com.example.videobrowser.browser.search

/**
 * 初学者阅读提示：
 * 这个文件属于“搜索入口与地址建议模块”。
 * 文件名 SearchProvider 可以拆开理解为“Search Provider”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：把地址栏输入、默认搜索引擎、远程搜索建议、收藏和历史候选项整理成用户可以点击的建议列表。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import com.example.videobrowser.settings.CustomSearchEngine
import java.util.Locale

data class SearchProvider(
    val id: String,
    val name: String,
    val badge: String,
    val homeUrl: String,
    val searchUrlPrefix: String,
    val displayUrl: String = homeUrl.trimEnd('/'),
    val searchTemplate: String = SearchEngineUrlTools.templateFromPrefix(searchUrlPrefix)
        ?: searchUrlPrefix,
    val queryParam: String = SearchEngineUrlTools.queryParamFromTemplate(searchTemplate).orEmpty(),
    val domains: List<String> = SearchEngineUrlTools.domainsFromTemplate(searchTemplate),
    val hideCss: List<String> = emptyList(),
    val hidePageSearchBox: Boolean = false,
    val addressBarSearchUrlPrefixes: List<String> = listOf(searchUrlPrefix),
    val accentColor: Int
) {
    val config: SearchEngineConfig
        get() = SearchEngineConfig(
            name = name,
            displayUrl = displayUrl,
            searchTemplate = searchTemplate,
            queryParam = queryParam,
            domains = domains,
            hideCss = hideCss,
            hidePageSearchBox = hidePageSearchBox
        )

    fun searchUrlFor(keyword: String): String? {
        return SearchEngineUrlTools.buildSearchUrl(config, keyword)
    }
}

object SearchProviders {
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
                hideCss = listOf(
                    "form[action*=\"/s\"]",
                    "[id*=\"search\"]"
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
                domains = listOf("m.baidu.com", "www.baidu.com", "baidu.com"),
                hideCss = listOf(
                    "form[action*=\"/s\"]",
                    "[role=\"search\"]"
                ),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF315EFB.toInt()
        ),
        fromConfig(
            id = "edge",
            badge = "B",
            homeUrl = "https://www.bing.com/",
            config = SearchEngineConfig(
                name = "Bing",
                displayUrl = "https://www.bing.com",
                searchTemplate = "https://www.bing.com/search?q={keyword}",
                queryParam = "q",
                domains = listOf("www.bing.com", "bing.com"),
                hideCss = listOf(
                    "form[action*=\"/search\"]",
                    "[role=\"search\"]"
                ),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF12837A.toInt()
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
                hideCss = listOf(
                    "form[action*=\"/s\"]",
                    "[role=\"search\"]"
                ),
                hidePageSearchBox = true
            ),
            accentColor = 0xFF12837A.toInt()
        )
    )

    fun all(customSearchEngines: List<CustomSearchEngine>): List<SearchProvider> {
        return defaults + customSearchEngines.map(::fromCustomSearchEngine)
    }

    private fun fromCustomSearchEngine(engine: CustomSearchEngine): SearchProvider {
        val searchTemplate = SearchEngineUrlTools.normalizeTemplate(engine.searchTemplate)
            ?: SearchEngineUrlTools.templateFromPrefix(engine.searchUrlPrefix)
            ?: engine.searchTemplate
        val queryParam = engine.queryParam.ifBlank {
            SearchEngineUrlTools.queryParamFromTemplate(searchTemplate).orEmpty()
        }
        val domains = engine.domains.ifEmpty {
            SearchEngineUrlTools.domainsFromTemplate(searchTemplate)
        }
        return SearchProvider(
            id = engine.id,
            name = engine.name,
            badge = engine.name.take(1).uppercase(Locale.getDefault()),
            homeUrl = engine.displayUrl,
            searchUrlPrefix = searchTemplate.substringBefore("{keyword}"),
            displayUrl = engine.displayUrl,
            searchTemplate = searchTemplate,
            queryParam = queryParam,
            domains = domains,
            hideCss = engine.hideCss,
            hidePageSearchBox = engine.hidePageSearchBox,
            accentColor = 0xFF5F6F7D.toInt()
        )
    }

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
            hideCss = config.hideCss,
            hidePageSearchBox = config.hidePageSearchBox,
            addressBarSearchUrlPrefixes = addressBarSearchUrlPrefixes,
            accentColor = accentColor
        )
    }
}
