package com.example.videobrowser.functioncenter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BrowserDataManagementPageTest {
    @Test
    fun cookieParserOnlyExposesCookieNames() {
        val cookies = BrowserCookieParser.parse("session=secret-token; theme=dark")

        assertEquals(listOf("session", "theme"), cookies.map { cookie -> cookie.name })
        assertFalse(
            BrowserCookieItem::class.java.declaredFields.any { field ->
                field.name == "valuePreview"
            }
        )
    }

    @Test
    fun siteDataSummaryOnlyShowsUsedStorage() {
        assertEquals("1.5 KB", BrowserDataDisplayFormatter.siteDataUsageSummary(1536L))
    }
}
