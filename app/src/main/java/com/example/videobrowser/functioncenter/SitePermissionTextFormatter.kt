package com.example.videobrowser.functioncenter

import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.settings.SitePermission
import com.example.videobrowser.settings.SitePermissionDecision
import com.example.videobrowser.settings.SitePermissionRecord

class SitePermissionTextFormatter(
    private val activity: AppCompatActivity
) {
    fun title(permission: SitePermission): String {
        return activity.getString(
            when (permission) {
                SitePermission.CAMERA -> R.string.setting_site_permission_camera
                SitePermission.MICROPHONE -> R.string.setting_site_permission_microphone
                SitePermission.LOCATION -> R.string.setting_site_permission_location
            }
        )
    }

    fun decisionText(decision: SitePermissionDecision): String {
        return activity.getString(
            when (decision) {
                SitePermissionDecision.ASK -> R.string.site_permission_ask
                SitePermissionDecision.ALLOW -> R.string.site_permission_allowed
                SitePermissionDecision.BLOCK -> R.string.site_permission_blocked
            }
        )
    }

    fun recordSummary(record: SitePermissionRecord): String {
        return listOf(
            title(record.permission),
            decisionText(record.decision)
        ).joinToString(separator = " | ")
    }

    fun currentSitePermissionSummary(siteHost: String?, decision: SitePermissionDecision?): String {
        return if (siteHost == null) {
            activity.getString(R.string.function_center_site_action_unavailable)
        } else {
            decision?.let(::decisionText)
                ?: activity.getString(R.string.function_center_site_action_unavailable)
        }
    }
}
