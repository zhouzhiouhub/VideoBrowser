package com.example.videobrowser.functioncenter

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager

class UserWhitelistPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val currentSiteHost: () -> String?,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show() {
        val hosts = settingsManager.userWhitelistedSiteHosts().sorted()
        val currentHost = currentSiteHost()

        host.showPage(
            title = activity.getString(R.string.title_user_whitelist),
            onBack = showRootPage
        ) { content ->
            val addableCurrentHost = currentHost
                ?.takeIf { hostName -> !settingsManager.isUserWhitelistedSite(hostName) }
            if (addableCurrentHost != null || hosts.isNotEmpty()) {
                host.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_actions)
                ) { section ->
                    if (addableCurrentHost != null) {
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_add_current_site),
                            summary = addableCurrentHost
                        ) {
                            settingsManager.setUserWhitelistedSite(addableCurrentHost, true)
                            Toast.makeText(
                                activity,
                                activity.getString(
                                    R.string.toast_user_whitelist_added,
                                    addableCurrentHost
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                            browserManager().reload()
                            show()
                        }
                    }
                    if (hosts.isNotEmpty()) {
                        host.addActionRow(
                            parent = section,
                            title = activity.getString(R.string.action_clear),
                            summary = activity.getString(R.string.action_clear_user_whitelist_summary)
                        ) {
                            showClearUserWhitelistDialog()
                        }
                    }
                }
            }

            if (hosts.isEmpty()) {
                host.addEmptyState(content, activity.getString(R.string.dialog_user_whitelist_empty))
            } else {
                host.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_sites)
                ) { section ->
                    hosts.forEach { hostName ->
                        host.addActionRow(
                            parent = section,
                            title = hostName,
                            summary = activity.getString(R.string.user_whitelist_host_summary)
                        ) {
                            showRemoveUserWhitelistHostPage(hostName)
                        }
                    }
                }
            }
        }
    }

    private fun showRemoveUserWhitelistHostPage(hostName: String) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_user_whitelist)
            .setMessage(activity.getString(R.string.dialog_remove_user_whitelist_message, hostName))
            .setPositiveButton(R.string.action_remove) { _, _ ->
                settingsManager.setUserWhitelistedSite(hostName, false)
                Toast.makeText(
                    activity,
                    activity.getString(R.string.toast_user_whitelist_removed, hostName),
                    Toast.LENGTH_SHORT
                ).show()
                browserManager().reload()
                show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showClearUserWhitelistDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_user_whitelist_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                settingsManager.clearUserWhitelistedSites()
                Toast.makeText(
                    activity,
                    R.string.toast_user_whitelist_cleared,
                    Toast.LENGTH_SHORT
                ).show()
                browserManager().reload()
                show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
