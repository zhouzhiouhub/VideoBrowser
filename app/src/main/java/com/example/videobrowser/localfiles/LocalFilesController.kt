package com.example.videobrowser.localfiles

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.functioncenter.FunctionCenterController
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.video.ExternalSubtitleCandidate
import com.example.videobrowser.video.LocalPlaybackQueueBuilder
import com.example.videobrowser.video.LocalSubtitleMatcher
import com.example.videobrowser.video.PlaybackQueue

class LocalFilesController(
    private val activity: AppCompatActivity,
    private val preferenceStore: PreferenceStore,
    private val functionCenter: FunctionCenterController,
    private val logTag: String,
    private val showMainFunctionCenterPage: () -> Unit,
    private val onOpenDocumentUri: (
        Uri,
        String?,
        String?,
        List<ExternalSubtitleCandidate>,
        PlaybackQueue?
    ) -> Unit
) {
    private val directoryPermissions =
        LocalDirectoryPermissionManager(activity, preferenceStore, logTag)
    private val documentRepository = LocalDocumentRepository(activity)
    private val documentFormatter = LocalDocumentFormatter(activity)
    private val fileLaunchers = LocalFileLaunchers(
        activity = activity,
        directoryPermissions = directoryPermissions,
        documentRepository = documentRepository,
        logTag = logTag,
        onOpenDocumentUri = onOpenDocumentUri,
        onDirectoryReady = { uri -> showLocalDirectoryPage(uri) },
        onDirectoryUnavailable = ::showLocalFolderUnavailableToast
    )

    fun setupLaunchers() {
        fileLaunchers.setup()
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
                    fileLaunchers.openFile()
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_browse_local_folder),
                    summary = activity.getString(R.string.action_browse_local_folder_summary)
                ) {
                    val uri = directoryPermissions.savedDirectoryUri()
                    if (uri == null) {
                        fileLaunchers.openDirectory(null)
                    } else {
                        showLocalDirectoryPage(uri)
                    }
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_choose_local_folder),
                    summary = activity.getString(R.string.action_choose_local_folder_summary)
                ) {
                    fileLaunchers.openDirectory(directoryPermissions.savedDirectoryUri())
                }
            }
        }
    }

    private fun showLocalFolderUnavailableToast() {
        Toast.makeText(activity, R.string.toast_local_folder_unavailable, Toast.LENGTH_SHORT).show()
    }

    private fun showLocalDirectoryPage(treeUri: Uri) {
        val rootDocumentId = documentRepository.rootDocumentId(treeUri)

        if (rootDocumentId == null) {
            directoryPermissions.releaseReadWritePermission(treeUri)
            directoryPermissions.clearSavedDirectoryUri()
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
            documentRepository.queryDocuments(treeUri, current.documentId)
        }.getOrElse {
            Log.w(logTag, "Unable to query local directory $treeUri", it)
            directoryPermissions.releaseReadWritePermission(treeUri)
            directoryPermissions.clearSavedDirectoryUri()
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
                        summary = documentFormatter.summary(document)
                    ) {
                        if (document.isDirectory) {
                            showLocalDirectoryPage(
                                treeUri = treeUri,
                                path = path + LocalDirectoryPathItem(document.documentId, document.name)
                            )
                        } else {
                            showLocalDocumentActionsPage(document, treeUri, path, documents)
                        }
                    }
                }
            }
        }
    }

    private fun showLocalDocumentActionsPage(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>,
        siblingDocuments: List<LocalDocument>
    ) {
        functionCenter.showPage(
            title = document.name,
            onBack = { showLocalDirectoryPage(treeUri, path) }
        ) { content ->
            functionCenter.addFunctionMessage(content, documentFormatter.summary(document))
            functionCenter.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_open_file),
                    summary = activity.getString(R.string.action_open_file_summary)
                ) {
                    val queueDocuments = siblingDocuments.map {
                        LocalPlaybackQueueBuilder.Document(
                            uri = it.uri.toString(),
                            name = it.name,
                            mimeType = it.mimeType,
                            isDirectory = it.isDirectory
                        )
                    }
                    val playbackQueue = LocalPlaybackQueueBuilder.fromDocuments(
                        currentUri = document.uri.toString(),
                        currentName = document.name,
                        currentMimeType = document.mimeType,
                        documents = queueDocuments
                    )
                    val subtitleCandidates = LocalSubtitleMatcher.findSubtitleCandidates(
                        mediaName = document.name,
                        documents = siblingDocuments
                            .filterNot { it.isDirectory }
                            .map {
                                LocalSubtitleMatcher.Document(
                                    uri = it.uri.toString(),
                                    name = it.name,
                                    mimeType = it.mimeType
                                )
                            }
                    )
                    onOpenDocumentUri(
                        document.uri,
                        document.name,
                        document.mimeType,
                        subtitleCandidates,
                        playbackQueue
                    )
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
            val createdUri = documentRepository.createDocument(
                treeUri = treeUri,
                parentDocumentId = parent.documentId,
                mimeType = mimeType,
                name = name
            )

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
            val renamedUri = documentRepository.renameDocument(document, name)

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
                val deleted = documentRepository.deleteDocument(document)

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
}
