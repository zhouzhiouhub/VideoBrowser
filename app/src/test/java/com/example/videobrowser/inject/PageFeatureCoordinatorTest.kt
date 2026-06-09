package com.example.videobrowser.inject

import com.example.videobrowser.adblock.AdBlockDecisionReason
import com.example.videobrowser.adblock.AdBlockRequestPolicy
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.PreferenceStore
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageFeatureCoordinatorTest {
    @Test
    fun injectPageFeatures_skipsJavascriptWhenCurrentSiteDisablesInjection() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.setJsInjectionDisabledForSite(" Video.Example.COM. ", true)
        val evaluatedScripts = mutableListOf<String>()
        val coordinator = coordinatorFor(
            settings = settings,
            evaluatedScripts = evaluatedScripts,
            currentSiteHost = "video.example.com",
            currentPageUrl = "https://video.example.com/watch"
        )

        coordinator.injectPageFeatures()

        assertTrue(evaluatedScripts.isEmpty())
    }

    @Test
    fun currentSiteSwitches_disableAdBlockCleanupVideoAndWhitelistSeparately() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.setAdBlockDisabledForSite("video.example.com", true)
        settings.setJsInjectionDisabledForSite("video.example.com", true)
        settings.setDomAdBlockDisabledForSite("video.example.com", true)
        settings.setVideoEnhancementDisabledForSite("video.example.com", true)
        settings.setUserWhitelistedSite("ads.example.com", true)
        val coordinator = coordinatorFor(
            settings = settings,
            evaluatedScripts = mutableListOf(),
            currentSiteHost = "video.example.com",
            currentPageUrl = "https://video.example.com/watch"
        )

        assertTrue(coordinator.isCurrentSiteAdBlockDisabled())
        assertTrue(coordinator.isCurrentSiteJsInjectionDisabled())
        assertTrue(coordinator.isCurrentSitePageCleanupDisabled())
        assertTrue(coordinator.isCurrentSiteVideoEnhancementDisabled())

        val decision = AdBlockRequestPolicy.evaluate(
            enabled = settings.isAdBlockEnabled(),
            siteAdBlockDisabled = coordinator.isCurrentSiteAdBlockDisabled(),
            userWhitelisted = settings.isUserWhitelistedSite("ads.example.com"),
            url = "https://ads.example.com/banner.js",
            host = "ads.example.com",
            scheme = "https",
            pageHost = "video.example.com",
            isForMainFrame = false,
            ruleEngine = RuleEngine(listOf(Rule.blockDomainContains("ads.example.com")))
        )

        assertFalse(decision.shouldBlock)
        assertEquals(AdBlockDecisionReason.USER_WHITELISTED, decision.reason)
    }

    @Test
    fun injectPageFeatures_passesCurrentSiteUserSelectorsIntoFeatureConfig() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.addUserElementHideSelectorForSite("video.example.com", ".picked-ad")
        settings.addUserElementHideSelectorForSite("other.example.com", ".other-ad")
        val evaluatedScripts = mutableListOf<String>()
        val coordinator = coordinatorFor(
            settings = settings,
            evaluatedScripts = evaluatedScripts,
            currentSiteHost = "video.example.com",
            currentPageUrl = "https://video.example.com/watch"
        )

        coordinator.injectPageFeatures()

        val script = evaluatedScripts.single()
        assertTrue(script.contains("\"userCssSelectors\":[\".picked-ad\"]"))
        assertTrue(script.contains("\"cleanupEnabled\":true"))
        assertTrue(script.contains("\"videoEnabled\":true"))
    }

    @Test
    fun injectPageFeatures_appliesCurrentSiteCleanupAndVideoSwitches() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.setDomAdBlockDisabledForSite("video.example.com", true)
        settings.setVideoEnhancementDisabledForSite("video.example.com", true)
        settings.addUserElementHideSelectorForSite("video.example.com", ".picked-ad")
        val evaluatedScripts = mutableListOf<String>()
        val coordinator = coordinatorFor(
            settings = settings,
            evaluatedScripts = evaluatedScripts,
            currentSiteHost = "video.example.com",
            currentPageUrl = "https://video.example.com/watch"
        )

        coordinator.injectPageFeatures()

        val script = evaluatedScripts.single()
        assertFalse(script.contains("\"jsInjectionEnabled\""))
        assertTrue(script.contains("\"cleanupEnabled\":false"))
        assertTrue(script.contains("\"videoEnabled\":false"))
        assertTrue(script.contains("\"userCssSelectors\":[\".picked-ad\"]"))
    }

    @Test
    fun injectPageFeatures_doesNotRequestBrowserManagerWhenCurrentPageUrlIsSupplied() {
        val evaluatedScripts = mutableListOf<String>()
        var browserManagerRequested = false
        val coordinator = PageFeatureCoordinator(
            settingsManager = SettingsManager(InMemoryPreferenceStore()),
            browserManager = {
                browserManagerRequested = true
                error("BrowserManager should not be instantiated in this JVM regression test.")
            },
            jsInjector = jsInjectorFor(evaluatedScripts),
            currentSiteHost = { "video.example.com" },
            currentPageUrl = { "https://video.example.com/watch" }
        )

        coordinator.injectPageFeatures()

        assertFalse(browserManagerRequested)
        assertTrue(evaluatedScripts.isNotEmpty())
    }

    private fun coordinatorFor(
        settings: SettingsManager,
        evaluatedScripts: MutableList<String>,
        currentSiteHost: String?,
        currentPageUrl: String?
    ): PageFeatureCoordinator {
        return PageFeatureCoordinator(
            settingsManager = settings,
            browserManager = { unusedBrowserManager() },
            jsInjector = jsInjectorFor(evaluatedScripts),
            currentSiteHost = { currentSiteHost },
            currentPageUrl = { currentPageUrl }
        )
    }

    private fun jsInjectorFor(evaluatedScripts: MutableList<String>): JsInjector {
        return JsInjector(
            scriptLoader = ScriptLoader {
                ByteArrayInputStream(COMMON_SCRIPT.toByteArray(Charsets.UTF_8))
            },
            evaluateJavascript = { script -> evaluatedScripts += script }
        )
    }

    private fun unusedBrowserManager(): BrowserManager {
        error("Current page URL should be supplied by the test.")
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any>()

        override fun contains(key: String): Boolean {
            return values.containsKey(key)
        }

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return values[key] as? Boolean ?: defaultValue
        }

        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }

        override fun getFloat(key: String, defaultValue: Float): Float {
            return values[key] as? Float ?: defaultValue
        }

        override fun putFloat(key: String, value: Float) {
            values[key] = value
        }

        override fun getString(key: String, defaultValue: String?): String? {
            return values[key] as? String ?: defaultValue
        }

        override fun putString(key: String, value: String) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }

        override fun remove(keys: Iterable<String>, commit: Boolean): Boolean {
            keys.forEach { key -> values.remove(key) }
            return true
        }
    }

    private companion object {
        private const val COMMON_SCRIPT =
            "window.VideoBrowserEnhancer={apply:function(config){window.__applied=config;}};"
    }
}
