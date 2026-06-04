package com.example.videobrowser.settings

import com.example.videobrowser.rules.RuleFileLoader
import com.example.videobrowser.storage.PreferenceStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BrowserDefaultSettingsResetterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun restoreDefaults_clearsSettingsAndCachedRules() {
        val store = InMemoryPreferenceStore()
        val settingsManager = SettingsManager(store)
        val filesDir = temporaryFolder.newFolder()
        val rulesDirectory = filesDir.resolve("rules").apply { mkdirs() }
        rulesDirectory.resolve(RuleFileLoader.REQUEST_RULES_CACHE_FILE).writeText("/blocked-url/")

        settingsManager.setAdBlockEnabled(false)
        settingsManager.addUserElementHideSelectorForSite("video.example.com", ".blocked")

        val resetter = BrowserDefaultSettingsResetter(
            settingsManager = settingsManager,
            filesDir = filesDir
        )

        assertTrue(resetter.restoreDefaults())

        assertTrue(settingsManager.isAdBlockEnabled())
        assertTrue(settingsManager.userElementHideRules().isEmpty())
        assertFalse(rulesDirectory.exists())
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
