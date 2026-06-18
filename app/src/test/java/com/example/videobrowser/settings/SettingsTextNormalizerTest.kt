package com.example.videobrowser.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTextNormalizerTest {
    @Test
    fun `collapse whitespace trims and normalizes internal runs`() {
        assertEquals(
            "Browser Shortcut",
            SettingsTextNormalizer.collapseWhitespace("  Browser\t\nShortcut  ")
        )
    }

    @Test
    fun `tab or line break detection matches persisted line separators`() {
        assertFalse(SettingsTextNormalizer.hasTabOrLineBreak("Browser Shortcut"))
        assertTrue(SettingsTextNormalizer.hasTabOrLineBreak("Browser\tShortcut"))
        assertTrue(SettingsTextNormalizer.hasTabOrLineBreak("Browser\nShortcut"))
        assertTrue(SettingsTextNormalizer.hasTabOrLineBreak("Browser\rShortcut"))
    }
}
