package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HostNameNormalizerTest {
    @Test
    fun `normalizes host casing and dot padding`() {
        assertEquals("video.example.com", HostNameNormalizer.normalize(" Video.Example.COM. "))
        assertNull(HostNameNormalizer.normalize(" . "))
    }

    @Test
    fun `extracts hosts from urls without treating bare hosts as urls`() {
        assertEquals("www.example.com", HostNameNormalizer.fromUrl("https://WWW.Example.COM/watch"))
        assertNull(HostNameNormalizer.fromUrl("example.com/watch"))
    }

    @Test
    fun `can extract hosts from urls or bare host paths`() {
        assertEquals("m.youtube.com", HostNameNormalizer.fromUrlOrBareHost("m.youtube.com/watch?v=1"))
        assertEquals("m.youtube.com", HostNameNormalizer.fromUrlOrBareHost("https://m.youtube.com/watch?v=1"))
    }

    @Test
    fun `matches domains only on subdomain boundaries`() {
        assertTrue(HostNameNormalizer.matchesDomainOrSubdomain("m.youtube.com", "youtube.com"))
        assertTrue(HostNameNormalizer.matchesDomainOrSubdomain("youtube.com", "youtube.com"))
        assertFalse(HostNameNormalizer.matchesDomainOrSubdomain("notyoutube.com", "youtube.com"))
        assertFalse(HostNameNormalizer.matchesDomainOrSubdomain("youtube.com.example.com", "youtube.com"))
    }
}
