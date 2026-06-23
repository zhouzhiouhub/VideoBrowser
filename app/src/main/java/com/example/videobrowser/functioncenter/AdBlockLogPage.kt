package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 AdBlockLogPage 可以拆开理解为“Ad Block Log Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.widget.Toast
import com.example.videobrowser.R
import com.example.videobrowser.adblock.AdBlockLogEntryFormatter
import com.example.videobrowser.adblock.AdBlockLogAction
import com.example.videobrowser.adblock.AdBlockLogEntry
import com.example.videobrowser.adblock.AdBlockLogger
import com.example.videobrowser.adblock.AdBlockLogRecoveryActionType
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.utils.ConfirmationDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdBlockLogPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val adBlockLogger: AdBlockLogger,
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
        val entries = adBlockLogger.entries()

        host.showPage(
            title = activity.getString(R.string.title_ad_block_log),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (entries.isEmpty()) {
                host.contentFactory.addEmptyState(content, activity.getString(R.string.toast_ad_block_log_empty))
                return@showPage
            }

            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_ad_block_log_summary)
                ) {
                    showClearAdBlockLogDialog()
                }
            }

            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_records)
            ) { section ->
                entries.forEach { entry ->
                    val hostName = adBlockLogHost(entry)
                    val rowTitle = "${adBlockLogTime(entry)} ${adBlockLogActionLabel(entry)} · $hostName"
                    val rowSummary = AdBlockLogEntryFormatter.summary(entry)
                    when (
                        val recoveryAction = AdBlockLogEntryFormatter.recoveryActionFor(
                            entry = entry,
                            isUserWhitelisted = { hostName ->
                                settingsManager.isUserWhitelistedSite(hostName)
                            },
                            isAdBlockDisabledForSite = { hostName ->
                                settingsManager.isAdBlockDisabledForSite(hostName)
                            }
                        )
                    ) {
                        null -> host.contentFactory.addInfoRow(section, rowTitle, rowSummary)
                        else -> host.contentFactory.addActionRow(section, rowTitle, rowSummary) {
                            when (recoveryAction.type) {
                                AdBlockLogRecoveryActionType.ADD_TO_USER_WHITELIST -> {
                                    showAddWhitelistFromLogDialog(recoveryAction.host)
                                }
                                AdBlockLogRecoveryActionType.RESTORE_SITE_AD_BLOCK -> {
                                    showRestoreSiteAdBlockFromLogDialog(recoveryAction.host)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 函数 `adBlockLogTime`：封装 `ad Block Log Time` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param entry 参数类型为 `AdBlockLogEntry`，表示函数执行 `entry` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun adBlockLogTime(entry: AdBlockLogEntry): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(Date(entry.timestampMillis))
    }

    /**
     * 函数 `adBlockLogActionLabel`：封装 `ad Block Log Action Label` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param entry 参数类型为 `AdBlockLogEntry`，表示函数执行 `entry` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun adBlockLogActionLabel(entry: AdBlockLogEntry): String {
        return when (entry.action) {
            AdBlockLogAction.BLOCK -> activity.getString(R.string.ad_block_log_action_blocked)
            AdBlockLogAction.ALLOW -> activity.getString(R.string.ad_block_log_action_allowed)
        }
    }

    /**
     * 函数 `adBlockLogHost`：封装 `ad Block Log Host` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param entry 参数类型为 `AdBlockLogEntry`，表示函数执行 `entry` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun adBlockLogHost(entry: AdBlockLogEntry): String {
        return entry.host ?: SiteHost.fromUrl(entry.url) ?: entry.url
    }

    /**
     * 函数 `showClearAdBlockLogDialog`：控制 `show Clear Ad Block Log Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearAdBlockLogDialog() {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            messageRes = R.string.dialog_clear_ad_block_log_message,
            positiveButtonRes = R.string.action_clear
        ) {
            adBlockLogger.clear()
            Toast.makeText(
                activity,
                R.string.toast_ad_block_log_cleared,
                Toast.LENGTH_SHORT
            ).show()
            show(replaceCurrent = true)
        }
    }

    /**
     * 函数 `showAddWhitelistFromLogDialog`：控制 `show Add Whitelist From Log Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param hostName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    private fun showAddWhitelistFromLogDialog(hostName: String) {
        if (settingsManager.isUserWhitelistedSite(hostName)) {
            Toast.makeText(
                activity,
                activity.getString(R.string.toast_user_whitelist_already_added, hostName),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.title_add_user_whitelist,
            message = activity.getString(R.string.dialog_add_user_whitelist_message, hostName),
            positiveButtonRes = R.string.action_add
        ) {
            settingsManager.setUserWhitelistedSite(hostName, true)
            Toast.makeText(
                activity,
                activity.getString(R.string.toast_user_whitelist_added, hostName),
                Toast.LENGTH_SHORT
            ).show()
            browserManager().reload()
            show(replaceCurrent = true)
        }
    }

    /**
     * 函数 `showRestoreSiteAdBlockFromLogDialog`：控制 `show Restore Site Ad Block From Log Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param hostName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    private fun showRestoreSiteAdBlockFromLogDialog(hostName: String) {
        if (!settingsManager.isAdBlockDisabledForSite(hostName)) {
            Toast.makeText(
                activity,
                activity.getString(R.string.toast_current_site_ad_block_restored, hostName),
                Toast.LENGTH_SHORT
            ).show()
            show(replaceCurrent = true)
            return
        }

        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.setting_current_site_ad_block,
            message = activity.getString(R.string.dialog_restore_site_ad_block_message, hostName),
            positiveButtonRes = R.string.action_restore
        ) {
            settingsManager.setAdBlockDisabledForSite(hostName, false)
            Toast.makeText(
                activity,
                activity.getString(R.string.toast_current_site_ad_block_restored, hostName),
                Toast.LENGTH_SHORT
            ).show()
            browserManager().reload()
            show(replaceCurrent = true)
        }
    }
}
