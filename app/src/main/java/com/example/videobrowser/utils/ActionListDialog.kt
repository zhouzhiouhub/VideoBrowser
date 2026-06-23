package com.example.videobrowser.utils

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

data class DialogAction(
    val title: String,
    val perform: () -> Unit
)

data class DialogButtonAction(
    @param:StringRes val titleRes: Int,
    val perform: () -> Unit
)

object ActionListDialog {
    fun show(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        actions: List<DialogAction>,
        @StringRes negativeButtonRes: Int? = null,
        positiveButton: DialogButtonAction? = null,
        neutralButton: DialogButtonAction? = null
    ) {
        show(
            activity = activity,
            title = activity.getString(titleRes),
            actions = actions,
            negativeButtonRes = negativeButtonRes,
            positiveButton = positiveButton,
            neutralButton = neutralButton
        )
    }

    fun show(
        activity: AppCompatActivity,
        title: String,
        actions: List<DialogAction>,
        @StringRes negativeButtonRes: Int? = null,
        positiveButton: DialogButtonAction? = null,
        neutralButton: DialogButtonAction? = null
    ) {
        val builder = AlertDialog.Builder(activity)
            .setTitle(title)
            .setItems(actions.map { action -> action.title }.toTypedArray()) { _, index ->
                actions.getOrNull(index)?.perform?.invoke()
            }
        if (positiveButton != null) {
            builder.setPositiveButton(positiveButton.titleRes) { _, _ -> positiveButton.perform() }
        }
        if (neutralButton != null) {
            builder.setNeutralButton(neutralButton.titleRes) { _, _ -> neutralButton.perform() }
        }
        if (negativeButtonRes != null) {
            builder.setNegativeButton(negativeButtonRes, null)
        }
        builder
            .show()
    }
}
