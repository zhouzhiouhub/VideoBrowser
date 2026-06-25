package com.example.videobrowser.browser.search

sealed class CustomSearchEngineInputAnalysis {
    data class Resolved(val config: SearchEngineConfig) : CustomSearchEngineInputAnalysis()
    data class ProbeRequired(val homeUrl: String) : CustomSearchEngineInputAnalysis()
    object Invalid : CustomSearchEngineInputAnalysis()
}

object CustomSearchEngineInputAnalyzer {
    fun analyze(
        input: String,
        knownProviders: Collection<SearchProvider> = SearchProviders.defaults
    ): CustomSearchEngineInputAnalysis {
        CustomSearchEngineInputResolver.resolve(input, knownProviders)?.let { config ->
            return CustomSearchEngineInputAnalysis.Resolved(config)
        }
        val homeUrl = SearchEngineTemplateProber.normalizeProbeUrl(input)
        return if (homeUrl == null) {
            CustomSearchEngineInputAnalysis.Invalid
        } else {
            CustomSearchEngineInputAnalysis.ProbeRequired(homeUrl)
        }
    }
}
