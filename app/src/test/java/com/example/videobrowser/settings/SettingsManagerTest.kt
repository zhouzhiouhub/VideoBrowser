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
        assertFalse(settings.isDesktopModeEnabled())
        assertEquals(SettingsManager.DEFAULT_VIDEO_SPEED, settings.defaultVideoSpeed(), 0.001f)
        assertEquals(SettingsManager.DEFAULT_HOME_URL, settings.homeUrl())
        assertEquals(SettingsManager.DEFAULT_SEARCH_ENGINE_ID, settings.searchEngineId())
    }

    @Test
    fun settings_arePersistedThroughPreferenceStore() {
        val store = InMemoryPreferenceStore()
        val settings = SettingsManager(store)

        settings.setAdBlockEnabled(false)
        settings.setJsInjectionEnabled(false)
        settings.setDomAdBlockEnabled(false)
        settings.setVideoEnhancementEnabled(false)
        settings.setDesktopModeEnabled(true)
        settings.setDefaultVideoSpeed(1.5f)
        settings.setHomeUrl("https://m.sogou.com/")
        settings.setSearchEngineId("sogou")

        val reloaded = SettingsManager(store)
        assertFalse(reloaded.isAdBlockEnabled())
        assertFalse(reloaded.isJsInjectionEnabled())
        assertFalse(reloaded.isDomAdBlockEnabled())
        assertFalse(reloaded.isVideoEnhancementEnabled())
        assertTrue(reloaded.isDesktopModeEnabled())
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
        settings.setJsInjectionEnabled(false)
        settings.setHomeUrl("https://m.sogou.com/")
        store.putString("bookmarks", "[]")

        assertTrue(settings.restoreDefaults())

        assertTrue(settings.isAdBlockEnabled())
        assertFalse(settings.isAdBlockDisabledForSite("video.example.com"))
        assertTrue(settings.isJsInjectionEnabled())
        assertEquals(SettingsManager.DEFAULT_HOME_URL, settings.homeUrl())
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
