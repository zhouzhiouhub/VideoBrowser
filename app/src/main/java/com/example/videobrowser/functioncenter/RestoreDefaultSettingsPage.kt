package com.example.videobrowser.functioncenter

import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R

class RestoreDefaultSettingsPage(
    private val host: FunctionCenterPageHost,
    private val restoreDefaultSettings: () -> Unit
) {
    private val activity = host.activity

    fun show() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_restore_default_settings)
            .setMessage(R.string.dialog_restore_default_settings_message)
            .setPositiveButton(R.string.action_restore) { _, _ -> restoreDefaultSettings() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
