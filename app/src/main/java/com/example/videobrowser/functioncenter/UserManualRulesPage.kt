package com.example.videobrowser.functioncenter

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

    fun show(replaceCurrent: Boolean = false) {
        val rules = settingsManager.userElementHideRules()
            .sortedWith(compareBy<UserElementHideRule> { it.host }.thenBy { it.selector })

        host.showPage(
            title = activity.getString(R.string.title_user_manual_rules),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (rules.isEmpty()) {
                host.addEmptyState(content, activity.getString(R.string.dialog_user_manual_rules_empty))
                return@showPage
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_user_manual_rules_summary)
                ) {
                    showClearUserManualRulesDialog()
                }
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_records)
            ) { section ->
                rules.forEach { rule ->
                    host.addActionRow(
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
