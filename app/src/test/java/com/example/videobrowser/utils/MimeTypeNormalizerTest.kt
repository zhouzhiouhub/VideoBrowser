package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MimeTypeNormalizerTest {
    @Test
    fun `removes parameters without changing original type casing`() {
        assertEquals("Video/MP4", MimeTypeNormalizer.withoutParameters(" Video/MP4 ; charset=utf-8"))
        assertNull(MimeTypeNormalizer.withoutParameters(" ; charset=utf-8"))
    }

    @Test
    fun `normalizes mime type for comparisons`() {
        assertEquals("video/mp4", MimeTypeNormalizer.normalize(" Video/MP4 ; charset=utf-8"))
        assertNull(MimeTypeNormalizer.normalize(null))
    }

    @Test
    fun `detects common top level media types`() {
        assertTrue(MimeTypeNormalizer.isVideo("video/mp4; charset=utf-8"))
        assertTrue(MimeTypeNormalizer.isImage("IMAGE/jpeg"))
        assertTrue(MimeTypeNormalizer.isAudio("audio/aac"))
        assertTrue(MimeTypeNormalizer.isText("text/vtt"))
        assertFalse(MimeTypeNormalizer.isVideo("application/pdf"))
    }
}
