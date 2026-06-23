package com.example.videobrowser.utils

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

object ConfirmationDialog {
    fun create(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        @StringRes messageRes: Int,
        @StringRes positiveButtonRes: Int,
        @StringRes negativeButtonRes: Int? = android.R.string.cancel,
        onCanceled: (() -> Unit)? = null,
        onConfirmed: () -> Unit
    ): AlertDialog {
        return create(
            activity = activity,
            titleRes = titleRes,
            message = activity.getString(messageRes),
            positiveButtonRes = positiveButtonRes,
            negativeButtonRes = negativeButtonRes,
            onCanceled = onCanceled,
            onConfirmed = onConfirmed
        )
    }

    fun show(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        @StringRes messageRes: Int,
        @StringRes positiveButtonRes: Int,
        onConfirmed: () -> Unit
    ): AlertDialog {
        val dialog = create(
            activity = activity,
            titleRes = titleRes,
            message = activity.getString(messageRes),
            positiveButtonRes = positiveButtonRes,
            onConfirmed = onConfirmed
        )
        dialog.show()
        return dialog
    }

    fun create(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        message: String,
        @StringRes positiveButtonRes: Int,
        @StringRes negativeButtonRes: Int? = android.R.string.cancel,
        onCanceled: (() -> Unit)? = null,
        onConfirmed: () -> Unit
    ): AlertDialog {
        val builder = AppDialog.builder(activity)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(positiveButtonRes) { _, _ -> onConfirmed() }
        if (negativeButtonRes != null) {
            if (onCanceled == null) {
                builder.setNegativeButton(negativeButtonRes, null)
            } else {
                builder.setNegativeButton(negativeButtonRes) { _, _ -> onCanceled() }
            }
        }
        val dialog = builder.create()
        if (onCanceled != null) {
            dialog.setOnCancelListener {
                onCanceled()
            }
        }
        return dialog
    }

    fun show(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        message: String,
        @StringRes positiveButtonRes: Int,
        onConfirmed: () -> Unit
    ): AlertDialog {
        val dialog = create(
            activity = activity,
            titleRes = titleRes,
            message = message,
            positiveButtonRes = positiveButtonRes,
            onConfirmed = onConfirmed
        )
        dialog.show()
        return dialog
    }
}
