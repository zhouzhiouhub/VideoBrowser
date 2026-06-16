package com.example.videobrowser.localfiles

/**
 * 初学者阅读提示：
 * 这个文件属于“本地文件模块”。
 * 文件名 LocalDocument 可以拆开理解为“Local Document”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：管理目录授权、读取本地文档列表，并把本地媒体交给浏览器或播放器打开。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
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
