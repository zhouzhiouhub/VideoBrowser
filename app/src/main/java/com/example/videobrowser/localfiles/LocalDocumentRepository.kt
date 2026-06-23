package com.example.videobrowser.localfiles

/**
 * 初学者阅读提示：
 * 这个文件属于“本地文件模块”。
 * 文件名 LocalDocumentRepository 可以拆开理解为“Local Document Repository”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：管理目录授权、读取本地文档列表，并把本地媒体交给浏览器或播放器打开。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.example.videobrowser.utils.columnValueReader
import java.util.Locale

class LocalDocumentRepository(
    private val context: Context
) {
    /**
     * 函数 `rootDocumentId`：封装 `root Document Id` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param treeUri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun rootDocumentId(treeUri: Uri): String? {
        return runCatching {
            DocumentsContract.getTreeDocumentId(treeUri)
        }.getOrNull()
    }

    /**
     * 函数 `queryDocuments`：封装 `query Documents` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param treeUri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param parentDocumentId 参数类型为 `String`，表示函数执行 `parentDocumentId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
            val reader = cursor.columnValueReader()

            while (cursor.moveToNext()) {
                val documentId = reader.stringOrNull(DocumentsContract.Document.COLUMN_DOCUMENT_ID) ?: continue
                val name = reader.stringOrNull(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    ?.takeIf { it.isNotBlank() }
                    ?: documentId.substringAfterLast(':')
                documents += LocalDocument(
                    uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId),
                    documentId = documentId,
                    name = name,
                    mimeType = reader.stringOrNull(DocumentsContract.Document.COLUMN_MIME_TYPE),
                    size = reader.longOrNull(DocumentsContract.Document.COLUMN_SIZE),
                    modifiedAt = reader.longOrNull(DocumentsContract.Document.COLUMN_LAST_MODIFIED),
                    flags = reader.intOrNull(DocumentsContract.Document.COLUMN_FLAGS) ?: 0
                )
            }
        }
        return documents.sortedWith(
            compareBy<LocalDocument> { !it.isDirectory }
                .thenBy { it.name.lowercase(Locale.getDefault()) }
        )
    }

    /**
     * 函数 `createDocument`：创建 `create Document` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param treeUri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param parentDocumentId 参数类型为 `String`，表示函数执行 `parentDocumentId` 相关逻辑时需要读取或处理的输入。
     * @param mimeType 参数类型为 `String`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `renameDocument`：封装 `rename Document` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param document 参数类型为 `LocalDocument`，表示函数执行 `document` 相关逻辑时需要读取或处理的输入。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun renameDocument(document: LocalDocument, name: String): Uri? {
        return runCatching {
            DocumentsContract.renameDocument(context.contentResolver, document.uri, name)
        }.getOrNull()
    }

    /**
     * 函数 `deleteDocument`：封装 `delete Document` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param document 参数类型为 `LocalDocument`，表示函数执行 `document` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun deleteDocument(document: LocalDocument): Boolean {
        return runCatching {
            DocumentsContract.deleteDocument(context.contentResolver, document.uri)
        }.getOrDefault(false)
    }

}
