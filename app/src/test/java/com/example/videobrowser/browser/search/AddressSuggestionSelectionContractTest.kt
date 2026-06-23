package com.example.videobrowser.browser.search

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressSuggestionSelectionContractTest {
    @Test
    fun suggestionSelectionUsesSharedSuggestionRoles() {
        val suggestion = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/AddressSuggestion.kt"
        ).readText()
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/AddressSuggestionController.kt"
        ).readText()

        assertTrue(suggestion.contains("sealed class SavedPageSuggestion : AddressSuggestion()"))
        assertTrue(suggestion.contains("sealed class KeywordSuggestion : AddressSuggestion()"))
        assertTrue(suggestion.contains(") : SavedPageSuggestion()"))
        assertTrue(suggestion.contains(") : KeywordSuggestion()"))
        assertTrue(controller.contains("is AddressSuggestion.SavedPageSuggestion -> openUrl(suggestion.url)"))
        assertTrue(controller.contains("is AddressSuggestion.KeywordSuggestion -> searchKeyword(suggestion.keyword)"))
        assertEquals(1, Regex("openUrl\\(suggestion\\.url\\)").findAll(controller).count())
        assertEquals(1, Regex("searchKeyword\\(suggestion\\.keyword\\)").findAll(controller).count())
    }
}
