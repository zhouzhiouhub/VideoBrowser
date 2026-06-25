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
    val resultPathRules: List<String> = emptyList(),
    val hideCss: List<String> = emptyList(),
    val hidePageSearchBox: Boolean = false,
    val addressBarSearchUrlPrefixes: List<String> = listOf(searchUrlPrefix),
    val extraJs: String? = null,
    val enabled: Boolean = true,
    val accentColor: Int
) {
    val config: SearchEngineConfig
        get() = SearchEngineConfig(
            name = name,
            displayUrl = displayUrl,
            searchTemplate = searchTemplate,
            queryParam = queryParam,
            domains = domains,
            resultPathRules = resultPathRules,
            hideCss = hideCss,
            hidePageSearchBox = hidePageSearchBox,
            extraJs = extraJs,
            enabled = enabled
        )

    fun searchUrlFor(keyword: String): String? {
        return SearchEngineUrlTools.buildSearchUrl(config, keyword)
    }
}

object SearchProviders {
    const val DEFAULT_PROVIDER_ID = "baidu"

    val defaults: List<SearchProvider> = BuiltInSearchProviders.defaults

    fun all(
        customSearchEngines: List<CustomSearchEngine>,
        removedProviderIds: Set<String> = emptySet()
    ): List<SearchProvider> {
        val visibleDefaults = defaults.filter { provider ->
            provider.enabled && provider.id !in removedProviderIds
        }
        val customProviders = customSearchEngines
            .map(::fromCustomSearchEngine)
            .filter { provider -> provider.enabled }
        val providers = visibleDefaults + customProviders
        return providers.ifEmpty {
            defaults.firstOrNull { provider -> provider.id == DEFAULT_PROVIDER_ID }
                ?.let { provider -> listOf(provider) }
                ?: defaults.take(1)
        }
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
            resultPathRules = engine.resultPathRules,
            hideCss = engine.hideCss,
            hidePageSearchBox = engine.hidePageSearchBox,
            extraJs = engine.extraJs,
            enabled = engine.enabled,
            accentColor = 0xFF5F6F7D.toInt()
        )
    }

}
