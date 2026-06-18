package com.example.videobrowser.settings

import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.PreferenceStore

internal class SettingsHostSetStore(
    private val preferenceStore: PreferenceStore
) {
    fun load(key: String): Set<String> {
        return preferenceStore.getString(key, null)
            ?.lineSequence()
            ?.mapNotNull(SiteHost::normalize)
            ?.toSet()
            ?: emptySet()
    }

    fun save(key: String, hosts: Set<String>) {
        if (hosts.isEmpty()) {
            preferenceStore.remove(key)
        } else {
            preferenceStore.putString(
                key,
                hosts.sorted().joinToString(separator = "\n")
            )
        }
    }
}
