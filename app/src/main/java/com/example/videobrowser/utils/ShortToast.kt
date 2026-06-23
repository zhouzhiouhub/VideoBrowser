package com.example.videobrowser.utils

import android.content.Context
import android.widget.Toast

object ShortToast {
    fun show(context: Context, messageResId: Int) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
    }

    fun show(context: Context, message: CharSequence) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
