package com.example.videobrowser.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

object ClipboardTextActions {
    fun copyPlainText(
        activity: AppCompatActivity,
        labelResId: Int,
        text: String,
        toastResId: Int
    ) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(activity.getString(labelResId), text)
        )
        Toast.makeText(activity, toastResId, Toast.LENGTH_SHORT).show()
    }
}
