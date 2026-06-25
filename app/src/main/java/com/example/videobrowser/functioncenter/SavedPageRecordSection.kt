package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.ShortDateTimeFormatter
import com.example.videobrowser.utils.UrlUtils

/**
 * 收藏/历史页面的记录区，负责分组、摘要和长按展开行内操作。
 */
internal class SavedPageRecordSection(
    private val host: FunctionCenterPageHost,
    private val inlineActionController: SavedPageInlineActionController,
    private val openPage: (SavedPage) -> Unit,
    private val showExpandedPage: (
        SavedPageCollection,
        String,
        String,
        String?,
        String?
    ) -> Unit
) {
    private val activity = host.activity
    private val historyRecordSection = SavedPageHistoryRecordSection(
        host = host,
        inlineActionController = inlineActionController,
        openPage = openPage,
        showExpandedPage = showExpandedPage
    )

    fun add(
        section: LinearLayout,
        collection: SavedPageCollection,
        pages: List<SavedPage>,
        title: String,
        emptyMessage: String,
        query: String?,
        expandedUrl: String?
    ) {
        if (collection == SavedPageCollection.BOOKMARKS) {
            addBookmarkGroups(section, pages, title, emptyMessage, query, expandedUrl)
        } else {
            historyRecordSection.add(
                section = section,
                pages = pages,
                title = title,
                emptyMessage = emptyMessage,
                query = query,
                expandedUrl = expandedUrl
            )
        }
    }

    private fun addBookmarkGroups(
        section: LinearLayout,
        pages: List<SavedPage>,
        title: String,
        emptyMessage: String,
        query: String?,
        expandedUrl: String?
    ) {
        bookmarkGroups(pages).forEachIndexed { index, group ->
            if (index > 0) {
                host.contentFactory.addDivider(section)
            }
            host.contentFactory.addInfoRow(
                parent = section,
                title = group.title,
                summary = activity.getString(R.string.bookmark_folder_count, group.pages.size)
            )
            addSavedPageRows(
                section = section,
                collection = SavedPageCollection.BOOKMARKS,
                pages = group.pages,
                title = title,
                emptyMessage = emptyMessage,
                query = query,
                expandedUrl = expandedUrl
            )
        }
    }

    private fun addSavedPageRows(
        section: LinearLayout,
        collection: SavedPageCollection,
        pages: List<SavedPage>,
        title: String,
        emptyMessage: String,
        query: String?,
        expandedUrl: String?
    ) {
        pages.forEach { page ->
            val expanded = page.url.equals(expandedUrl, ignoreCase = true)
            host.contentFactory.addActionRow(
                parent = section,
                title = page.title.ifBlank { page.url },
                summary = pageSummary(page),
                onClick = { openPage(page) },
                onLongClick = {
                    showExpandedPage(
                        collection,
                        title,
                        emptyMessage,
                        query,
                        if (expanded) null else page.url
                    )
                }
            )
            if (expanded) {
                inlineActionController.addActions(
                    section = section,
                    collection = collection,
                    page = page,
                    title = title,
                    emptyMessage = emptyMessage,
                    query = query
                )
            }
        }
    }

    private fun pageSummary(page: SavedPage): String {
        val timestamp = page.updatedAtMillis.takeIf { it > 0L }
        return listOfNotNull(
            page.folder.takeIf { it.isNotBlank() }?.let { folder ->
                activity.getString(R.string.bookmark_folder_summary, folder)
            },
            UrlUtils.displayUrl(page.url),
            timestamp?.let(ShortDateTimeFormatter::format)
        ).joinToString(" | ")
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

    private data class SavedPageGroup(
        val title: String,
        val pages: List<SavedPage>
    )
}
