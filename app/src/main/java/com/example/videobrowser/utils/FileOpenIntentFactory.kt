package com.example.videobrowser.utils

import android.content.Intent
import android.net.Uri

object FileOpenIntentFactory {
    fun create(uri: Uri, mimeType: String?): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
