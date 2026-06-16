package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 UserWhitelistPage 可以拆开理解为“User Whitelist Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
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

    fun show(replaceCurrent: Boolean = false) {
        val hosts = settingsManager.userWhitelistedSiteHosts().sorted()
        val currentHost = currentSiteHost()

        host.showPage(
            title = activity.getString(R.string.title_user_whitelist),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
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
                            show(replaceCurrent = true)
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
                            summary = ""
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
                show(replaceCurrent = true)
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
                show(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
