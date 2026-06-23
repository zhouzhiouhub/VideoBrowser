package com.example.videobrowser.settings

import com.example.videobrowser.storage.PreferenceStore

internal class PreferenceLineStore(
    private val preferenceStore: PreferenceStore,
    private val key: String
) {
    fun loadLines(): Sequence<String> {
        return preferenceStore.getString(key, null)?.lineSequence() ?: emptySequence()
    }

    fun saveLines(lines: Collection<String>) {
        if (lines.isEmpty()) {
            clear()
        } else {
            preferenceStore.putString(key, lines.joinToString(separator = "\n"))
        }
    }

    fun clear() {
        preferenceStore.remove(key)
    }
}
