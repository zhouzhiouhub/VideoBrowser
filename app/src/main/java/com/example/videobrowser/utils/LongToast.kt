package com.example.videobrowser.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object LongToast {
    fun show(context: Context, @StringRes messageResId: Int) {
        AppToast.show(context, messageResId, Toast.LENGTH_LONG)
    }

    fun show(context: Context, message: CharSequence) {
        AppToast.show(context, message, Toast.LENGTH_LONG)
    }
}
