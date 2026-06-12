package com.example.videobrowser.functioncenter

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SitePermission
import com.example.videobrowser.settings.SitePermissionDecision
import com.example.videobrowser.settings.SitePermissionRecord

class SitePermissionsPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show(replaceCurrent: Boolean = false) {
        val records = settingsManager.sitePermissionRecords()
            .sortedWith(
                compareBy<SitePermissionRecord> { record -> record.host }
                    .thenBy { record -> record.permission.name }
                    .thenBy { record -> record.decision.name }
            )

        host.showPage(
            title = activity.getString(R.string.title_site_permissions),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (records.isEmpty()) {
                host.addEmptyState(content, activity.getString(R.string.dialog_site_permissions_empty))
                return@showPage
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_site_permissions_summary)
                ) {
                    showClearSitePermissionsDialog()
                }
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_records)
            ) { section ->
                records.forEach { record ->
                    host.addActionRow(
                        parent = section,
                        title = record.host,
                        summary = sitePermissionRecordSummary(record)
                    ) {
                        showRemoveSitePermissionDialog(record)
                    }
                }
            }
        }
    }

    private fun showRemoveSitePermissionDialog(record: SitePermissionRecord) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_site_permission)
            .setMessage(
                activity.getString(
                    R.string.dialog_remove_site_permission_message,
                    record.host,
                    sitePermissionTitle(record.permission),
                    sitePermissionDecisionText(record.decision)
                )
            )
            .setPositiveButton(R.string.action_remove) { _, _ ->
                settingsManager.setSitePermissionDecision(
                    record.host,
                    record.permission,
                    SitePermissionDecision.ASK
                )
                Toast.makeText(
                    activity,
                    R.string.toast_site_permission_removed,
                    Toast.LENGTH_SHORT
                ).show()
                show(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showClearSitePermissionsDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_site_permissions_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                settingsManager.clearSitePermissionDecisions()
                Toast.makeText(
                    activity,
                    R.string.toast_site_permissions_cleared,
                    Toast.LENGTH_SHORT
                ).show()
                show(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun sitePermissionRecordSummary(record: SitePermissionRecord): String {
        return listOf(
            sitePermissionTitle(record.permission),
            sitePermissionDecisionText(record.decision)
        ).joinToString(separator = " | ")
    }

    private fun sitePermissionTitle(permission: SitePermission): String {
        return activity.getString(
            when (permission) {
                SitePermission.CAMERA -> R.string.setting_site_permission_camera
                SitePermission.MICROPHONE -> R.string.setting_site_permission_microphone
                SitePermission.LOCATION -> R.string.setting_site_permission_location
            }
        )
    }

    private fun sitePermissionDecisionText(decision: SitePermissionDecision): String {
        return activity.getString(
            when (decision) {
                SitePermissionDecision.ASK -> R.string.site_permission_ask
                SitePermissionDecision.ALLOW -> R.string.site_permission_allowed
                SitePermissionDecision.BLOCK -> R.string.site_permission_blocked
            }
        )
    }
}
