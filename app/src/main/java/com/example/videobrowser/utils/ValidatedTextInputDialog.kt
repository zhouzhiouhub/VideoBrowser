package com.example.videobrowser.utils

import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

internal object ValidatedTextInputDialog {
    fun show(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        @StringRes hintRes: Int,
        initialValue: String,
        inputType: Int,
        @StringRes positiveButtonRes: Int,
        @StringRes invalidToastRes: Int,
        @StringRes successToastRes: Int? = null,
        selectAllOnFocus: Boolean = false,
        valueTransform: (String) -> String = { value -> value },
        saveValue: (String) -> Boolean,
        onSaved: (String) -> Unit
    ) {
        show(
            activity = activity,
            title = activity.getString(titleRes),
            hint = activity.getString(hintRes),
            initialValue = initialValue,
            inputType = inputType,
            positiveButtonText = activity.getString(positiveButtonRes),
            invalidToastRes = invalidToastRes,
            successToastRes = successToastRes,
            selectAllOnFocus = selectAllOnFocus,
            valueTransform = valueTransform,
            saveValue = saveValue,
            onSaved = onSaved
        )
    }

    fun show(
        activity: AppCompatActivity,
        title: String,
        hint: String? = null,
        initialValue: String,
        inputType: Int,
        positiveButtonText: String,
        @StringRes invalidToastRes: Int,
        @StringRes successToastRes: Int? = null,
        selectAllOnFocus: Boolean = false,
        valueTransform: (String) -> String = { value -> value },
        saveValue: (String) -> Boolean,
        onSaved: (String) -> Unit
    ) {
        val input = EditText(activity).apply {
            this.inputType = inputType
            setSingleLine(true)
            if (hint != null) {
                this.hint = hint
            }
            setText(initialValue)
            if (selectAllOnFocus) {
                setSelectAllOnFocus(true)
                selectAll()
            } else {
                setSelection(text?.length ?: 0)
            }
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(input)
            .setPositiveButton(positiveButtonText, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val value = valueTransform(input.text?.toString().orEmpty())
                if (!saveValue(value)) {
                    Toast.makeText(activity, invalidToastRes, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (successToastRes != null) {
                    Toast.makeText(activity, successToastRes, Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
                onSaved(value)
            }
        }
        dialog.show()
    }
}
