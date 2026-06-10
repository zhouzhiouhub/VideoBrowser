package com.example.videobrowser.settings

import com.example.videobrowser.storage.PreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsManagerTest {
    @Test
    fun defaults_useMvpSettingsWhenPreferencesAreEmpty() {
        val settings = SettingsManager(InMemoryPreferenceStore())

        assertTrue(settings.isAdBlockEnabled())
        assertTrue(settings.isJsInjectionEnabled())
        assertTrue(settings.isDomAdBlockEnabled())
        assertTrue(settings.isVideoEnhancementEnabled())
        assertFalse(settings.isSmartNoImageEnabled())
        assertFalse(settings.isDesktopModeEnabled())
        assertFalse(settings.isPrivateBrowsingEnabled())
        assertEquals(SettingsManager.DEFAULT_VIDEO_SPEED, settings.defaultVideoSpeed(), 0.001f)
        assertEquals("https://m.baidu.com/", settings.homeUrl())
        assertEquals("baidu", settings.searchEngineId())
    }

    @Test
    fun privateBrowsingPreference_isIgnoredOnStartup() {
        val store = InMemoryPreferenceStore()
        store.putBoolean("private_browsing", true)

        val settings = SettingsManager(store)

        assertFalse(settings.isPrivateBrowsingEnabled())
        assertFalse(store.contains("private_browsing"))
    }

    @Test
    fun setPrivateBrowsingEnabled_doesNotPersistPrivateMode() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        settings.setPrivateBrowsingEnabled(true)

        val reloaded = SettingsManager(store)
        assertFalse(reloaded.isPrivateBrowsingEnabled())
        assertFalse(store.contains("private_browsing"))
    }

    @Test
    fun settings_arePersistedThroughPreferenceStore() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        settings.setAdBlockEnabled(false)
        settings.setJsInjectionEnabled(false)
        settings.setDomAdBlockEnabled(false)
        settings.setVideoEnhancementEnabled(false)
        settings.setSmartNoImageEnabled(true)
        settings.setDesktopModeEnabled(true)
        settings.setPrivateBrowsingEnabled(true)
        settings.setDefaultVideoSpeed(1.5f)
        settings.setHomeUrl("https://m.sogou.com/")
        settings.setSearchEngineId("sogou")

        val reloaded = SettingsManager(store)
        assertFalse(reloaded.isAdBlockEnabled())
        assertFalse(reloaded.isJsInjectionEnabled())
        assertFalse(reloaded.isDomAdBlockEnabled())
        assertFalse(reloaded.isVideoEnhancementEnabled())
        assertTrue(reloaded.isSmartNoImageEnabled())
        assertTrue(reloaded.isDesktopModeEnabled())
        assertFalse(reloaded.isPrivateBrowsingEnabled())
        assertFalse(store.contains("private_browsing"))
        assertEquals(1.5f, reloaded.defaultVideoSpeed(), 0.001f)
        assertEquals("https://m.sogou.com/", reloaded.homeUrl())
        assertEquals("sogou", reloaded.searchEngineId())
    }

    @Test
    fun invalidValues_fallBackToCodeDefaults() {
        val settings = SettingsManager(InMemoryPreferenceStore())

        settings.setDefaultVideoSpeed(Float.NaN)
        settings.setHomeUrl("about:blank")
        settings.setSearchEngineId("   ")

        assertEquals(SettingsManager.DEFAULT_VIDEO_SPEED, settings.defaultVideoSpeed(), 0.001f)
        assertEquals(SettingsManager.DEFAULT_HOME_URL, settings.homeUrl())
        assertEquals(SettingsManager.DEFAULT_SEARCH_ENGINE_ID, settings.searchEngineId())
    }

    @Test
    fun restoreDefaults_removesOnlySettingsKeys() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)
        settings.setAdBlockEnabled(false)
        settings.setAdBlockDisabledForSite("video.example.com", true)
        settings.setJsInjectionDisabledForSite("video.example.com", true)
        settings.setDomAdBlockDisabledForSite("video.example.com", true)
        settings.setVideoEnhancementDisabledForSite("video.example.com", true)
        settings.setSmartNoImageDisabledForSite("video.example.com", true)
        settings.setUserWhitelistedSite("ads.example.com", true)
        settings.addUserElementHideSelectorForSite("video.example.com", "#ad")
        settings.setJsInjectionEnabled(false)
        settings.setSmartNoImageEnabled(true)
        settings.setPrivateBrowsingEnabled(true)
        settings.setHomeUrl("https://m.sogou.com/")
        settings.addCustomShortcut("Docs", "https://docs.example.com/")
        store.putString("bookmarks", "[]")

        assertTrue(settings.restoreDefaults())

        assertTrue(settings.isAdBlockEnabled())
        assertFalse(settings.isAdBlockDisabledForSite("video.example.com"))
        assertFalse(settings.isJsInjectionDisabledForSite("video.example.com"))
        assertFalse(settings.isDomAdBlockDisabledForSite("video.example.com"))
        assertFalse(settings.isVideoEnhancementDisabledForSite("video.example.com"))
        assertFalse(settings.isSmartNoImageDisabledForSite("video.example.com"))
        assertFalse(settings.isUserWhitelistedSite("ads.example.com"))
        assertTrue(settings.userElementHideSelectorsForSite("video.example.com").isEmpty())
        assertTrue(settings.isJsInjectionEnabled())
        assertFalse(settings.isSmartNoImageEnabled())
        assertFalse(settings.isPrivateBrowsingEnabled())
        assertEquals(SettingsManager.DEFAULT_HOME_URL, settings.homeUrl())
        assertTrue(settings.customShortcuts().isEmpty())
        assertEquals("[]", store.getString("bookmarks", null))
    }

    @Test
    fun siteAdBlockDisabledHosts_areNormalizedAndPersisted() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertTrue(settings.setAdBlockDisabledForSite(" Video.Example.COM. ", true))

        val reloaded = SettingsManager(store)
        assertTrue(reloaded.isAdBlockDisabledForSite("video.example.com"))
        assertEquals(setOf("video.example.com"), reloaded.adBlockDisabledSiteHosts())

        assertTrue(reloaded.setAdBlockDisabledForSite("video.example.com", false))
        assertFalse(reloaded.isAdBlockDisabledForSite("video.example.com"))
    }

    @Test
    fun siteJsInjectionDisabledHosts_areNormalizedAndPersisted() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertTrue(settings.setJsInjectionDisabledForSite(" Video.Example.COM. ", true))

        val reloaded = SettingsManager(store)
        assertTrue(reloaded.isJsInjectionDisabledForSite("video.example.com"))
        assertEquals(setOf("video.example.com"), reloaded.jsInjectionDisabledSiteHosts())

        assertTrue(reloaded.setJsInjectionDisabledForSite("video.example.com", false))
        assertFalse(reloaded.isJsInjectionDisabledForSite("video.example.com"))
    }

    @Test
    fun siteDomAdBlockDisabledHosts_areNormalizedAndPersisted() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertTrue(settings.setDomAdBlockDisabledForSite(" Video.Example.COM. ", true))

        val reloaded = SettingsManager(store)
        assertTrue(reloaded.isDomAdBlockDisabledForSite("video.example.com"))
        assertEquals(setOf("video.example.com"), reloaded.domAdBlockDisabledSiteHosts())

        assertTrue(reloaded.setDomAdBlockDisabledForSite("video.example.com", false))
        assertFalse(reloaded.isDomAdBlockDisabledForSite("video.example.com"))
    }

    @Test
    fun siteVideoEnhancementDisabledHosts_areNormalizedAndPersisted() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertTrue(settings.setVideoEnhancementDisabledForSite(" Video.Example.COM. ", true))

        val reloaded = SettingsManager(store)
        assertTrue(reloaded.isVideoEnhancementDisabledForSite("video.example.com"))
        assertEquals(setOf("video.example.com"), reloaded.videoEnhancementDisabledSiteHosts())

        assertTrue(reloaded.setVideoEnhancementDisabledForSite("video.example.com", false))
        assertFalse(reloaded.isVideoEnhancementDisabledForSite("video.example.com"))
    }

    @Test
    fun siteSmartNoImageDisabledHosts_areNormalizedAndPersisted() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertTrue(settings.setSmartNoImageDisabledForSite(" Video.Example.COM. ", true))

        val reloaded = SettingsManager(store)
        assertTrue(reloaded.isSmartNoImageDisabledForSite("video.example.com"))
        assertEquals(setOf("video.example.com"), reloaded.smartNoImageDisabledSiteHosts())

        assertTrue(reloaded.setSmartNoImageDisabledForSite("video.example.com", false))
        assertFalse(reloaded.isSmartNoImageDisabledForSite("video.example.com"))
    }

    @Test
    fun userWhitelistedSiteHosts_areNormalizedAndPersisted() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertTrue(settings.setUserWhitelistedSite(" Ads.Example.COM. ", true))

        val reloaded = SettingsManager(store)
        assertTrue(reloaded.isUserWhitelistedSite("ads.example.com"))
        assertEquals(setOf("ads.example.com"), reloaded.userWhitelistedSiteHosts())

        assertTrue(reloaded.setUserWhitelistedSite("ads.example.com", false))
        assertFalse(reloaded.isUserWhitelistedSite("ads.example.com"))
    }

    @Test
    fun userWhitelistedSiteHosts_canBeCleared() {
        val settings = SettingsManager(InMemoryPreferenceStore())

        assertTrue(settings.setUserWhitelistedSite("ads.example.com", true))
        assertTrue(settings.setUserWhitelistedSite("tracker.example.com", true))

        settings.clearUserWhitelistedSites()

        assertTrue(settings.userWhitelistedSiteHosts().isEmpty())
    }

    @Test
    fun userElementHideSelectors_areNormalizedAndPersistedByHost() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertTrue(settings.addUserElementHideSelectorForSite(" Video.Example.COM. ", "  #ad   .banner  "))
        assertFalse(settings.addUserElementHideSelectorForSite("video.example.com", "#ad .banner"))

        val reloaded = SettingsManager(store)
        assertTrue(reloaded.hasUserElementHideSelectorForSite("video.example.com", "#ad .banner"))
        assertEquals(listOf("#ad .banner"), reloaded.userElementHideSelectorsForSite("video.example.com"))
        assertTrue(reloaded.userElementHideSelectorsForSite("other.example.com").isEmpty())
    }

    @Test
    fun userElementHideSelectors_dropPositionalPickerSegmentsWhenStableTokensExist() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertTrue(
            settings.addUserElementHideSelectorForSite(
                "video.example.com",
                "div.ad-card:nth-of-type(3)  button.close-ad:nth-of-type(1)"
            )
        )

        val reloaded = SettingsManager(store)
        assertEquals(
            listOf("div.ad-card button.close-ad"),
            reloaded.userElementHideSelectorsForSite("video.example.com")
        )
    }

    @Test
    fun userElementHideSelectors_keepPositionalPickerSegmentsWithoutStableTokens() {
        val settings = SettingsManager(InMemoryPreferenceStore())

        assertTrue(settings.addUserElementHideSelectorForSite("video.example.com", "div:nth-of-type(3)"))

        assertEquals(
            listOf("div:nth-of-type(3)"),
            settings.userElementHideSelectorsForSite("video.example.com")
        )
    }

    @Test
    fun userElementHideSelectors_rejectUnsafeValues() {
        val settings = SettingsManager(InMemoryPreferenceStore())

        assertFalse(settings.addUserElementHideSelectorForSite(null, "#ad"))
        assertFalse(settings.addUserElementHideSelectorForSite("video.example.com", ""))
        assertFalse(settings.addUserElementHideSelectorForSite("video.example.com", "div{display:none}"))
        assertFalse(settings.addUserElementHideSelectorForSite("video.example.com", "a[href^=\"javascript:\"]"))
        assertTrue(settings.userElementHideRules().isEmpty())
    }

    @Test
    fun userElementHideRules_canBeRemovedIndividuallyAndCleared() {
        val settings = SettingsManager(InMemoryPreferenceStore())

        assertTrue(settings.addUserElementHideSelectorForSite("video.example.com", ".ad-card"))
        assertTrue(settings.addUserElementHideSelectorForSite("news.example.com", "#sponsor"))

        assertTrue(settings.removeUserElementHideRule(UserElementHideRule("video.example.com", ".ad-card")))
        assertFalse(settings.removeUserElementHideRule(UserElementHideRule("video.example.com", ".ad-card")))
        assertEquals(
            listOf(UserElementHideRule("news.example.com", "#sponsor")),
            settings.userElementHideRules()
        )

        settings.clearUserElementHideRules()

        assertTrue(settings.userElementHideRules().isEmpty())
    }

    @Test
    fun customShortcuts_arePersistedInInsertionOrder() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertTrue(settings.addCustomShortcut(" 视频站 ", " https://video.example.com/home "))
        assertTrue(settings.addCustomShortcut("Docs", "http://docs.example.com"))

        val reloaded = SettingsManager(store)
        assertEquals(
            listOf(
                CustomShortcut("视频站", "https://video.example.com/home"),
                CustomShortcut("Docs", "http://docs.example.com")
            ),
            reloaded.customShortcuts()
        )
    }

    @Test
    fun customShortcuts_keepMostRecentTenEntries() {
        val settings = SettingsManager(InMemoryPreferenceStore())

        (1..11).forEach { index ->
            assertTrue(settings.addCustomShortcut("站点$index", "https://example.com/$index"))
        }

        assertEquals(
            (2..11).map { index -> CustomShortcut("站点$index", "https://example.com/$index") },
            settings.customShortcuts()
        )
    }

    @Test
    fun customShortcuts_rejectInvalidInputAndFilterCorruptStorage() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertFalse(settings.addCustomShortcut("", "https://example.com"))
        assertFalse(settings.addCustomShortcut("Docs", ""))
        assertFalse(settings.addCustomShortcut("Script", "javascript:alert(1)"))
        store.putString(
            "custom_shortcuts",
            listOf(
                "Good\thttps://good.example.com",
                "BadUrl\tftp://bad.example.com",
                "\thttps://missing-name.example.com",
                "MissingUrl\t",
                "Good\thttps://good.example.com"
            ).joinToString(separator = "\n")
        )

        assertEquals(
            listOf(CustomShortcut("Good", "https://good.example.com")),
            settings.customShortcuts()
        )
    }

    @Test
    fun customShortcuts_doNotChangeSearchProviderOrHomeUrl() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        settings.setSearchEngineId("sogou")
        settings.setHomeUrl("https://m.sogou.com/")

        assertTrue(settings.addCustomShortcut("Bing", "https://www.bing.com/"))

        assertEquals("sogou", settings.searchEngineId())
        assertEquals("https://m.sogou.com/", settings.homeUrl())
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
}
