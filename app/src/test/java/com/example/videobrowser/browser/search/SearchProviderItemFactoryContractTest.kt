package com.example.videobrowser.browser.search

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchProviderItemFactoryContractTest {
    @Test
    fun selectableHomeItemSetupIsShared() {
        val itemFactory = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderItemFactory.kt"
        ).readText()

        assertTrue(itemFactory.contains("private fun createSelectableHomeItem("))
        assertEquals(4, Regex("return createSelectableHomeItem\\(").findAll(itemFactory).count())
        assertEquals(1, Regex("orientation = LinearLayout\\.VERTICAL").findAll(itemFactory).count())
        assertEquals(1, Regex("setPadding\\(dp\\(4\\), 0, dp\\(4\\), 0\\)").findAll(itemFactory).count())
        assertEquals(1, Regex("setBoundedSelectableItemBackground\\(\\)").findAll(itemFactory).count())
        assertEquals(1, Regex("setOnLongClickListener").findAll(itemFactory).count())
        assertTrue(itemFactory.contains("onLongClick = { onCustomShortcutLongClick(shortcut) }"))
        assertTrue(itemFactory.contains("onLongClick = { onRecentHistoryLongClick(quickLink) }"))
    }
}
