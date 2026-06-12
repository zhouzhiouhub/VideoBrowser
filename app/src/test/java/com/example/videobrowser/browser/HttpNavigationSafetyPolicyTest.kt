package com.example.videobrowser.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpNavigationSafetyPolicyTest {
    @Test
    fun requiresInsecureNavigationConfirmation_onlyForHttpsToHttpPageDowngrades() {
        assertTrue(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = "https://secure.example.com/page",
                targetUrl = "http://plain.example.com/"
            )
        )

        assertFalse(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = "https://secure.example.com/page",
                targetUrl = "https://secure.example.com/next"
            )
        )
        assertFalse(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = "http://plain.example.com/page",
                targetUrl = "http://plain.example.com/next"
            )
        )
        assertFalse(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = null,
                targetUrl = "http://plain.example.com/"
            )
        )
    }

    @Test
    fun requiresInsecureNavigationConfirmation_rejectsNonNetworkTargets() {
        assertFalse(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = "https://secure.example.com/page",
                targetUrl = "http:/missing-host"
            )
        )
        assertFalse(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = "https://secure.example.com/page",
                targetUrl = "file:///sdcard/page.html"
            )
        )
    }
}
