package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import com.example.videobrowser.R
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SitePermission
import com.example.videobrowser.settings.SitePermissionDecision
import com.example.videobrowser.utils.ShortToast

internal class CurrentSitePermissionSection(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val currentSiteHost: () -> String?,
    private val onPermissionChanged: () -> Unit
) {
    private val activity = host.activity
    private val sitePermissionTextFormatter = SitePermissionTextFormatter(activity)

    fun addRows(section: LinearLayout, siteHost: String?, hasSite: Boolean) {
        addSitePermissionRow(section, siteHost, hasSite, SitePermission.CAMERA)
        addSitePermissionRow(section, siteHost, hasSite, SitePermission.MICROPHONE)
        addSitePermissionRow(section, siteHost, hasSite, SitePermission.LOCATION)
    }

    fun title(permission: SitePermission): String {
        return sitePermissionTextFormatter.title(permission)
    }

    fun summary(siteHost: String?, permission: SitePermission): String {
        return sitePermissionTextFormatter.currentSitePermissionSummary(
            siteHost = siteHost,
            decision = siteHost?.let { hostName ->
                settingsManager.sitePermissionDecision(hostName, permission)
            }
        )
    }

    private fun addSitePermissionRow(
        section: LinearLayout,
        siteHost: String?,
        hasSite: Boolean,
        permission: SitePermission
    ) {
        host.contentFactory.addActionRow(
            parent = section,
            title = title(permission),
            summary = summary(siteHost, permission),
            enabled = hasSite
        ) {
            showSitePermissionDialog(permission)
        }
    }

    private fun showSitePermissionDialog(permission: SitePermission) {
        val hostName = currentSiteHost() ?: return
        val decisions = SitePermissionDecision.entries
        val labels = decisions
            .map { decision -> sitePermissionTextFormatter.decisionText(decision) }
        val checkedIndex = decisions.indexOf(settingsManager.sitePermissionDecision(hostName, permission))

        SingleChoiceDialog.show(
            activity = activity,
            title = title(permission),
            labels = labels,
            checkedIndex = checkedIndex
        ) { index ->
            val decision = decisions[index]
            settingsManager.setSitePermissionDecision(hostName, permission, decision)
            ShortToast.show(
                activity,
                activity.getString(
                    R.string.toast_site_permission_updated,
                    title(permission),
                    hostName
                )
            )
            onPermissionChanged()
        }
    }
}
