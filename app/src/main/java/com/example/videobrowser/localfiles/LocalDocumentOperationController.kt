package com.example.videobrowser.localfiles

import android.net.Uri
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.utils.ChooserIntentLauncher
import com.example.videobrowser.utils.ConfirmationDialog
import com.example.videobrowser.utils.FileShareIntentFactory
import com.example.videobrowser.utils.ValidatedTextInputDialog

internal class LocalDocumentOperationController(
    private val activity: AppCompatActivity,
    private val documentRepository: LocalDocumentRepository,
    private val showLocalDirectoryPage: (Uri, List<LocalDirectoryPathItem>) -> Unit
) {
    fun promptCreate(
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
                showOperationFailedToast()
                return@showNameInputDialog
            }

            Toast.makeText(activity, R.string.toast_local_file_created, Toast.LENGTH_SHORT).show()
            showLocalDirectoryPage(treeUri, path)
        }
    }

    fun promptRename(
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
                showOperationFailedToast()
                return@showNameInputDialog
            }

            Toast.makeText(activity, R.string.toast_local_file_renamed, Toast.LENGTH_SHORT).show()
            showLocalDirectoryPage(treeUri, path)
        }
    }

    fun confirmDelete(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>
    ) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.title_delete_file,
            message = activity.getString(R.string.dialog_delete_local_file_message, document.name),
            positiveButtonRes = R.string.action_delete_file
        ) {
            if (documentRepository.deleteDocument(document)) {
                Toast.makeText(activity, R.string.toast_local_file_deleted, Toast.LENGTH_SHORT).show()
                showLocalDirectoryPage(treeUri, path)
            } else {
                showOperationFailedToast()
            }
        }
    }

    fun share(document: LocalDocument) {
        val intent = FileShareIntentFactory.create(
            contentResolver = activity.contentResolver,
            uri = document.uri,
            displayName = document.name,
            mimeType = document.mimeType?.takeUnless { document.isDirectory }
        )
        ChooserIntentLauncher.start(
            activity = activity,
            intent = intent,
            chooserTitleRes = R.string.action_share_file
        )
    }

    private fun showNameInputDialog(
        title: String,
        initialValue: String,
        positiveButtonText: String,
        onConfirm: (String) -> Unit
    ) {
        ValidatedTextInputDialog.show(
            activity = activity,
            title = title,
            initialValue = initialValue,
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            positiveButtonText = positiveButtonText,
            invalidToastRes = R.string.toast_local_file_name_invalid,
            selectAllOnFocus = true,
            valueTransform = { value -> value.trim() },
            saveValue = { name -> name.isNotBlank() },
            onSaved = onConfirm
        )
    }

    private fun showOperationFailedToast() {
        Toast.makeText(activity, R.string.toast_local_file_operation_failed, Toast.LENGTH_SHORT).show()
    }
}
