package com.example.videobrowser.browser.search

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressSuggestionRowFactoryContractTest {
    @Test
    fun savedPageSuggestionTextRowsUseSharedAssembly() {
        val rowFactory = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/AddressSuggestionRowFactory.kt"
        ).readText()

        assertTrue(rowFactory.contains("private fun LinearLayout.addSavedPageTextRows(title: String, displayUrl: String)"))
        assertTrue(rowFactory.contains("is AddressSuggestion.Bookmark -> addSavedPageTextRows("))
        assertTrue(rowFactory.contains("is AddressSuggestion.History -> addSavedPageTextRows("))
        assertEquals(1, Regex("addView\\(createPrimaryText\\(title\\)\\)").findAll(rowFactory).count())
        assertEquals(1, Regex("addView\\(createSecondaryText\\(displayUrl\\)\\)").findAll(rowFactory).count())
        assertEquals(0, Regex("createPrimaryText\\(suggestion\\.title\\)").findAll(rowFactory).count())
        assertEquals(0, Regex("createSecondaryText\\(suggestion\\.displayUrl\\)").findAll(rowFactory).count())
    }
}
