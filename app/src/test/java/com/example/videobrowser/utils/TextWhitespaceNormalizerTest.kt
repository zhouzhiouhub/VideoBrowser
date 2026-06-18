package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextWhitespaceNormalizerTest {
    @Test
    fun `collapse trims and normalizes internal whitespace runs`() {
        assertEquals(
            "Browser Shortcut",
            TextWhitespaceNormalizer.collapse("  Browser\t\nShortcut  ")
        )
    }

    @Test
    fun `tab or line break detection matches persisted line separators`() {
        assertFalse(TextWhitespaceNormalizer.hasTabOrLineBreak("Browser Shortcut"))
        assertTrue(TextWhitespaceNormalizer.hasTabOrLineBreak("Browser\tShortcut"))
        assertTrue(TextWhitespaceNormalizer.hasTabOrLineBreak("Browser\nShortcut"))
        assertTrue(TextWhitespaceNormalizer.hasTabOrLineBreak("Browser\rShortcut"))
    }
}
