package com.example.videobrowser.functioncenter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.storage.SavedPageSearch
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
        replaceCurrent: Boolean = false,
        query: String? = null
    ) {
        val allPages = savedPageRepository.pages(collection)
        val pages = SavedPageSearch.filter(allPages, query)

        host.showPage(
            title = title,
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (allPages.isEmpty()) {
                host.addEmptyState(content, emptyMessage)
                return@showPage
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_search_saved_pages),
                    summary = currentSearchSummary(query)
                ) {
                    showSearchDialog(collection, title, emptyMessage, query)
                }
                if (!query.isNullOrBlank()) {
                    host.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_clear_search),
                        summary = query
                    ) {
                        show(
                            collection = collection,
                            title = title,
                            emptyMessage = emptyMessage,
                            replaceCurrent = true
                        )
                    }
                }
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
                if (pages.isEmpty()) {
                    host.addEmptyState(section, activity.getString(R.string.dialog_saved_pages_search_empty))
                    return@addFunctionSection
                }
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

    private fun showSearchDialog(
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
                show(
                    collection = collection,
                    title = title,
                    emptyMessage = emptyMessage,
                    replaceCurrent = true,
                    query = input.text?.toString()?.trim().orEmpty()
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showSavedPageActionsDialog(
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
            SavedPageAction(activity.getString(R.string.action_copy_link)) {
                copySavedPageUrl(page)
            },
            SavedPageAction(activity.getString(R.string.action_remove)) {
                savedPageRepository.remove(collection, page.url)
                Toast.makeText(
                    activity,
                    R.string.toast_saved_page_removed,
                    Toast.LENGTH_SHORT
                ).show()
                show(collection, title, emptyMessage, replaceCurrent = true)
            }
        )
    }

    private fun copySavedPageUrl(page: SavedPage) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                activity.getString(R.string.clipboard_page_url),
                page.url
            )
        )
        Toast.makeText(activity, R.string.toast_link_copied, Toast.LENGTH_SHORT).show()
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

    private fun currentSearchSummary(query: String?): String {
        return query
            ?.takeIf { it.isNotBlank() }
            ?: activity.getString(R.string.action_search_saved_pages_summary)
    }

    private data class SavedPageAction(
        val title: String,
        val perform: () -> Unit
    )
}
