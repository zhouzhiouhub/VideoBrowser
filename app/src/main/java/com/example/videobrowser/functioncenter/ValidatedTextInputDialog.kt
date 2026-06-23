package com.example.videobrowser.functioncenter

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
        @StringRes successToastRes: Int,
        saveValue: (String) -> Boolean,
        onSaved: () -> Unit
    ) {
        val input = EditText(activity).apply {
            this.inputType = inputType
            setSingleLine(true)
            hint = activity.getString(hintRes)
            setText(initialValue)
            setSelection(text?.length ?: 0)
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle(titleRes)
            .setView(input)
            .setPositiveButton(positiveButtonRes, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (!saveValue(input.text?.toString().orEmpty())) {
                    Toast.makeText(activity, invalidToastRes, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                Toast.makeText(activity, successToastRes, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                onSaved()
            }
        }
        dialog.show()
    }
}
