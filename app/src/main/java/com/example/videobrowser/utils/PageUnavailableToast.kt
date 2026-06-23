package com.example.videobrowser.utils

import android.content.Context
import android.widget.Toast
import com.example.videobrowser.R

object PageUnavailableToast {
    fun showNoPageUrl(context: Context) {
        Toast.makeText(context, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
    }
}
