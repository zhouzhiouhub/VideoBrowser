package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Utf8UrlCodecTest {
    @Test
    fun `encodes and decodes utf8 form components`() {
        assertEquals("hello+world", Utf8UrlCodec.encodeFormComponent("hello world"))
        assertEquals("a+b space", Utf8UrlCodec.decodeFormComponent("a%2Bb+space"))
        assertEquals("fallback", Utf8UrlCodec.decodeFormComponentOr("%", "fallback"))
        assertNull(Utf8UrlCodec.decodeFormComponent("%"))
    }
}
