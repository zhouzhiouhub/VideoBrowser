package com.example.videobrowser.utils

import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

data class TextInputDialogField(
    @param:StringRes val hintRes: Int,
    val inputType: Int = InputType.TYPE_CLASS_TEXT,
    val initialValue: String = "",
    val singleLine: Boolean = true
)

data class TwoTextInputValues(
    val first: String,
    val second: String
)

object TwoTextInputDialog {
    fun create(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        message: String? = null,
        firstField: TextInputDialogField,
        secondField: TextInputDialogField,
        @StringRes positiveButtonRes: Int,
        @StringRes negativeButtonRes: Int? = android.R.string.cancel,
        dp: (Int) -> Int,
        onPositive: (TwoTextInputValues) -> Boolean
    ): AlertDialog {
        val firstInput = createInput(activity, firstField)
        val secondInput = createInput(activity, secondField)
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(firstInput)
            addView(secondInput)
        }

        val builder = AppDialog.builder(activity)
            .setTitle(titleRes)
            .setView(content)
            .setPositiveButton(positiveButtonRes, null)
        if (message != null) {
            builder.setMessage(message)
        }
        if (negativeButtonRes != null) {
            builder.setNegativeButton(negativeButtonRes, null)
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val shouldDismiss = onPositive(
                    TwoTextInputValues(
                        first = firstInput.text?.toString().orEmpty(),
                        second = secondInput.text?.toString().orEmpty()
                    )
                )
                if (shouldDismiss) {
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    fun show(
        activity: AppCompatActivity,
        @StringRes titleRes: Int,
        message: String? = null,
        firstField: TextInputDialogField,
        secondField: TextInputDialogField,
        @StringRes positiveButtonRes: Int,
        @StringRes negativeButtonRes: Int? = android.R.string.cancel,
        dp: (Int) -> Int,
        onPositive: (TwoTextInputValues) -> Boolean
    ): AlertDialog {
        val dialog = create(
            activity = activity,
            titleRes = titleRes,
            message = message,
            firstField = firstField,
            secondField = secondField,
            positiveButtonRes = positiveButtonRes,
            negativeButtonRes = negativeButtonRes,
            dp = dp,
            onPositive = onPositive
        )
        dialog.show()
        return dialog
    }

    private fun createInput(activity: AppCompatActivity, field: TextInputDialogField): EditText {
        return EditText(activity).apply {
            hint = activity.getString(field.hintRes)
            inputType = field.inputType
            setSingleLine(field.singleLine)
            setText(field.initialValue)
            setSelection(text?.length ?: 0)
        }
    }
}
