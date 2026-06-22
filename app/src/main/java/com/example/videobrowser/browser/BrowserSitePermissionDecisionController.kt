package com.example.videobrowser.browser

import com.example.videobrowser.settings.SessionSitePermissionStore
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SitePermission
import com.example.videobrowser.settings.SitePermissionDecision
import com.example.videobrowser.site.SiteHost

/**
 * 浏览器站点权限决策模块。
 *
 * WebView 相机/麦克风和定位权限都要遵守同一套持久允许、持久阻止和仅本次允许规则。
 * 这个类集中保存这套规则，避免每个权限控制器重复实现。
 */
class BrowserSitePermissionDecisionController(
    private val settingsManager: SettingsManager,
    private val sessionSitePermissionStore: SessionSitePermissionStore,
    private val isPrivateBrowsingEnabled: () -> Boolean
) {
    fun decisionForOrigin(
        origin: String?,
        permissions: Collection<SitePermission>
    ): SitePermissionDecision {
        val hostName = SiteHost.fromUrl(origin) ?: return SitePermissionDecision.ASK
        val distinctPermissions = permissions.distinct()
        if (distinctPermissions.isEmpty()) {
            return SitePermissionDecision.ASK
        }

        val persistentDecisions = distinctPermissions.associateWith { permission ->
            settingsManager.sitePermissionDecision(hostName, permission)
        }
        return when {
            persistentDecisions.values.any { decision -> decision == SitePermissionDecision.BLOCK } ->
                SitePermissionDecision.BLOCK

            distinctPermissions.all { permission ->
                persistentDecisions[permission] == SitePermissionDecision.ALLOW ||
                    sessionSitePermissionStore.isAllowed(hostName, permission)
            } -> SitePermissionDecision.ALLOW

            else -> SitePermissionDecision.ASK
        }
    }

    fun saveDecisionForOrigin(
        origin: String?,
        permissions: Collection<SitePermission>,
        allowed: Boolean
    ) {
        if (isPrivateBrowsingEnabled()) {
            if (allowed) {
                allowForSession(origin, permissions)
            }
            return
        }

        val hostName = SiteHost.fromUrl(origin) ?: return
        val decision = if (allowed) SitePermissionDecision.ALLOW else SitePermissionDecision.BLOCK
        permissions
            .distinct()
            .forEach { permission ->
                settingsManager.setSitePermissionDecision(hostName, permission, decision)
            }
    }

    fun allowForSession(
        origin: String?,
        permissions: Collection<SitePermission>
    ) {
        val hostName = SiteHost.fromUrl(origin) ?: return
        permissions
            .distinct()
            .forEach { permission ->
                sessionSitePermissionStore.allow(hostName, permission)
            }
    }
}
