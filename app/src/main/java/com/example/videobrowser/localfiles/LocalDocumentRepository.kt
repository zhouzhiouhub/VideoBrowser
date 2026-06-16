package com.example.videobrowser.localfiles

/**
 * 初学者阅读提示：
 * 这个文件属于“本地文件模块”。
 * 文件名 LocalDocumentRepository 可以拆开理解为“Local Document Repository”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：管理目录授权、读取本地文档列表，并把本地媒体交给浏览器或播放器打开。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
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
