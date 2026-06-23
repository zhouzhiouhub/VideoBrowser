package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 UserManualRulesPage 可以拆开理解为“User Manual Rules Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.UserElementHideRule

class UserManualRulesPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
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
        val rules = settingsManager.userElementHideRules()
            .sortedWith(compareBy<UserElementHideRule> { it.host }.thenBy { it.selector })

        host.showPage(
            title = activity.getString(R.string.title_user_manual_rules),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (rules.isEmpty()) {
                host.contentFactory.addEmptyState(content, activity.getString(R.string.dialog_user_manual_rules_empty))
                return@showPage
            }

            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_user_manual_rules_summary)
                ) {
                    showClearUserManualRulesDialog()
                }
            }

            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_records)
            ) { section ->
                rules.forEach { rule ->
                    host.contentFactory.addActionRow(
                        parent = section,
                        title = rule.host,
                        summary = rule.selector
                    ) {
                        showRemoveUserManualRuleDialog(rule)
                    }
                }
            }
        }
    }

    /**
     * 函数 `showRemoveUserManualRuleDialog`：控制 `show Remove User Manual Rule Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `UserElementHideRule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     */
    private fun showRemoveUserManualRuleDialog(rule: UserElementHideRule) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_user_manual_rule)
            .setMessage(
                activity.getString(
                    R.string.dialog_remove_user_manual_rule_message,
                    rule.host,
                    rule.selector
                )
            )
            .setPositiveButton(R.string.action_remove) { _, _ ->
                if (settingsManager.removeUserElementHideRule(rule)) {
                    Toast.makeText(
                        activity,
                        R.string.toast_user_manual_rule_removed,
                        Toast.LENGTH_SHORT
                    ).show()
                    browserManager().reload()
                }
                show(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showClearUserManualRulesDialog`：控制 `show Clear User Manual Rules Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearUserManualRulesDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_user_manual_rules_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                settingsManager.clearUserElementHideRules()
                Toast.makeText(
                    activity,
                    R.string.toast_user_manual_rules_cleared,
                    Toast.LENGTH_SHORT
                ).show()
                browserManager().reload()
                show(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
