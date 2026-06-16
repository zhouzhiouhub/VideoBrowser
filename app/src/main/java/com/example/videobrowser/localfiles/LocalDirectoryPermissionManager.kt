package com.example.videobrowser.localfiles

/**
 * 初学者阅读提示：
 * 这个文件属于“本地文件模块”。
 * 文件名 LocalDirectoryPermissionManager 可以拆开理解为“Local Directory Permission Manager”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：管理目录授权、读取本地文档列表，并把本地媒体交给浏览器或播放器打开。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.videobrowser.storage.PreferenceStore

class LocalDirectoryPermissionManager(
    private val context: Context,
    private val preferenceStore: PreferenceStore,
    private val logTag: String
) {
    fun savedDirectoryUri(): Uri? {
        return preferenceStore.getString(KEY_LOCAL_DIRECTORY_URI, null)
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)
    }

    fun saveDirectoryUri(uri: Uri) {
        preferenceStore.putString(KEY_LOCAL_DIRECTORY_URI, uri.toString())
    }

    fun clearSavedDirectoryUri() {
        preferenceStore.remove(KEY_LOCAL_DIRECTORY_URI)
    }

    fun persistReadWritePermission(uri: Uri): Boolean {
        return runCatching {
            context.contentResolver.takePersistableUriPermission(uri, DIRECTORY_PERMISSION_FLAGS)
        }.fold(
            onSuccess = { true },
            onFailure = {
                Log.w(logTag, "Unable to persist URI permission for $uri", it)
                false
            }
        )
    }

    fun releaseReadWritePermission(uri: Uri) {
        runCatching {
            context.contentResolver.releasePersistableUriPermission(uri, DIRECTORY_PERMISSION_FLAGS)
        }.onFailure {
            Log.w(logTag, "Unable to release URI permission for $uri", it)
        }
    }

    companion object {
        private const val DIRECTORY_PERMISSION_FLAGS =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        private const val KEY_LOCAL_DIRECTORY_URI = "local_directory_uri"
    }
}
