package com.example.videobrowser.functioncenter

import com.example.videobrowser.R

class RestoreDefaultSettingsPage(
    private val host: FunctionCenterPageHost,
    private val restoreDefaultSettings: () -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show() {
        host.showPage(
            title = activity.getString(R.string.action_restore_default_settings),
            onBack = showRootPage
        ) { content ->
            host.addFunctionMessage(
                content,
                activity.getString(R.string.dialog_restore_default_settings_message)
            )
            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addFunctionActionButton(section, activity.getString(R.string.action_restore)) {
                    restoreDefaultSettings()
                }
            }
        }
    }
}
