package com.example.videobrowser.utils

import android.content.Context
import com.example.videobrowser.R

object PageUnavailableToast {
    fun showNoPageUrl(context: Context) {
        ShortToast.show(context, R.string.toast_no_page_url)
    }
}
