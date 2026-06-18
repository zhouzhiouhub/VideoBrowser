package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SafeUriParserTest {
    @Test
    fun `parses trimmed uri values`() {
        assertEquals("https", SafeUriParser.parse(" https://example.com/path ")?.scheme)
        assertEquals("example.com", SafeUriParser.parse(" https://example.com/path ")?.host)
    }

    @Test
    fun `returns null for blank or invalid uri values`() {
        assertNull(SafeUriParser.parse(" "))
        assertNull(SafeUriParser.parse("http://exa mple.com"))
        assertNull(SafeUriParser.parse(null))
    }

    @Test
    fun `extracts scheme through shared parser`() {
        assertEquals("http", SafeUriParser.scheme(" http://example.com "))
        assertNull(SafeUriParser.scheme(" "))
    }
}
