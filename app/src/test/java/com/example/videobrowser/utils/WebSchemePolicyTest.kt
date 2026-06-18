package com.example.videobrowser.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebSchemePolicyTest {
    @Test
    fun `recognizes http and https schemes case insensitively`() {
        assertTrue(WebSchemePolicy.isHttpScheme("HTTP"))
        assertTrue(WebSchemePolicy.isHttpsScheme("https"))
        assertTrue(WebSchemePolicy.isHttpOrHttpsScheme("HtTpS"))
    }

    @Test
    fun `does not treat about as network http`() {
        assertFalse(WebSchemePolicy.isHttpOrHttpsScheme("about"))
        assertFalse(WebSchemePolicy.isHttpOrHttpsScheme(null))
    }

    @Test
    fun `allows about only for webview loadable schemes`() {
        assertTrue(WebSchemePolicy.isWebViewLoadableScheme("about"))
        assertTrue(WebSchemePolicy.isWebViewLoadableScheme("http"))
        assertFalse(WebSchemePolicy.isWebViewLoadableScheme("file"))
    }
}
