package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Utf8UrlCodecContractTest {
    @Test
    fun `url component callers share utf8 url codec`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/utils/UrlUtils.kt"),
            projectFile("src/main/java/com/example/videobrowser/utils/SearchUrlQueryParser.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/search/SearchEngineUrlTools.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/search/SearchSuggestionClient.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/BrowserTabSessionRepository.kt"),
            projectFile("src/main/java/com/example/videobrowser/storage/SavedPageCodec.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/RuleNavigationUrlCleaner.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/ExternalProtocolPolicy.kt"),
            projectFile("src/main/java/com/example/videobrowser/functioncenter/PlaybackHistoryDisplayText.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("Utf8UrlCodec."))
            assertFalse(source.contains("URLEncoder.encode"))
            assertFalse(source.contains("URLDecoder.decode"))
        }
    }

}
