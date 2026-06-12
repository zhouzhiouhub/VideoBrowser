package com.example.videobrowser.functioncenter

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.UrlUtils
import java.text.DateFormat
import java.util.Date

class SavedPagesPage(
    private val host: FunctionCenterPageHost,
    private val savedPageRepository: SavedPageRepository,
    private val loadUrl: (String) -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show(
        collection: SavedPageCollection,
        title: String,
        emptyMessage: String,
        replaceCurrent: Boolean = false
    ) {
        val pages = savedPageRepository.pages(collection)

        host.showPage(
            title = title,
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (pages.isEmpty()) {
                host.addEmptyState(content, emptyMessage)
                return@showPage
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_saved_pages_summary)
                ) {
                    showClearSavedPagesDialog(collection)
                }
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_records)
            ) { section ->
                pages.forEach { page ->
                    host.addActionRow(
                        parent = section,
                        title = page.title.ifBlank { page.url },
                        summary = pageSummary(page)
                    ) {
                        showSavedPageActionsDialog(collection, page, title, emptyMessage)
                    }
                }
            }
        }
    }

    private fun showSavedPageActionsDialog(
        collection: SavedPageCollection,
        page: SavedPage,
        title: String,
        emptyMessage: String
    ) {
        AlertDialog.Builder(activity)
            .setTitle(page.title.ifBlank { page.url })
            .setMessage(UrlUtils.displayUrl(page.url))
            .setPositiveButton(R.string.action_open_page) { _, _ -> loadUrl(page.url) }
            .setNeutralButton(R.string.action_remove) { _, _ ->
                savedPageRepository.remove(collection, page.url)
                Toast.makeText(
                    activity,
                    R.string.toast_saved_page_removed,
                    Toast.LENGTH_SHORT
                ).show()
                show(collection, title, emptyMessage, replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showClearSavedPagesDialog(collection: SavedPageCollection) {
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
                show(
                    collection = collection,
                    title = collectionTitle(collection),
                    emptyMessage = collectionEmptyMessage(collection),
                    replaceCurrent = true
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun collectionTitle(collection: SavedPageCollection): String {
        return when (collection) {
            SavedPageCollection.BOOKMARKS -> activity.getString(R.string.title_bookmarks)
            SavedPageCollection.HISTORY -> activity.getString(R.string.title_history)
        }
    }

    private fun collectionEmptyMessage(collection: SavedPageCollection): String {
        return when (collection) {
            SavedPageCollection.BOOKMARKS -> activity.getString(R.string.toast_bookmarks_empty)
            SavedPageCollection.HISTORY -> activity.getString(R.string.toast_history_empty)
        }
    }

    private fun pageSummary(page: SavedPage): String {
        val timestamp = page.updatedAtMillis.takeIf { it > 0L }
        return listOfNotNull(
            UrlUtils.displayUrl(page.url),
            timestamp?.let { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(it)) }
        ).joinToString(" | ")
    }
}
