package com.example.videobrowser.functioncenter

import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.ConfirmationDialog
import com.example.videobrowser.utils.ShortToast
import com.example.videobrowser.utils.ValidatedTextInputDialog

internal class SavedPagesDialogController(
    private val activity: AppCompatActivity,
    private val savedPageRepository: SavedPageRepository,
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
        SearchQueryDialog.show(
            activity = activity,
            titleRes = R.string.action_search_saved_pages,
            hintRes = R.string.hint_saved_pages_search,
            currentQuery = currentQuery
        ) { query ->
            showSavedPagesPage(collection, title, emptyMessage, true, query)
        }
    }

    fun showClearSavedPagesDialog(collection: SavedPageCollection) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            messageRes = R.string.dialog_clear_saved_pages_message,
            positiveButtonRes = R.string.action_clear
        ) {
            savedPageRepository.clear(collection)
            ShortToast.show(activity, R.string.toast_saved_pages_cleared)
            showSavedPagesPage(
                collection,
                SavedPageCollectionDisplayText.title(activity, collection),
                SavedPageCollectionDisplayText.emptyMessage(activity, collection),
                true,
                null
            )
        }
    }

    fun showRenameSavedPageDialog(
        collection: SavedPageCollection,
        page: SavedPage,
        title: String,
        emptyMessage: String,
        query: String?
    ) {
        ValidatedTextInputDialog.show(
            activity = activity,
            titleRes = R.string.title_rename_saved_page,
            hintRes = R.string.hint_saved_page_title,
            initialValue = page.title.ifBlank { page.url },
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
            positiveButtonRes = R.string.action_rename,
            invalidToastRes = R.string.toast_saved_page_title_invalid,
            successToastRes = R.string.toast_saved_page_renamed,
            saveValue = { newTitle ->
                savedPageRepository.updateTitle(
                    collection = collection,
                    url = page.url,
                    title = newTitle
                )
            },
            onSaved = {
                showSavedPagesPage(collection, title, emptyMessage, true, query)
            }
        )
    }

    fun showMoveBookmarkFolderDialog(
        page: SavedPage,
        title: String,
        emptyMessage: String,
        query: String?
    ) {
        ValidatedTextInputDialog.show(
            activity = activity,
            titleRes = R.string.title_move_bookmark_folder,
            hintRes = R.string.hint_bookmark_folder,
            initialValue = page.folder,
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
            positiveButtonRes = R.string.action_save,
            invalidToastRes = R.string.toast_bookmark_folder_invalid,
            successToastRes = R.string.toast_bookmark_folder_updated,
            saveValue = { folder ->
                savedPageRepository.updateBookmarkFolder(
                    url = page.url,
                    folder = folder
                )
            },
            onSaved = {
                showSavedPagesPage(SavedPageCollection.BOOKMARKS, title, emptyMessage, true, query)
            }
        )
    }
}
