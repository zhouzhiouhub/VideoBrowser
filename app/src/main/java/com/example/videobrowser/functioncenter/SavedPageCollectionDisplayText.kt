package com.example.videobrowser.functioncenter

import android.content.Context
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection

internal object SavedPageCollectionDisplayText {
    fun title(context: Context, collection: SavedPageCollection): String {
        return when (collection) {
            SavedPageCollection.BOOKMARKS -> context.getString(R.string.title_bookmarks)
            SavedPageCollection.HISTORY -> context.getString(R.string.title_history)
        }
    }

    fun emptyMessage(context: Context, collection: SavedPageCollection): String {
        return when (collection) {
            SavedPageCollection.BOOKMARKS -> context.getString(R.string.toast_bookmarks_empty)
            SavedPageCollection.HISTORY -> context.getString(R.string.toast_history_empty)
        }
    }
}
