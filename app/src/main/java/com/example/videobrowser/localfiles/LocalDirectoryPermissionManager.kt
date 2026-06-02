package com.example.videobrowser.localfiles

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
