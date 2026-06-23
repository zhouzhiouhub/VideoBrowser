package com.example.videobrowser.utils

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

data class DialogAction(
    val title: String,
    val perform: () -> Unit
)

object ActionListDialog {
    fun show(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        actions: List<DialogAction>,
        @StringRes negativeButtonRes: Int? = null
    ) {
        show(
            activity = activity,
            title = activity.getString(titleRes),
            actions = actions,
            negativeButtonRes = negativeButtonRes
        )
    }

    fun show(
        activity: AppCompatActivity,
        title: String,
        actions: List<DialogAction>,
        @StringRes negativeButtonRes: Int? = null
    ) {
        val builder = AlertDialog.Builder(activity)
            .setTitle(title)
            .setItems(actions.map { action -> action.title }.toTypedArray()) { _, index ->
                actions.getOrNull(index)?.perform?.invoke()
            }
        if (negativeButtonRes != null) {
            builder.setNegativeButton(negativeButtonRes, null)
        }
        builder
            .show()
    }
}
