package com.example.videobrowser.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TabSeparatedLineCodecTest {
    @Test
    fun `split pair accepts exactly the first tab as separator`() {
        assertEquals("name" to "https://example.com", TabSeparatedLineCodec.splitPair("name\thttps://example.com"))
        assertEquals("host" to ".ad\t.extra", TabSeparatedLineCodec.splitPair("host\t.ad\t.extra"))
    }

    @Test
    fun `split pair rejects missing or empty sides`() {
        assertNull(TabSeparatedLineCodec.splitPair(""))
        assertNull(TabSeparatedLineCodec.splitPair("missing-tab"))
        assertNull(TabSeparatedLineCodec.splitPair("\tmissing-first"))
        assertNull(TabSeparatedLineCodec.splitPair("missing-second\t"))
    }

    @Test
    fun `join pair uses a single tab separator`() {
        assertEquals("host\t.selector", TabSeparatedLineCodec.joinPair("host", ".selector"))
    }
}
