package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.ShortToast

/**
 * 在收藏/历史列表中渲染长按展开的单条记录操作。
 */
internal class SavedPageInlineActionController(
    private val host: FunctionCenterPageHost,
    private val savedPageRepository: SavedPageRepository,
    private val dialogController: SavedPagesDialogController,
    private val linkActions: SavedPageLinkActions,
    private val openUrlInNewTab: (String) -> Unit,
    private val refreshSavedPagesPage: (SavedPageCollection, String, String, String?) -> Unit
) {
    private val activity = host.activity

    fun addActions(
        section: LinearLayout,
        collection: SavedPageCollection,
        page: SavedPage,
        title: String,
        emptyMessage: String,
        query: String?
    ) {
        if (collection == SavedPageCollection.HISTORY) {
            addCopyAction(section, page)
            addRemoveAction(section, collection, page, title, emptyMessage, query)
            return
        }
        host.contentFactory.addActionRow(
            parent = section,
            title = activity.getString(R.string.action_edit),
            summary = activity.getString(R.string.action_edit_saved_page_title_summary)
        ) {
            dialogController.showRenameSavedPageDialog(collection, page, title, emptyMessage, query)
        }
        if (collection == SavedPageCollection.BOOKMARKS) {
            host.contentFactory.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_move_bookmark_folder),
                summary = activity.getString(R.string.action_move_bookmark_folder_summary)
            ) {
                dialogController.showMoveBookmarkFolderDialog(page, title, emptyMessage, query)
            }
        }
        host.contentFactory.addActionRow(
            parent = section,
            title = activity.getString(R.string.action_open_in_new_tab),
            summary = activity.getString(R.string.action_open_saved_page_new_tab_summary)
        ) {
            openUrlInNewTab(page.url)
        }
        addCopyAction(section, page)
        host.contentFactory.addActionRow(
            parent = section,
            title = activity.getString(R.string.action_share_page),
            summary = activity.getString(R.string.action_share_saved_page_summary)
        ) {
            linkActions.shareUrl(page)
        }
        addRemoveAction(section, collection, page, title, emptyMessage, query)
    }

    private fun addCopyAction(section: LinearLayout, page: SavedPage) {
        host.contentFactory.addActionRow(
            parent = section,
            title = activity.getString(R.string.action_copy_link),
            summary = activity.getString(R.string.action_copy_saved_page_link_summary)
        ) {
            linkActions.copyUrl(page)
        }
    }

    private fun addRemoveAction(
        section: LinearLayout,
        collection: SavedPageCollection,
        page: SavedPage,
        title: String,
        emptyMessage: String,
        query: String?
    ) {
        host.contentFactory.addActionRow(
            parent = section,
            title = activity.getString(R.string.action_remove),
            summary = activity.getString(R.string.action_remove_saved_page_summary)
        ) {
            removePage(collection, page, title, emptyMessage, query)
        }
    }

    private fun removePage(
        collection: SavedPageCollection,
        page: SavedPage,
        title: String,
        emptyMessage: String,
        query: String?
    ) {
        if (savedPageRepository.remove(collection, page.url)) {
            ShortToast.show(activity, R.string.toast_saved_page_removed)
            refreshSavedPagesPage(collection, title, emptyMessage, query)
        }
    }
}
