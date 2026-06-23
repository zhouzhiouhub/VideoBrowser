package com.example.videobrowser.functioncenter

import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.ActionListDialog
import com.example.videobrowser.utils.ConfirmationDialog
import com.example.videobrowser.utils.DialogAction
import com.example.videobrowser.utils.ShortToast
import com.example.videobrowser.utils.ValidatedTextInputDialog

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
        SearchQueryDialog.show(
            activity = activity,
            titleRes = R.string.action_search_saved_pages,
            hintRes = R.string.hint_saved_pages_search,
            currentQuery = currentQuery
        ) { query ->
            showSavedPagesPage(collection, title, emptyMessage, true, query)
        }
    }

    fun showSavedPageActionsDialog(
        collection: SavedPageCollection,
        page: SavedPage,
        title: String,
        emptyMessage: String
    ) {
        val actions = savedPageActions(collection, page, title, emptyMessage)
        ActionListDialog.show(
            activity = activity,
            title = page.title.ifBlank { page.url },
            actions = actions,
            negativeButtonRes = android.R.string.cancel
        )
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

    private fun savedPageActions(
        collection: SavedPageCollection,
        page: SavedPage,
        title: String,
        emptyMessage: String
    ): List<DialogAction> {
        return listOf(
            DialogAction(activity.getString(R.string.action_open_page)) {
                loadUrl(page.url)
            },
            DialogAction(activity.getString(R.string.action_open_in_new_tab)) {
                openUrlInNewTab(page.url)
            },
            if (collection == SavedPageCollection.BOOKMARKS) {
                DialogAction(activity.getString(R.string.action_rename)) {
                    showRenameBookmarkDialog(page, title, emptyMessage)
                }
            } else {
                null
            },
            if (collection == SavedPageCollection.BOOKMARKS) {
                DialogAction(activity.getString(R.string.action_move_bookmark_folder)) {
                    showMoveBookmarkFolderDialog(page, title, emptyMessage)
                }
            } else {
                null
            },
            DialogAction(activity.getString(R.string.action_copy_link)) {
                linkActions.copyUrl(page)
            },
            DialogAction(activity.getString(R.string.action_share_page)) {
                linkActions.shareUrl(page)
            },
            DialogAction(activity.getString(R.string.action_remove)) {
                savedPageRepository.remove(collection, page.url)
                ShortToast.show(activity, R.string.toast_saved_page_removed)
                showSavedPagesPage(collection, title, emptyMessage, true, null)
            }
        ).filterNotNull()
    }

    private fun showRenameBookmarkDialog(
        page: SavedPage,
        title: String,
        emptyMessage: String
    ) {
        ValidatedTextInputDialog.show(
            activity = activity,
            titleRes = R.string.title_rename_bookmark,
            hintRes = R.string.hint_saved_page_title,
            initialValue = page.title.ifBlank { page.url },
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
            positiveButtonRes = R.string.action_rename,
            invalidToastRes = R.string.toast_saved_page_title_invalid,
            successToastRes = R.string.toast_saved_page_renamed,
            saveValue = { newTitle ->
                savedPageRepository.updateTitle(
                    collection = SavedPageCollection.BOOKMARKS,
                    url = page.url,
                    title = newTitle
                )
            },
            onSaved = {
                showSavedPagesPage(SavedPageCollection.BOOKMARKS, title, emptyMessage, true, null)
            }
        )
    }

    private fun showMoveBookmarkFolderDialog(
        page: SavedPage,
        title: String,
        emptyMessage: String
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
                showSavedPagesPage(SavedPageCollection.BOOKMARKS, title, emptyMessage, true, null)
            }
        )
    }
}
