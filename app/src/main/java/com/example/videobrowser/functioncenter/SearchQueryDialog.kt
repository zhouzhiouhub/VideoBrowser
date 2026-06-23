package com.example.videobrowser.functioncenter

import android.text.InputType
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.utils.AppDialog

internal object SearchQueryDialog {
    fun show(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        @StringRes hintRes: Int,
        currentQuery: String?,
        onSearch: (String) -> Unit
    ) {
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setSingleLine(true)
            hint = activity.getString(hintRes)
            setText(currentQuery.orEmpty())
            setSelection(text?.length ?: 0)
        }

        AppDialog.builder(activity)
            .setTitle(titleRes)
            .setView(input)
            .setPositiveButton(titleRes) { _, _ ->
                onSearch(input.text?.toString()?.trim().orEmpty())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
