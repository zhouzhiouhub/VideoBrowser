package com.example.videobrowser.functioncenter

import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection

internal class SavedPagesDialogController(
    private val activity: AppCompatActivity,
    private val savedPageRepository: SavedPageRepository,
    private val linkActions: SavedPageLinkActions,
    private val openUrlInNewTab: (String) -> Unit,
    private val loadUrl: (String) -> Unit,
    private val showSavedPagesPage: (
        collection: SavedPageCollection,
        title: String,
        emptyMessage: String,
        replaceCurrent: Boolean,
        query: String?
    ) -> Unit
) {
    fun showSearchDialog(
        collection: SavedPageCollection,
        title: String,
        emptyMessage: String,
        currentQuery: String?
    ) {
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setSingleLine(true)
            hint = activity.getString(R.string.hint_saved_pages_search)
            setText(currentQuery.orEmpty())
            setSelection(text?.length ?: 0)
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_search_saved_pages)
            .setView(input)
            .setPositiveButton(R.string.action_search_saved_pages) { _, _ ->
                showSavedPagesPage(
                    collection,
                    title,
                    emptyMessage,
                    true,
                    input.text?.toString()?.trim().orEmpty()
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showSavedPageActionsDialog(
        collection: SavedPageCollection,
        page: SavedPage,
        title: String,
        emptyMessage: String
    ) {
        val actions = savedPageActions(collection, page, title, emptyMessage)
        AlertDialog.Builder(activity)
            .setTitle(page.title.ifBlank { page.url })
            .setItems(actions.map { action -> action.title }.toTypedArray()) { _, index ->
                actions.getOrNull(index)?.perform?.invoke()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showClearSavedPagesDialog(collection: SavedPageCollection) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_saved_pages_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                savedPageRepository.clear(collection)
                Toast.makeText(
                    activity,
                    R.string.toast_saved_pages_cleared,
                    Toast.LENGTH_SHORT
                ).show()
                showSavedPagesPage(
                    collection,
                    SavedPageCollectionDisplayText.title(activity, collection),
                    SavedPageCollectionDisplayText.emptyMessage(activity, collection),
                    true,
                    null
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun savedPageActions(
        collection: SavedPageCollection,
        page: SavedPage,
        title: String,
        emptyMessage: String
    ): List<SavedPageAction> {
        return listOf(
            SavedPageAction(activity.getString(R.string.action_open_page)) {
                loadUrl(page.url)
            },
            SavedPageAction(activity.getString(R.string.action_open_in_new_tab)) {
                openUrlInNewTab(page.url)
            },
            if (collection == SavedPageCollection.BOOKMARKS) {
                SavedPageAction(activity.getString(R.string.action_rename)) {
                    showRenameBookmarkDialog(page, title, emptyMessage)
                }
            } else {
                null
            },
            if (collection == SavedPageCollection.BOOKMARKS) {
                SavedPageAction(activity.getString(R.string.action_move_bookmark_folder)) {
                    showMoveBookmarkFolderDialog(page, title, emptyMessage)
                }
            } else {
                null
            },
            SavedPageAction(activity.getString(R.string.action_copy_link)) {
                linkActions.copyUrl(page)
            },
            SavedPageAction(activity.getString(R.string.action_share_page)) {
                linkActions.shareUrl(page)
            },
            SavedPageAction(activity.getString(R.string.action_remove)) {
                savedPageRepository.remove(collection, page.url)
                Toast.makeText(
                    activity,
                    R.string.toast_saved_page_removed,
                    Toast.LENGTH_SHORT
                ).show()
                showSavedPagesPage(collection, title, emptyMessage, true, null)
            }
        ).filterNotNull()
    }

    private fun showRenameBookmarkDialog(
        page: SavedPage,
        title: String,
        emptyMessage: String
    ) {
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setSingleLine(true)
            hint = activity.getString(R.string.hint_saved_page_title)
            setText(page.title.ifBlank { page.url })
            setSelection(text?.length ?: 0)
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.title_rename_bookmark)
            .setView(input)
            .setPositiveButton(R.string.action_rename, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val updated = savedPageRepository.updateTitle(
                    collection = SavedPageCollection.BOOKMARKS,
                    url = page.url,
                    title = input.text?.toString().orEmpty()
                )
                if (!updated) {
                    Toast.makeText(activity, R.string.toast_saved_page_title_invalid, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                Toast.makeText(activity, R.string.toast_saved_page_renamed, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showSavedPagesPage(SavedPageCollection.BOOKMARKS, title, emptyMessage, true, null)
            }
        }
        dialog.show()
    }

    private fun showMoveBookmarkFolderDialog(
        page: SavedPage,
        title: String,
        emptyMessage: String
    ) {
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setSingleLine(true)
            hint = activity.getString(R.string.hint_bookmark_folder)
            setText(page.folder)
            setSelection(text?.length ?: 0)
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.title_move_bookmark_folder)
            .setView(input)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val updated = savedPageRepository.updateBookmarkFolder(
                    url = page.url,
                    folder = input.text?.toString().orEmpty()
                )
                if (!updated) {
                    Toast.makeText(activity, R.string.toast_bookmark_folder_invalid, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                Toast.makeText(activity, R.string.toast_bookmark_folder_updated, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showSavedPagesPage(SavedPageCollection.BOOKMARKS, title, emptyMessage, true, null)
            }
        }
        dialog.show()
    }

    private data class SavedPageAction(
        val title: String,
        val perform: () -> Unit
    )
}
