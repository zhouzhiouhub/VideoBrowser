package com.example.videobrowser.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

object AppDialog {
    fun builder(context: Context): AlertDialog.Builder {
        return AlertDialog.Builder(context)
    }
}
