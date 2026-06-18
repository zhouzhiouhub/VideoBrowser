package com.example.videobrowser.settings

import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.PreferenceStore

internal class SiteFeatureHostSettings(
    private val preferenceStore: PreferenceStore,
    private val hostSets: SettingsHostSetStore
) {
    fun contains(key: String, host: String?): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return hosts(key).contains(normalizedHost)
    }

    fun set(key: String, host: String?, enabled: Boolean): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val hosts = hosts(key).toMutableSet()
        if (enabled) {
            hosts.add(normalizedHost)
        } else {
            hosts.remove(normalizedHost)
        }
        hostSets.save(key, hosts)
        return true
    }

    fun hosts(key: String): Set<String> {
        return hostSets.load(key)
    }

    fun clear(key: String) {
        preferenceStore.remove(key)
    }
}
