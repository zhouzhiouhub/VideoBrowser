package com.example.videobrowser.localfiles

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import java.util.Locale

class LocalDocumentRepository(
    private val context: Context
) {
    fun rootDocumentId(treeUri: Uri): String? {
        return runCatching {
            DocumentsContract.getTreeDocumentId(treeUri)
        }.getOrNull()
    }

    fun queryDocuments(treeUri: Uri, parentDocumentId: String): List<LocalDocument> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS
        )
        val documents = mutableListOf<LocalDocument>()
        val cursor = context.contentResolver.query(childrenUri, projection, null, null, null)
            ?: throw IllegalStateException("Unable to query child documents for $childrenUri")
        cursor.use { cursor ->
            val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
            val modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            val flagsIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS)

            while (cursor.moveToNext()) {
                val documentId = cursor.getStringOrNull(idIndex) ?: continue
                val name = cursor.getStringOrNull(nameIndex)
                    ?.takeIf { it.isNotBlank() }
                    ?: documentId.substringAfterLast(':')
                documents += LocalDocument(
                    uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId),
                    documentId = documentId,
                    name = name,
                    mimeType = cursor.getStringOrNull(mimeIndex),
                    size = cursor.getLongOrNull(sizeIndex),
                    modifiedAt = cursor.getLongOrNull(modifiedIndex),
                    flags = cursor.getIntOrNull(flagsIndex) ?: 0
                )
            }
        }
        return documents.sortedWith(
            compareBy<LocalDocument> { !it.isDirectory }
                .thenBy { it.name.lowercase(Locale.getDefault()) }
        )
    }

    fun createDocument(
        treeUri: Uri,
        parentDocumentId: String,
        mimeType: String,
        name: String
    ): Uri? {
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocumentId)
        return runCatching {
            DocumentsContract.createDocument(context.contentResolver, parentUri, mimeType, name)
        }.getOrNull()
    }

    fun renameDocument(document: LocalDocument, name: String): Uri? {
        return runCatching {
            DocumentsContract.renameDocument(context.contentResolver, document.uri, name)
        }.getOrNull()
    }

    fun deleteDocument(document: LocalDocument): Boolean {
        return runCatching {
            DocumentsContract.deleteDocument(context.contentResolver, document.uri)
        }.getOrDefault(false)
    }

    private fun Cursor.getStringOrNull(index: Int): String? {
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun Cursor.getLongOrNull(index: Int): Long? {
        return if (index >= 0 && !isNull(index)) getLong(index) else null
    }

    private fun Cursor.getIntOrNull(index: Int): Int? {
        return if (index >= 0 && !isNull(index)) getInt(index) else null
    }
}
