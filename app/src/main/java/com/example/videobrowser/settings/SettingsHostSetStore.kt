package com.example.videobrowser.settings

import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.PreferenceStore

internal class SettingsHostSetStore(
    private val preferenceStore: PreferenceStore
) {
    fun load(key: String): Set<String> {
        return lineStore(key)
            .loadLines()
            .mapNotNull(SiteHost::normalize)
            .toSet()
    }

    fun save(key: String, hosts: Set<String>) {
        lineStore(key).saveLines(hosts.sorted())
    }

    private fun lineStore(key: String): PreferenceLineStore {
        return PreferenceLineStore(preferenceStore, key)
    }
}
