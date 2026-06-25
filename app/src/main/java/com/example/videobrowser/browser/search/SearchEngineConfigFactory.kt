package com.example.videobrowser.browser.search

import com.example.videobrowser.utils.WebUrlNormalizer

internal object SearchEngineConfigFactory {
    fun fromTemplate(
        name: String,
        searchTemplate: String,
        displayUrlFallback: String? = null,
        hideCss: List<String> = emptyList(),
        hidePageSearchBox: Boolean = false,
        extraJs: String? = null,
        enabled: Boolean = true
    ): SearchEngineConfig? {
        val normalizedTemplate = SearchEngineUrlTools.normalizeTemplate(searchTemplate) ?: return null
        val queryParam = SearchEngineUrlTools.queryParamFromTemplate(normalizedTemplate) ?: return null
        val domains = SearchEngineUrlTools.domainsFromTemplate(normalizedTemplate)
        if (domains.isEmpty()) {
            return null
        }
        val displayUrl = displayUrlFallback
            ?.trim()
            ?.trimEnd('/')
            ?.takeIf(WebUrlNormalizer::isHttpOrHttpsUrl)
            ?: SearchEngineUrlTools.displayUrlFromTemplate(normalizedTemplate)
            ?: return null

        return SearchEngineConfig(
            name = name.trim(),
            displayUrl = displayUrl,
            searchTemplate = normalizedTemplate,
            queryParam = queryParam,
            domains = domains,
            resultPathRules = SearchEngineUrlTools.resultPathRulesFromTemplate(normalizedTemplate),
            hideCss = hideCss,
            hidePageSearchBox = hidePageSearchBox,
            extraJs = extraJs?.trim()?.takeIf { value -> value.isNotEmpty() },
            enabled = enabled
        )
    }
}
