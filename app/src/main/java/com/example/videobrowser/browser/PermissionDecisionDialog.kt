package com.example.videobrowser.browser

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R

internal object PermissionDecisionDialog {
    fun create(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        message: String,
        onAllow: () -> Unit,
        onAllowOnce: () -> Unit,
        onDeny: () -> Unit
    ): AlertDialog {
        val dialog = AlertDialog.Builder(activity)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(R.string.action_allow) { _, _ -> onAllow() }
            .setNeutralButton(R.string.action_allow_once) { _, _ -> onAllowOnce() }
            .setNegativeButton(R.string.action_deny) { _, _ -> onDeny() }
            .create()
        dialog.setOnCancelListener {
            onDeny()
        }
        return dialog
    }
}
