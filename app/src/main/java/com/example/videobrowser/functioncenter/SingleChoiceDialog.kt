package com.example.videobrowser.functioncenter

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

internal object SingleChoiceDialog {
    fun show(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        labels: List<String>,
        checkedIndex: Int,
        onSelected: (Int) -> Unit
    ) {
        show(
            activity = activity,
            title = activity.getString(titleRes),
            labels = labels,
            checkedIndex = checkedIndex,
            onSelected = onSelected
        )
    }

    fun show(
        activity: AppCompatActivity,
        title: String,
        labels: List<String>,
        checkedIndex: Int,
        onSelected: (Int) -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setSingleChoiceItems(labels.toTypedArray(), checkedIndex) { dialog, index ->
                dialog.dismiss()
                onSelected(index)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
