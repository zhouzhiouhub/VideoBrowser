package com.example.videobrowser.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class DomainScopeParserTest {
    @Test
    fun `comma separated domains normalize included and excluded hosts`() {
        val scope = DomainScopeParser.parseCommaSeparated(" Video.Example.COM , ~Safe.Example.COM ")

        assertEquals(setOf("video.example.com"), scope?.includedDomains)
        assertEquals(setOf("safe.example.com"), scope?.excludedDomains)
    }

    @Test
    fun `comma separated domains require at least one parsed domain by default`() {
        assertSame(DomainScope.Empty, DomainScopeParser.parseCommaSeparated(" "))
        assertNull(DomainScopeParser.parseCommaSeparated(","))
    }

    @Test
    fun `comma separated domains can allow empty parsed domain scopes`() {
        assertEquals(
            DomainScope.Empty,
            DomainScopeParser.parseCommaSeparated(",", requireDomain = false)
        )
    }

    @Test
    fun `pipe separated domains parse request option scopes`() {
        val scope = DomainScopeParser.parsePipeSeparated("video.example.com|~safe.video.example.com")

        assertEquals(setOf("video.example.com"), scope?.includedDomains)
        assertEquals(setOf("safe.video.example.com"), scope?.excludedDomains)
    }

    @Test
    fun `domain scopes reject unsupported characters`() {
        assertNull(DomainScopeParser.parseCommaSeparated("exa*mple.com"))
        assertNull(DomainScopeParser.parsePipeSeparated("exa*mple.com"))
    }
}
