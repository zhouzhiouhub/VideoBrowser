package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 SavedPagesPage 可以拆开理解为“Saved Pages Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
    private val openUrlInNewTab: (String) -> Unit,
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
                parent = content,
                title = activity.getString(R.string.function_center_section_records)
            ) { section ->
                if (pages.isEmpty()) {
                    host.addEmptyState(section, activity.getString(R.string.dialog_saved_pages_search_empty))
                    return@addFunctionSection
                }
                if (collection == SavedPageCollection.BOOKMARKS) {
                    addBookmarkGroups(section, pages, title, emptyMessage)
                } else {
                    addSavedPageRows(section, collection, pages, title, emptyMessage)
                }
            }
        }
    }

    private fun addBookmarkGroups(
        section: android.widget.LinearLayout,
        pages: List<SavedPage>,
        title: String,
        emptyMessage: String
    ) {
        bookmarkGroups(pages).forEachIndexed { index, group ->
            if (index > 0) {
                host.addDivider(section)
            }
            host.addInfoRow(
                parent = section,
                title = group.title,
                summary = activity.getString(R.string.bookmark_folder_count, group.pages.size)
            )
            addSavedPageRows(section, SavedPageCollection.BOOKMARKS, group.pages, title, emptyMessage)
        }
    }

    private fun addSavedPageRows(
        section: android.widget.LinearLayout,
        collection: SavedPageCollection,
        pages: List<SavedPage>,
        title: String,
        emptyMessage: String
    ) {
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
                copySavedPageUrl(page)
            },
            SavedPageAction(activity.getString(R.string.action_share_page)) {
                shareSavedPageUrl(page)
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
                show(SavedPageCollection.BOOKMARKS, title, emptyMessage, replaceCurrent = true)
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
                show(SavedPageCollection.BOOKMARKS, title, emptyMessage, replaceCurrent = true)
            }
        }
        dialog.show()
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

    private fun shareSavedPageUrl(page: SavedPage) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, page.url)
        }
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.action_share_page)))
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
            page.folder.takeIf { it.isNotBlank() }?.let { folder ->
                activity.getString(R.string.bookmark_folder_summary, folder)
            },
            UrlUtils.displayUrl(page.url),
            timestamp?.let { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(it)) }
        ).joinToString(" | ")
    }

    private fun currentSearchSummary(query: String?): String {
        return query
            ?.takeIf { it.isNotBlank() }
            ?: activity.getString(R.string.action_search_saved_pages_summary)
    }

    private fun bookmarkGroups(pages: List<SavedPage>): List<SavedPageGroup> {
        val unfiled = pages.filter { page -> page.folder.isBlank() }
        val folderGroups = pages
            .filter { page -> page.folder.isNotBlank() }
            .groupBy { page -> page.folder }
            .toSortedMap()
            .map { (folder, folderPages) ->
                SavedPageGroup(title = folder, pages = folderPages)
            }
        return listOfNotNull(
            SavedPageGroup(
                title = activity.getString(R.string.bookmark_folder_unfiled),
                pages = unfiled
            ).takeIf { group -> group.pages.isNotEmpty() }
        ) + folderGroups
    }

    private data class SavedPageAction(
        val title: String,
        val perform: () -> Unit
    )

    private data class SavedPageGroup(
        val title: String,
        val pages: List<SavedPage>
    )
}
