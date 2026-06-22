package com.example.videobrowser.settings

import com.example.videobrowser.testutil.InMemoryPreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsPreferenceResetterTest {
    @Test
    fun restoreDefaults_removesOnlySettingsPreferenceKeys() {
        val store = InMemoryPreferenceStore()
        RESET_KEYS.forEach { key -> store.putString(key, "custom") }
        store.putString(NON_SETTINGS_KEY, "keep")

        assertTrue(SettingsPreferenceResetter(store).restoreDefaults())

        RESET_KEYS.forEach { key ->
            assertFalse("Expected $key to be removed", store.contains(key))
        }
        assertEquals("keep", store.getString(NON_SETTINGS_KEY))
    }

    private companion object {
        const val NON_SETTINGS_KEY = "bookmarks"
    }
}
