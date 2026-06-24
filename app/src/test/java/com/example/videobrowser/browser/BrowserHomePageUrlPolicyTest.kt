package com.example.videobrowser.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserHomePageUrlPolicyTest {
    private val policy = BrowserHomePageUrlPolicy(
        homeUrls = {
            listOf(
                "https://m.baidu.com/",
                "https://portal.example.com/start"
            )
        }
    )

    @Test
    fun isHomeUrl_matchesProviderAndConfiguredHomeUrlsIgnoringQueryAndCase() {
        assertTrue(policy.isHomeUrl("HTTPS://M.BAIDU.COM/?from=browser"))
        assertTrue(policy.isHomeUrl("https://portal.example.com/start?source=browser"))
    }

    @Test
    fun isHomeUrl_rejectsContentPagesAndNonWebUrls() {
        assertFalse(policy.isHomeUrl("https://m.baidu.com/s?word=video"))
        assertFalse(policy.isHomeUrl("https://portal.example.com/start/article"))
        assertFalse(policy.isHomeUrl("about:blank"))
    }
}
