package com.example.videobrowser.browser.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomSearchEngineInputAnalyzerTest {
    @Test
    fun analyze_knownHomesResolveWithoutNetworkProbe() {
        val analysis = CustomSearchEngineInputAnalyzer.analyze("https://quark.sm.cn")

        assertTrue(analysis is CustomSearchEngineInputAnalysis.Resolved)
        assertEquals(
            "https://quark.sm.cn/s?q={keyword}",
            (analysis as CustomSearchEngineInputAnalysis.Resolved).config.searchTemplate
        )
    }

    @Test
    fun analyze_advancedTemplatesResolveImmediately() {
        val analysis = CustomSearchEngineInputAnalyzer.analyze("https://example.com/search?q=%s")

        assertTrue(analysis is CustomSearchEngineInputAnalysis.Resolved)
        assertEquals(
            "https://example.com/search?q={keyword}",
            (analysis as CustomSearchEngineInputAnalysis.Resolved).config.searchTemplate
        )
    }

    @Test
    fun analyze_unknownHttpHomesRequireProbe() {
        val analysis = CustomSearchEngineInputAnalyzer.analyze("https://example.com")

        assertTrue(analysis is CustomSearchEngineInputAnalysis.ProbeRequired)
        assertEquals(
            "https://example.com",
            (analysis as CustomSearchEngineInputAnalysis.ProbeRequired).homeUrl
        )
    }

    @Test
    fun analyze_invalidInputsRequireManualAdvancedTemplate() {
        assertTrue(
            CustomSearchEngineInputAnalyzer.analyze("ftp://example.com/search?q=%s") is
                CustomSearchEngineInputAnalysis.Invalid
        )
        assertTrue(
            CustomSearchEngineInputAnalyzer.analyze("") is CustomSearchEngineInputAnalysis.Invalid
        )
    }
}
