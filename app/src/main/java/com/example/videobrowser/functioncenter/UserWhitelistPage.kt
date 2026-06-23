package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 UserWhitelistPage 可以拆开理解为“User Whitelist Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.widget.Toast
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.ConfirmationDialog

class UserWhitelistPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val currentSiteHost: () -> String?,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    /**
     * 函数 `show`：控制 `show` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     */
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
                host.contentFactory.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_actions)
                ) { section ->
                    if (addableCurrentHost != null) {
                        host.contentFactory.addActionRow(
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
                        host.contentFactory.addActionRow(
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
                host.contentFactory.addEmptyState(content, activity.getString(R.string.dialog_user_whitelist_empty))
            } else {
                host.contentFactory.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_sites)
                ) { section ->
                    hosts.forEach { hostName ->
                        host.contentFactory.addActionRow(
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

    /**
     * 函数 `showRemoveUserWhitelistHostPage`：控制 `show Remove User Whitelist Host Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param hostName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    private fun showRemoveUserWhitelistHostPage(hostName: String) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.title_remove_user_whitelist,
            message = activity.getString(R.string.dialog_remove_user_whitelist_message, hostName),
            positiveButtonRes = R.string.action_remove
        ) {
            settingsManager.setUserWhitelistedSite(hostName, false)
            Toast.makeText(
                activity,
                activity.getString(R.string.toast_user_whitelist_removed, hostName),
                Toast.LENGTH_SHORT
            ).show()
            browserManager().reload()
            show(replaceCurrent = true)
        }
    }

    /**
     * 函数 `showClearUserWhitelistDialog`：控制 `show Clear User Whitelist Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearUserWhitelistDialog() {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            messageRes = R.string.dialog_clear_user_whitelist_message,
            positiveButtonRes = R.string.action_clear
        ) {
            settingsManager.clearUserWhitelistedSites()
            Toast.makeText(
                activity,
                R.string.toast_user_whitelist_cleared,
                Toast.LENGTH_SHORT
            ).show()
            browserManager().reload()
            show(replaceCurrent = true)
        }
    }
}
