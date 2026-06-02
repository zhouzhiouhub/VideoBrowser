package com.example.videobrowser.localfiles

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.functioncenter.FunctionCenterController
import com.example.videobrowser.storage.PreferenceStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalFilesController(
    private val activity: AppCompatActivity,
    private val preferenceStore: PreferenceStore,
    private val functionCenter: FunctionCenterController,
    private val logTag: String,
    private val showMainFunctionCenterPage: () -> Unit,
    private val onOpenDocumentUri: (Uri, String?, String?) -> Unit
) {
    private data class LocalDirectoryPathItem(
        val documentId: String,
        val title: String
    )

    private data class LocalDocument(
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

    private lateinit var openLocalFileLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var openLocalDirectoryLauncher: ActivityResultLauncher<Uri?>

    fun setupLaunchers() {
        openLocalFileLauncher = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                return@registerForActivityResult
            }
            onOpenDocumentUri(uri, null, null)
        }

        openLocalDirectoryLauncher =
            activity.registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                if (uri == null) {
                    return@registerForActivityResult
                }
                val previousUri = savedLocalDirectoryUri()
                if (!persistUriPermission(uri, DIRECTORY_PERMISSION_FLAGS)) {
                    Toast.makeText(activity, R.string.toast_local_folder_unavailable, Toast.LENGTH_SHORT)
                        .show()
                    return@registerForActivityResult
                }
                val rootDocumentId = runCatching {
                    DocumentsContract.getTreeDocumentId(uri)
                }.getOrNull()
                if (rootDocumentId == null) {
                    releaseUriPermission(uri, DIRECTORY_PERMISSION_FLAGS)
                    Toast.makeText(activity, R.string.toast_local_folder_unavailable, Toast.LENGTH_SHORT)
                        .show()
                    return@registerForActivityResult
                }
                val isDirectoryUsable = runCatching {
                    queryLocalDocuments(uri, rootDocumentId)
                }.onFailure {
                    Log.w(logTag, "Unable to access selected local directory $uri", it)
                    releaseUriPermission(uri, DIRECTORY_PERMISSION_FLAGS)
                    Toast.makeText(activity, R.string.toast_local_folder_unavailable, Toast.LENGTH_SHORT)
                        .show()
                }.isSuccess
                if (!isDirectoryUsable) {
                    return@registerForActivityResult
                }
                if (previousUri != null && previousUri != uri) {
                    releaseUriPermission(previousUri, DIRECTORY_PERMISSION_FLAGS)
                }
                preferenceStore.putString(KEY_LOCAL_DIRECTORY_URI, uri.toString())
                showLocalDirectoryPage(uri)
            }
    }

    fun showFileOperationsPage() {
        functionCenter.showPage(
            title = activity.getString(R.string.title_file_operations),
            onBack = showMainFunctionCenterPage
        ) { content ->
            functionCenter.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_open_local_file),
                    summary = activity.getString(R.string.action_open_local_file_summary)
                ) {
                    openLocalFileLauncher.launch(arrayOf("*/*"))
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_browse_local_folder),
                    summary = activity.getString(R.string.action_browse_local_folder_summary)
                ) {
                    val uri = savedLocalDirectoryUri()
                    if (uri == null) {
                        openLocalDirectoryLauncher.launch(null)
                    } else {
                        showLocalDirectoryPage(uri)
                    }
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_choose_local_folder),
                    summary = activity.getString(R.string.action_choose_local_folder_summary)
                ) {
                    openLocalDirectoryLauncher.launch(savedLocalDirectoryUri())
                }
            }
        }
    }

    private fun persistUriPermission(uri: Uri, flags: Int): Boolean {
        return runCatching {
            activity.contentResolver.takePersistableUriPermission(uri, flags)
        }.fold(
            onSuccess = { true },
            onFailure = {
                Log.w(logTag, "Unable to persist URI permission for $uri", it)
                false
            }
        )
    }

    private fun releaseUriPermission(uri: Uri, flags: Int) {
        runCatching {
            activity.contentResolver.releasePersistableUriPermission(uri, flags)
        }.onFailure {
            Log.w(logTag, "Unable to release URI permission for $uri", it)
        }
    }

    private fun savedLocalDirectoryUri(): Uri? {
        return preferenceStore.getString(KEY_LOCAL_DIRECTORY_URI, null)
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)
    }

    private fun showLocalDirectoryPage(treeUri: Uri) {
        val rootDocumentId = runCatching {
            DocumentsContract.getTreeDocumentId(treeUri)
        }.getOrNull()

        if (rootDocumentId == null) {
            releaseUriPermission(treeUri, DIRECTORY_PERMISSION_FLAGS)
            preferenceStore.remove(KEY_LOCAL_DIRECTORY_URI)
            Toast.makeText(activity, R.string.toast_local_folder_unavailable, Toast.LENGTH_SHORT).show()
            showFileOperationsPage()
            return
        }

        showLocalDirectoryPage(
            treeUri = treeUri,
            path = listOf(
                LocalDirectoryPathItem(
                    documentId = rootDocumentId,
                    title = activity.getString(R.string.title_local_files)
                )
            )
        )
    }

    private fun showLocalDirectoryPage(treeUri: Uri, path: List<LocalDirectoryPathItem>) {
        val current = path.lastOrNull() ?: return showLocalDirectoryPage(treeUri)
        val onBack: () -> Unit = if (path.size > 1) {
            { showLocalDirectoryPage(treeUri, path.dropLast(1)) }
        } else {
            { showFileOperationsPage() }
        }

        val documents = runCatching {
            queryLocalDocuments(treeUri, current.documentId)
        }.getOrElse {
            Log.w(logTag, "Unable to query local directory $treeUri", it)
            releaseUriPermission(treeUri, DIRECTORY_PERMISSION_FLAGS)
            preferenceStore.remove(KEY_LOCAL_DIRECTORY_URI)
            Toast.makeText(
                activity,
                R.string.toast_local_folder_unavailable,
                Toast.LENGTH_SHORT
            ).show()
            showFileOperationsPage()
            return
        }

        functionCenter.showPage(
            title = current.title,
            onBack = onBack
        ) { content ->
            functionCenter.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_new_folder),
                    summary = activity.getString(R.string.action_new_folder_summary)
                ) {
                    promptCreateLocalDocument(
                        treeUri = treeUri,
                        path = path,
                        mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                        defaultName = activity.getString(R.string.default_new_folder_name),
                        dialogTitle = activity.getString(R.string.title_new_folder)
                    )
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_new_text_file),
                    summary = activity.getString(R.string.action_new_text_file_summary)
                ) {
                    promptCreateLocalDocument(
                        treeUri = treeUri,
                        path = path,
                        mimeType = "text/plain",
                        defaultName = activity.getString(R.string.default_new_text_file_name),
                        dialogTitle = activity.getString(R.string.title_new_text_file)
                    )
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_refresh),
                    summary = activity.getString(R.string.action_refresh_local_folder_summary)
                ) {
                    showLocalDirectoryPage(treeUri, path)
                }
            }

            if (documents.isEmpty()) {
                functionCenter.addEmptyState(content, activity.getString(R.string.dialog_local_folder_empty))
                return@showPage
            }

            functionCenter.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_files)
            ) { section ->
                documents.forEach { document ->
                    functionCenter.addActionRow(
                        parent = section,
                        title = document.name,
                        summary = localDocumentSummary(document)
                    ) {
                        if (document.isDirectory) {
                            showLocalDirectoryPage(
                                treeUri = treeUri,
                                path = path + LocalDirectoryPathItem(document.documentId, document.name)
                            )
                        } else {
                            showLocalDocumentActionsPage(document, treeUri, path)
                        }
                    }
                }
            }
        }
    }

    private fun queryLocalDocuments(treeUri: Uri, parentDocumentId: String): List<LocalDocument> {
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
        val cursor = activity.contentResolver.query(childrenUri, projection, null, null, null)
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

    private fun showLocalDocumentActionsPage(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>
    ) {
        functionCenter.showPage(
            title = document.name,
            onBack = { showLocalDirectoryPage(treeUri, path) }
        ) { content ->
            functionCenter.addFunctionMessage(content, localDocumentSummary(document))
            functionCenter.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_open_file),
                    summary = activity.getString(R.string.action_open_file_summary)
                ) {
                    onOpenDocumentUri(document.uri, document.name, document.mimeType)
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_share_file),
                    summary = activity.getString(R.string.action_share_file_summary)
                ) {
                    shareLocalDocument(document)
                }

                if (document.canRename) {
                    functionCenter.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_rename_file),
                        summary = activity.getString(R.string.action_rename_file_summary)
                    ) {
                        promptRenameLocalDocument(document, treeUri, path)
                    }
                }

                if (document.canDelete) {
                    functionCenter.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_delete_file),
                        summary = activity.getString(R.string.action_delete_file_summary)
                    ) {
                        confirmDeleteLocalDocument(document, treeUri, path)
                    }
                }
            }
        }
    }

    private fun promptCreateLocalDocument(
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>,
        mimeType: String,
        defaultName: String,
        dialogTitle: String
    ) {
        showNameInputDialog(
            title = dialogTitle,
            initialValue = defaultName,
            positiveButtonText = activity.getString(R.string.action_create)
        ) { name ->
            val parent = path.lastOrNull() ?: return@showNameInputDialog
            val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parent.documentId)
            val createdUri = runCatching {
                DocumentsContract.createDocument(activity.contentResolver, parentUri, mimeType, name)
            }.getOrNull()

            if (createdUri == null) {
                Toast.makeText(activity, R.string.toast_local_file_operation_failed, Toast.LENGTH_SHORT).show()
                return@showNameInputDialog
            }

            Toast.makeText(activity, R.string.toast_local_file_created, Toast.LENGTH_SHORT).show()
            showLocalDirectoryPage(treeUri, path)
        }
    }

    private fun promptRenameLocalDocument(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>
    ) {
        showNameInputDialog(
            title = activity.getString(R.string.title_rename_file),
            initialValue = document.name,
            positiveButtonText = activity.getString(R.string.action_rename)
        ) { name ->
            val renamedUri = runCatching {
                DocumentsContract.renameDocument(activity.contentResolver, document.uri, name)
            }.getOrNull()

            if (renamedUri == null) {
                Toast.makeText(activity, R.string.toast_local_file_operation_failed, Toast.LENGTH_SHORT).show()
                return@showNameInputDialog
            }

            Toast.makeText(activity, R.string.toast_local_file_renamed, Toast.LENGTH_SHORT).show()
            showLocalDirectoryPage(treeUri, path)
        }
    }

    private fun confirmDeleteLocalDocument(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>
    ) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_delete_file)
            .setMessage(activity.getString(R.string.dialog_delete_local_file_message, document.name))
            .setPositiveButton(R.string.action_delete_file) { _, _ ->
                val deleted = runCatching {
                    DocumentsContract.deleteDocument(activity.contentResolver, document.uri)
                }.getOrDefault(false)

                if (!deleted) {
                    Toast.makeText(
                        activity,
                        R.string.toast_local_file_operation_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                Toast.makeText(activity, R.string.toast_local_file_deleted, Toast.LENGTH_SHORT).show()
                showLocalDirectoryPage(treeUri, path)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showNameInputDialog(
        title: String,
        initialValue: String,
        positiveButtonText: String,
        onConfirm: (String) -> Unit
    ) {
        val input = EditText(activity).apply {
            setText(initialValue)
            setSelectAllOnFocus(true)
            selectAll()
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }

        AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(input)
            .setPositiveButton(positiveButtonText) { _, _ ->
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isBlank()) {
                    Toast.makeText(activity, R.string.toast_local_file_name_invalid, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                onConfirm(name)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun shareLocalDocument(document: LocalDocument) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = document.mimeType?.takeUnless { document.isDirectory } ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, document.uri)
            clipData = ClipData.newUri(activity.contentResolver, document.name, document.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.action_share_file)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        }
    }

    private fun localDocumentSummary(document: LocalDocument): String {
        if (document.isDirectory) {
            return activity.getString(R.string.local_file_type_folder)
        }

        val type = document.mimeType
            ?.takeIf { it.isNotBlank() }
            ?: activity.getString(R.string.local_file_type_unknown)
        return listOf(
            type,
            formatFileSize(document.size),
            formatModifiedTime(document.modifiedAt)
        ).joinToString(separator = " · ")
    }

    private fun formatFileSize(size: Long?): String {
        if (size == null || size < 0) {
            return activity.getString(R.string.local_file_size_unknown)
        }

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = size.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024
            unitIndex++
        }
        return if (unitIndex == 0) {
            "$size ${units[unitIndex]}"
        } else {
            String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex])
        }
    }

    private fun formatModifiedTime(modifiedAt: Long?): String {
        if (modifiedAt == null || modifiedAt <= 0L) {
            return activity.getString(R.string.local_file_time_unknown)
        }
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(modifiedAt))
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

    private companion object {
        private const val DIRECTORY_PERMISSION_FLAGS =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        private const val KEY_LOCAL_DIRECTORY_URI = "local_directory_uri"
    }
}