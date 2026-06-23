package com.example.videobrowser.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object AppToast {
    fun show(context: Context, @StringRes messageResId: Int, duration: Int) {
        show(context, context.getText(messageResId), duration)
    }

    fun show(context: Context, message: CharSequence, duration: Int) {
        Toast.makeText(context, message, duration).show()
    }
}
