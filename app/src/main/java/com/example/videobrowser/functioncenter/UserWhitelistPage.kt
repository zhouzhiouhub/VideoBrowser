package com.example.videobrowser.functioncenter

import android.graphics.Color
import android.widget.Toast
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager

class UserWhitelistPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: BrowserManager,
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
            if (currentHost != null && !settingsManager.isUserWhitelistedSite(currentHost)) {
                host.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_actions)
                ) { section ->
                    host.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_add_current_site),
                        summary = currentHost
                    ) {
                        settingsManager.setUserWhitelistedSite(currentHost, true)
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.toast_user_whitelist_added, currentHost),
                            Toast.LENGTH_SHORT
                        ).show()
                        browserManager.reload()
                        show()
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
        host.showPage(
            title = activity.getString(R.string.title_remove_user_whitelist),
            onBack = { show() }
        ) { content ->
            host.addFunctionMessage(
                content,
                activity.getString(R.string.dialog_remove_user_whitelist_message, hostName)
            )
            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addFunctionActionButton(
                    parent = section,
                    title = activity.getString(R.string.action_remove),
                    backgroundColor = Color.parseColor("#D92D20")
                ) {
                    settingsManager.setUserWhitelistedSite(hostName, false)
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.toast_user_whitelist_removed, hostName),
                        Toast.LENGTH_SHORT
                    ).show()
                    browserManager.reload()
                    show()
                }
            }
        }
    }
}
