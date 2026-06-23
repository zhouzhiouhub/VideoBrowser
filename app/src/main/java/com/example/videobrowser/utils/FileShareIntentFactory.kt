package com.example.videobrowser.utils

import android.content.ClipData
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri

object FileShareIntentFactory {
    fun create(
        contentResolver: ContentResolver,
        uri: Uri,
        displayName: String,
        mimeType: String?
    ): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(contentResolver, displayName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
