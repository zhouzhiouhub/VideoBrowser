package com.example.videobrowser.adblock

import android.net.Uri
import com.example.videobrowser.browser.BrowserRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AdBlockRequestInterceptorInstrumentedTest {
    @Test
    fun intercept_returnsEmptyResponseForMatchingAdUrl() {
        val interceptor = AdBlockRequestInterceptor(AdBlockManager())
        val request = BrowserRequest.from(
            uri = Uri.parse("https://stats.g.doubleclick.net/pagead/banner.js"),
            isForMainFrame = false
        )

        val response = interceptor.intercept(request)

        assertNotNull(response)
        requireNotNull(response)
        assertEquals(204, response.statusCode)
        assertEquals("No Content", response.reasonPhrase)
        assertEquals("text/plain", response.mimeType)
        assertEquals("utf-8", response.encoding)
        assertEquals(-1, response.data.read())
    }
}
