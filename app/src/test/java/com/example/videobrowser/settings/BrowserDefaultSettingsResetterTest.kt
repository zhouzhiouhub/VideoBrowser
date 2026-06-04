package com.example.videobrowser.settings

import com.example.videobrowser.rules.RuleFileLoader
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
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
        val savedPageRepository = SavedPageRepository(store)
        val filesDir = temporaryFolder.newFolder()
        val rulesDirectory = filesDir.resolve("rules").apply { mkdirs() }
        rulesDirectory.resolve(RuleFileLoader.REQUEST_RULES_CACHE_FILE).writeText("/blocked-url/")

        settingsManager.setAdBlockEnabled(false)
        settingsManager.addUserElementHideSelectorForSite("video.example.com", ".blocked")
        settingsManager.addCustomShortcut("Docs", "https://docs.example.com/")
        store.putString(SavedPageCollection.BOOKMARKS.key, "bookmarks")
        store.putString(SavedPageCollection.HISTORY.key, "history")

        val resetter = BrowserDefaultSettingsResetter(
            settingsManager = settingsManager,
            savedPageRepository = savedPageRepository,
            filesDir = filesDir
        )

        assertTrue(resetter.restoreDefaults())

        assertTrue(settingsManager.isAdBlockEnabled())
        assertTrue(settingsManager.userElementHideRules().isEmpty())
        assertTrue(settingsManager.customShortcuts().isEmpty())
        assertFalse(store.contains(SavedPageCollection.BOOKMARKS.key))
        assertFalse(store.contains(SavedPageCollection.HISTORY.key))
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
