package com.example.videobrowser.functioncenter

import android.net.Uri
import android.widget.Toast
import com.example.videobrowser.R
import com.example.videobrowser.adblock.AdBlockLogAction
import com.example.videobrowser.adblock.AdBlockLogEntry
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdBlockLogPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: BrowserManager,
    private val adBlockLogger: AdBlockLogger,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show() {
        val entries = adBlockLogger.entries()
        if (entries.isEmpty()) {
            Toast.makeText(activity, R.string.toast_ad_block_log_empty, Toast.LENGTH_SHORT).show()
            return
        }

        host.showPage(
            title = activity.getString(R.string.title_ad_block_log),
            onBack = showRootPage
        ) { content ->
            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_ad_block_log_summary)
                ) {
                    adBlockLogger.clear()
                    Toast.makeText(
                        activity,
                        R.string.toast_ad_block_log_cleared,
                        Toast.LENGTH_SHORT
                    ).show()
                    showRootPage()
                }
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_records)
            ) { section ->
                entries.forEach { entry ->
                    val hostName = adBlockLogHost(entry)
                    val source = entry.ruleSource ?: entry.reason.name.lowercase(Locale.US)
                    val rule = entry.ruleId ?: entry.rulePattern ?: entry.reason.name
                    val rowTitle = "${adBlockLogTime(entry)} ${adBlockLogActionLabel(entry)} · $hostName"
                    val rowSummary = "$source  $rule"
                    val whitelistHost = entry.host?.takeIf { value -> value.isNotBlank() }
                    if (
                        entry.action == AdBlockLogAction.BLOCK &&
                        whitelistHost != null &&
                        !settingsManager.isUserWhitelistedSite(whitelistHost)
                    ) {
                        host.addActionRow(section, rowTitle, rowSummary) {
                            showAddWhitelistFromLogPage(whitelistHost)
                        }
                    } else {
                        host.addInfoRow(section, rowTitle, rowSummary)
                    }
                }
            }
        }
    }

    private fun adBlockLogTime(entry: AdBlockLogEntry): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(Date(entry.timestampMillis))
    }

    private fun adBlockLogActionLabel(entry: AdBlockLogEntry): String {
        return when (entry.action) {
            AdBlockLogAction.BLOCK -> activity.getString(R.string.ad_block_log_action_blocked)
            AdBlockLogAction.ALLOW -> activity.getString(R.string.ad_block_log_action_allowed)
        }
    }

    private fun adBlockLogHost(entry: AdBlockLogEntry): String {
        return entry.host ?: Uri.parse(entry.url).host ?: entry.url
    }

    private fun showAddWhitelistFromLogPage(hostName: String) {
        if (settingsManager.isUserWhitelistedSite(hostName)) {
            Toast.makeText(
                activity,
                activity.getString(R.string.toast_user_whitelist_already_added, hostName),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        host.showPage(
            title = activity.getString(R.string.title_add_user_whitelist),
            onBack = { show() }
        ) { content ->
            host.addFunctionMessage(
                content,
                activity.getString(R.string.dialog_add_user_whitelist_message, hostName)
            )
            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addFunctionActionButton(section, activity.getString(R.string.action_add)) {
                    settingsManager.setUserWhitelistedSite(hostName, true)
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.toast_user_whitelist_added, hostName),
                        Toast.LENGTH_SHORT
                    ).show()
                    browserManager.reload()
                    show()
                }
            }
        }
    }
}
