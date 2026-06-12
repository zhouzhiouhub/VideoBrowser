package com.example.videobrowser.settings

import com.example.videobrowser.site.SiteHost

class SessionSitePermissionStore {
    private val grants = mutableSetOf<SessionSitePermissionGrant>()

    fun allow(host: String?, permission: SitePermission): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        grants += SessionSitePermissionGrant(normalizedHost, permission)
        return true
    }

    fun isAllowed(host: String?, permission: SitePermission): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        return SessionSitePermissionGrant(normalizedHost, permission) in grants
    }

    fun clear() {
        grants.clear()
    }

    private data class SessionSitePermissionGrant(
        val host: String,
        val permission: SitePermission
    )
}
