package com.example.videobrowser.localfiles

import android.net.Uri
import android.provider.DocumentsContract

data class LocalDocument(
    val uri: Uri,
    val documentId: String,
    val name: String,
    val mimeType: String?,
    val size: Long?,
    val modifiedAt: Long?,
    val flags: Int
) {
    val isDirectory: Boolean
        get() = mimeType == DocumentsContract.Document.MIME_TYPE_DIR

    val canDelete: Boolean
        get() = flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE != 0

    val canRename: Boolean
        get() = flags and DocumentsContract.Document.FLAG_SUPPORTS_RENAME != 0
}
