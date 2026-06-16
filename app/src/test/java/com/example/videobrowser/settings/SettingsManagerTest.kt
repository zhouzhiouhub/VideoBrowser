package com.example.videobrowser.settings

/**
 * 测试阅读提示：
 * 这个测试文件验证“Settings Manager Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
        assertFalse(settings.alwaysStartVideosFromBeginning())
        assertFalse(settings.areThirdPartyCookiesEnabled())
        assertTrue(settings.isMixedContentBlocked())
        assertEquals(SettingsManager.DEFAULT_VIDEO_SPEED, settings.defaultVideoSpeed(), 0.001f)
        assertEquals(SettingsManager.DEFAULT_TEXT_ZOOM_PERCENT, settings.textZoomPercent())
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
        settings.setAlwaysStartVideosFromBeginning(true)
        settings.setThirdPartyCookiesEnabled(true)
        settings.setMixedContentBlocked(false)
        settings.setDefaultVideoSpeed(1.5f)
        settings.setTextZoomPercent(125)
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
        assertTrue(reloaded.alwaysStartVideosFromBeginning())
        assertTrue(reloaded.areThirdPartyCookiesEnabled())
        assertFalse(reloaded.isMixedContentBlocked())
        assertFalse(store.contains("private_browsing"))
        assertEquals(1.5f, reloaded.defaultVideoSpeed(), 0.001f)
        assertEquals(125, reloaded.textZoomPercent())
        assertEquals("https://m.sogou.com/", reloaded.homeUrl())
        assertEquals("sogou", reloaded.searchEngineId())
    }

    @Test
    fun invalidValues_fallBackToCodeDefaults() {
        val settings = SettingsManager(InMemoryPreferenceStore())

        settings.setDefaultVideoSpeed(Float.NaN)
        settings.setTextZoomPercent(999)
        settings.setHomeUrl("about:blank")
        settings.setSearchEngineId("   ")

        assertEquals(SettingsManager.DEFAULT_VIDEO_SPEED, settings.defaultVideoSpeed(), 0.001f)
        assertEquals(SettingsManager.DEFAULT_TEXT_ZOOM_PERCENT, settings.textZoomPercent())
        assertEquals(SettingsManager.DEFAULT_HOME_URL, settings.homeUrl())
        assertEquals(SettingsManager.DEFAULT_SEARCH_ENGINE_ID, settings.searchEngineId())
    }

    @Test
    fun homeUrlValidationAcceptsOnlyHttpUrlsWithHosts() {
        val settings = SettingsManager(InMemoryPreferenceStore())

        assertTrue(settings.isValidHomeUrl(" https://example.com/start "))
        assertTrue(settings.isValidHomeUrl("http://localhost:8080"))
        assertFalse(settings.isValidHomeUrl("about:blank"))
        assertFalse(settings.isValidHomeUrl("javascript:alert(1)"))
        assertFalse(settings.isValidHomeUrl("https://"))
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
        settings.setSitePermissionDecision("camera.example.com", SitePermission.CAMERA, SitePermissionDecision.ALLOW)
        settings.setSitePermissionDecision("mic.example.com", SitePermission.MICROPHONE, SitePermissionDecision.BLOCK)
        settings.setSitePermissionDecision("maps.example.com", SitePermission.LOCATION, SitePermissionDecision.ALLOW)
        settings.addUserElementHideSelectorForSite("video.example.com", "#ad")
        settings.setJsInjectionEnabled(false)
        settings.setSmartNoImageEnabled(true)
        settings.setPrivateBrowsingEnabled(true)
        settings.setAlwaysStartVideosFromBeginning(true)
        settings.setThirdPartyCookiesEnabled(true)
        settings.setMixedContentBlocked(false)
        settings.setTextZoomPercent(150)
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
        assertEquals(
            SitePermissionDecision.ASK,
            settings.sitePermissionDecision("camera.example.com", SitePermission.CAMERA)
        )
        assertEquals(
            SitePermissionDecision.ASK,
            settings.sitePermissionDecision("mic.example.com", SitePermission.MICROPHONE)
        )
        assertEquals(
            SitePermissionDecision.ASK,
            settings.sitePermissionDecision("maps.example.com", SitePermission.LOCATION)
        )
        assertTrue(settings.userElementHideSelectorsForSite("video.example.com").isEmpty())
        assertTrue(settings.isJsInjectionEnabled())
        assertFalse(settings.isSmartNoImageEnabled())
        assertFalse(settings.isPrivateBrowsingEnabled())
        assertFalse(settings.alwaysStartVideosFromBeginning())
        assertFalse(settings.areThirdPartyCookiesEnabled())
        assertTrue(settings.isMixedContentBlocked())
        assertEquals(SettingsManager.DEFAULT_TEXT_ZOOM_PERCENT, settings.textZoomPercent())
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
    fun sitePermissionDecisions_areNormalizedPersistedAndMutuallyExclusive() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        assertTrue(
            settings.setSitePermissionDecision(
                " Camera.Example.COM. ",
                SitePermission.CAMERA,
                SitePermissionDecision.ALLOW
            )
        )
        assertTrue(
            settings.setSitePermissionDecision(
                " Mic.Example.COM. ",
                SitePermission.MICROPHONE,
                SitePermissionDecision.BLOCK
            )
        )
        assertTrue(
            settings.setSitePermissionDecision(
                " Maps.Example.COM. ",
                SitePermission.LOCATION,
                SitePermissionDecision.ALLOW
            )
        )

        val reloaded = SettingsManager(store)
        assertEquals(
            SitePermissionDecision.ALLOW,
            reloaded.sitePermissionDecision("camera.example.com", SitePermission.CAMERA)
        )
        assertEquals(
            SitePermissionDecision.BLOCK,
            reloaded.sitePermissionDecision("mic.example.com", SitePermission.MICROPHONE)
        )
        assertEquals(
            SitePermissionDecision.ALLOW,
            reloaded.sitePermissionDecision("maps.example.com", SitePermission.LOCATION)
        )
        assertEquals(setOf("camera.example.com"), reloaded.allowedSitePermissionHosts(SitePermission.CAMERA))
        assertEquals(setOf("mic.example.com"), reloaded.blockedSitePermissionHosts(SitePermission.MICROPHONE))

        assertTrue(
            reloaded.setSitePermissionDecision(
                "camera.example.com",
                SitePermission.CAMERA,
                SitePermissionDecision.BLOCK
            )
        )
        assertEquals(
            SitePermissionDecision.BLOCK,
            reloaded.sitePermissionDecision("camera.example.com", SitePermission.CAMERA)
        )
        assertTrue(reloaded.allowedSitePermissionHosts(SitePermission.CAMERA).isEmpty())
        assertEquals(setOf("camera.example.com"), reloaded.blockedSitePermissionHosts(SitePermission.CAMERA))

        assertTrue(
            reloaded.setSitePermissionDecision(
                "camera.example.com",
                SitePermission.CAMERA,
                SitePermissionDecision.ASK
            )
        )
        assertEquals(
            SitePermissionDecision.ASK,
            reloaded.sitePermissionDecision("camera.example.com", SitePermission.CAMERA)
        )
        assertTrue(reloaded.blockedSitePermissionHosts(SitePermission.CAMERA).isEmpty())
    }

    @Test
    fun sitePermissionRecords_listAndClearPersistedDecisions() {
        val settings = SettingsManager(InMemoryPreferenceStore())

        settings.setSitePermissionDecision(
            "Camera.Example.COM",
            SitePermission.CAMERA,
            SitePermissionDecision.ALLOW
        )
        settings.setSitePermissionDecision(
            "Mic.Example.COM",
            SitePermission.MICROPHONE,
            SitePermissionDecision.BLOCK
        )
        settings.setSitePermissionDecision(
            "Maps.Example.COM",
            SitePermission.LOCATION,
            SitePermissionDecision.ALLOW
        )

        assertEquals(
            setOf(
                SitePermissionRecord(
                    host = "camera.example.com",
                    permission = SitePermission.CAMERA,
                    decision = SitePermissionDecision.ALLOW
                ),
                SitePermissionRecord(
                    host = "mic.example.com",
                    permission = SitePermission.MICROPHONE,
                    decision = SitePermissionDecision.BLOCK
                ),
                SitePermissionRecord(
                    host = "maps.example.com",
                    permission = SitePermission.LOCATION,
                    decision = SitePermissionDecision.ALLOW
                )
            ),
            settings.sitePermissionRecords().toSet()
        )

        settings.clearSitePermissionDecisions()

        assertTrue(settings.sitePermissionRecords().isEmpty())
        assertEquals(
            SitePermissionDecision.ASK,
            settings.sitePermissionDecision("camera.example.com", SitePermission.CAMERA)
        )
        assertEquals(
            SitePermissionDecision.ASK,
            settings.sitePermissionDecision("mic.example.com", SitePermission.MICROPHONE)
        )
        assertEquals(
            SitePermissionDecision.ASK,
            settings.sitePermissionDecision("maps.example.com", SitePermission.LOCATION)
        )
    }

    @Test
    fun sitePermissionRecords_followEffectiveDecisionWhenStoredSetsConflict() {
        val store = InMemoryPreferenceStore()
        store.putString("site_permission_camera_allowed_hosts", "camera.example.com")
        store.putString("site_permission_camera_blocked_hosts", "camera.example.com\nother.example.com")
        val settings = SettingsManager(store)

        assertEquals(
            setOf(
                SitePermissionRecord(
                    host = "camera.example.com",
                    permission = SitePermission.CAMERA,
                    decision = SitePermissionDecision.ALLOW
                ),
                SitePermissionRecord(
                    host = "other.example.com",
                    permission = SitePermission.CAMERA,
                    decision = SitePermissionDecision.BLOCK
                )
            ),
            settings.sitePermissionRecords().toSet()
        )
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
    fun customShortcuts_canBeRemoved() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        assertTrue(settings.addCustomShortcut("Video", "https://video.example.com"))
        assertTrue(settings.addCustomShortcut("Docs", "https://docs.example.com"))

        assertTrue(settings.removeCustomShortcut(CustomShortcut(" Video ", " https://video.example.com ")))
        assertFalse(settings.removeCustomShortcut(CustomShortcut("Missing", "https://missing.example.com")))

        assertEquals(
            listOf(CustomShortcut("Docs", "https://docs.example.com")),
            settings.customShortcuts()
        )
    }

    @Test
    fun customShortcuts_canBeUpdatedInPlace() {
        val settings = SettingsManager(InMemoryPreferenceStore())
        assertTrue(settings.addCustomShortcut("Video", "https://video.example.com"))
        assertTrue(settings.addCustomShortcut("Docs", "https://docs.example.com"))

        assertTrue(
            settings.updateCustomShortcut(
                CustomShortcut(" Video ", " https://video.example.com "),
                " Movies ",
                " https://movies.example.com "
            )
        )
        assertFalse(
            settings.updateCustomShortcut(
                CustomShortcut("Missing", "https://missing.example.com"),
                "Missing 2",
                "https://missing.example.com/2"
            )
        )
        assertFalse(
            settings.updateCustomShortcut(
                CustomShortcut("Docs", "https://docs.example.com"),
                "",
                "https://docs.example.com"
            )
        )

        assertEquals(
            listOf(
                CustomShortcut("Movies", "https://movies.example.com"),
                CustomShortcut("Docs", "https://docs.example.com")
            ),
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
