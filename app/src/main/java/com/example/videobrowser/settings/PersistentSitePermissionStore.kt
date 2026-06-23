package com.example.videobrowser.settings

import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.PreferenceStore

internal class PersistentSitePermissionStore(
    private val preferenceStore: PreferenceStore,
    private val hostSets: SettingsHostSetStore
) {
    fun decision(host: String?, permission: SitePermission): SitePermissionDecision {
        val normalizedHost = SiteHost.normalize(host) ?: return SitePermissionDecision.ASK
        return when {
            allowedHosts(permission).contains(normalizedHost) -> SitePermissionDecision.ALLOW
            blockedHosts(permission).contains(normalizedHost) -> SitePermissionDecision.BLOCK
            else -> SitePermissionDecision.ASK
        }
    }

    fun setDecision(
        host: String?,
        permission: SitePermission,
        decision: SitePermissionDecision
    ): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val allowedHosts = allowedHosts(permission).toMutableSet()
        val blockedHosts = blockedHosts(permission).toMutableSet()

        allowedHosts.remove(normalizedHost)
        blockedHosts.remove(normalizedHost)
        when (decision) {
            SitePermissionDecision.ALLOW -> allowedHosts.add(normalizedHost)
            SitePermissionDecision.BLOCK -> blockedHosts.add(normalizedHost)
            SitePermissionDecision.ASK -> Unit
        }

        val keys = hostKeys(permission)
        hostSets.save(keys.allowed, allowedHosts)
        hostSets.save(keys.blocked, blockedHosts)
        return true
    }

    fun allowedHosts(permission: SitePermission): Set<String> {
        return hostSets.load(hostKeys(permission).allowed)
    }

    fun blockedHosts(permission: SitePermission): Set<String> {
        return hostSets.load(hostKeys(permission).blocked)
    }

    fun records(): List<SitePermissionRecord> {
        return SitePermission.entries.flatMap { permission ->
            val allowedHosts = allowedHosts(permission)
            val blockedHosts = blockedHosts(permission)
            allowedHosts.map { host ->
                SitePermissionRecord(
                    host = host,
                    permission = permission,
                    decision = SitePermissionDecision.ALLOW
                )
            } + blockedHosts
                .filterNot { host -> host in allowedHosts }
                .map { host ->
                    SitePermissionRecord(
                        host = host,
                        permission = permission,
                        decision = SitePermissionDecision.BLOCK
                    )
                }
        }.distinct()
    }

    fun clear() {
        SitePermission.entries.forEach { permission ->
            hostKeys(permission).all.forEach { key ->
                preferenceStore.remove(key)
            }
        }
    }

    private fun hostKeys(permission: SitePermission): SitePermissionHostKeys {
        return hostKeysByPermission.getValue(permission)
    }

    private data class SitePermissionHostKeys(
        val allowed: String,
        val blocked: String
    ) {
        val all: List<String>
            get() = listOf(allowed, blocked)
    }

    private companion object {
        private val hostKeysByPermission = SitePermission.entries.associateWith { permission ->
            when (permission) {
                SitePermission.CAMERA -> SitePermissionHostKeys(
                    allowed = KEY_SITE_PERMISSION_CAMERA_ALLOWED_HOSTS,
                    blocked = KEY_SITE_PERMISSION_CAMERA_BLOCKED_HOSTS
                )

                SitePermission.MICROPHONE -> SitePermissionHostKeys(
                    allowed = KEY_SITE_PERMISSION_MICROPHONE_ALLOWED_HOSTS,
                    blocked = KEY_SITE_PERMISSION_MICROPHONE_BLOCKED_HOSTS
                )

                SitePermission.LOCATION -> SitePermissionHostKeys(
                    allowed = KEY_SITE_PERMISSION_LOCATION_ALLOWED_HOSTS,
                    blocked = KEY_SITE_PERMISSION_LOCATION_BLOCKED_HOSTS
                )
            }
        }
    }
}
