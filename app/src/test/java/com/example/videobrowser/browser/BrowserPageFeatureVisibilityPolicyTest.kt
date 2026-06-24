package com.example.videobrowser.browser

import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.testutil.InMemoryPreferenceStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserPageFeatureVisibilityPolicyTest {
    @Test
    fun shouldHideUntilPageFeaturesInjected_hidesCleanupPagesWhenDomCleanupEnabled() {
        val policy = policyFor(settings = SettingsManager(InMemoryPreferenceStore()))

        assertTrue(policy.shouldHideUntilPageFeaturesInjected("https://video.example.com/watch"))
    }

    @Test
    fun shouldHideUntilPageFeaturesInjected_hidesBuiltInSearchPagesWhenDomCleanupDisabled() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.setDomAdBlockEnabled(false)
        val policy = policyFor(
            settings = settings,
            isBuiltInSearchResultPage = { url -> url == "https://m.baidu.com/s?word=hello" }
        )

        assertTrue(policy.shouldHideUntilPageFeaturesInjected("https://m.baidu.com/s?word=hello"))
    }

    @Test
    fun shouldHideUntilPageFeaturesInjected_hidesUserSelectorPagesWhenDomCleanupDisabled() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.setDomAdBlockEnabled(false)
        settings.addUserElementHideSelectorForSite("video.example.com", ".picked-ad")
        val policy = policyFor(settings = settings)

        assertTrue(policy.shouldHideUntilPageFeaturesInjected("https://video.example.com/watch"))
    }

    @Test
    fun shouldHideUntilPageFeaturesInjected_respectsJavascriptAndDomCleanupSiteSwitches() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.setDomAdBlockDisabledForSite("video.example.com", true)
        val policy = policyFor(settings = settings)

        assertFalse(policy.shouldHideUntilPageFeaturesInjected("https://video.example.com/watch"))

        settings.setDomAdBlockDisabledForSite("video.example.com", false)
        settings.setJsInjectionDisabledForSite("video.example.com", true)

        assertFalse(policy.shouldHideUntilPageFeaturesInjected("https://video.example.com/watch"))
    }

    @Test
    fun shouldHideUntilPageFeaturesInjected_ignoresNonWebPages() {
        val policy = policyFor(settings = SettingsManager(InMemoryPreferenceStore()))

        assertFalse(policy.shouldHideUntilPageFeaturesInjected("about:blank"))
        assertFalse(policy.shouldHideUntilPageFeaturesInjected(null))
    }

    private fun policyFor(
        settings: SettingsManager,
        isBuiltInSearchResultPage: (String?) -> Boolean = { false }
    ): BrowserPageFeatureVisibilityPolicy {
        return BrowserPageFeatureVisibilityPolicy(
            settingsManager = settings,
            isBuiltInSearchResultPage = isBuiltInSearchResultPage
        )
    }
}
