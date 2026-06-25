package com.example.videobrowser.settings

import com.example.videobrowser.storage.PreferenceStore

internal class RemovedSearchProviderStore(
    preferenceStore: PreferenceStore
) {
    private val lineStore = PreferenceLineStore(preferenceStore, KEY_REMOVED_SEARCH_PROVIDERS)

    fun load(): Set<String> {
        return lineStore.loadLines()
            .mapNotNull(::normalizeId)
            .toSet()
    }

    fun add(id: String): Boolean {
        val normalizedId = normalizeId(id) ?: return false
        lineStore.saveLines(load() + normalizedId)
        return true
    }

    private fun normalizeId(id: String): String? {
        return id.trim()
            .takeIf { value -> value.isNotEmpty() }
            ?.takeIf { value -> SEARCH_PROVIDER_ID_REGEX.matches(value) }
    }

    private companion object {
        private val SEARCH_PROVIDER_ID_REGEX = Regex("[A-Za-z0-9._-]{1,64}")
    }
}
