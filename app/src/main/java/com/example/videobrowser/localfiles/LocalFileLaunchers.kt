package com.example.videobrowser.localfiles

import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class LocalFileLaunchers(
    private val activity: AppCompatActivity,
    private val directoryPermissions: LocalDirectoryPermissionManager,
    private val documentRepository: LocalDocumentRepository,
    private val logTag: String,
    private val onOpenDocumentUri: (Uri, String?, String?) -> Unit,
    private val onDirectoryReady: (Uri) -> Unit,
    private val onDirectoryUnavailable: () -> Unit
) {
    private lateinit var openLocalFileLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var openLocalDirectoryLauncher: ActivityResultLauncher<Uri?>

    fun setup() {
        openLocalFileLauncher =
            activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    onOpenDocumentUri(uri, null, null)
                }
            }

        openLocalDirectoryLauncher =
            activity.registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                if (uri != null) {
                    handleSelectedDirectory(uri)
                }
            }
    }

    fun openFile() {
        openLocalFileLauncher.launch(arrayOf("*/*"))
    }

    fun openDirectory(initialUri: Uri?) {
        openLocalDirectoryLauncher.launch(initialUri)
    }

    private fun handleSelectedDirectory(uri: Uri) {
        val previousUri = directoryPermissions.savedDirectoryUri()
        if (!directoryPermissions.persistReadWritePermission(uri)) {
            onDirectoryUnavailable()
            return
        }

        val rootDocumentId = documentRepository.rootDocumentId(uri)
        if (rootDocumentId == null) {
            directoryPermissions.releaseReadWritePermission(uri)
            onDirectoryUnavailable()
            return
        }

        val isDirectoryUsable = runCatching {
            documentRepository.queryDocuments(uri, rootDocumentId)
        }.onFailure {
            Log.w(logTag, "Unable to access selected local directory $uri", it)
            directoryPermissions.releaseReadWritePermission(uri)
            onDirectoryUnavailable()
        }.isSuccess
        if (!isDirectoryUsable) {
            return
        }

        if (previousUri != null && previousUri != uri) {
            directoryPermissions.releaseReadWritePermission(previousUri)
        }
        directoryPermissions.saveDirectoryUri(uri)
        onDirectoryReady(uri)
    }
}
