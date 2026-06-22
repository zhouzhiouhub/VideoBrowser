package com.example.videobrowser.settings

import com.example.videobrowser.storage.PreferenceStore

internal class SettingsPreferenceResetter(
    private val preferenceStore: PreferenceStore
) {
    fun restoreDefaults(): Boolean {
        return preferenceStore.remove(RESET_KEYS, commit = true)
    }
}
