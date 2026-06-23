package com.example.videobrowser.utils

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

object ConfirmationDialog {
    fun show(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        @StringRes messageRes: Int,
        @StringRes positiveButtonRes: Int,
        onConfirmed: () -> Unit
    ) {
        show(
            activity = activity,
            titleRes = titleRes,
            message = activity.getString(messageRes),
            positiveButtonRes = positiveButtonRes,
            onConfirmed = onConfirmed
        )
    }

    fun show(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        message: String,
        @StringRes positiveButtonRes: Int,
        onConfirmed: () -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(positiveButtonRes) { _, _ -> onConfirmed() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
