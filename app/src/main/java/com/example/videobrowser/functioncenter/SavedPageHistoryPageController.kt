package com.example.videobrowser.functioncenter

import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.ShortToast

/**
 * 浏览历史页控制器，集中维护历史分类、选择状态、批量删除和页面刷新。
 */
internal class SavedPageHistoryPageController(
    private val host: FunctionCenterPageHost,
    private val savedPageRepository: SavedPageRepository,
    private val dialogController: SavedPagesDialogController,
    private val loadUrl: (String) -> Unit,
    private val showRootPage: () -> Unit,
    private val showHistoryPage: (
        title: String,
        emptyMessage: String,
        replaceCurrent: Boolean,
        query: String?
    ) -> Unit
) {
    private val activity = host.activity
    private val historyPageContent = SavedPageHistoryPageContent(host)
    private var selectedHistoryCategory = SavedPageHistoryCategory.ALL
    private var selectedHistoryUrls: Set<String> = emptySet()
    private var currentHistoryQuery: String? = null

    fun reset() {
        selectedHistoryUrls = emptySet()
        currentHistoryQuery = null
    }

    fun show(
        allPages: List<SavedPage>,
        pages: List<SavedPage>,
        title: String,
        emptyMessage: String,
        replaceCurrent: Boolean,
        query: String?
    ) {
        syncHistoryState(query, replaceCurrent)
        val state = createHistoryPageState(allPages, pages, query, emptyMessage)
        val actions = createHistoryPageActions(title, emptyMessage, query)
        host.showPageWithFooter(
            title = title,
            onBack = showRootPage,
            replaceCurrent = replaceCurrent,
            buildContent = { content ->
                historyPageContent.add(
                    parent = content,
                    state = state,
                    actions = actions
                )
            },
            buildFooter = { footer ->
                historyPageContent.addSelectionFooter(
                    parent = footer,
                    state = state,
                    actions = actions
                )
            }
        )
    }

    private fun syncHistoryState(query: String?, replaceCurrent: Boolean) {
        if (!replaceCurrent) {
            selectedHistoryUrls = emptySet()
            selectedHistoryCategory = SavedPageHistoryCategory.ALL
        }
        val normalizedQuery = query?.trim()?.takeIf { it.isNotEmpty() }
        if (normalizedQuery != currentHistoryQuery) {
            selectedHistoryUrls = emptySet()
            selectedHistoryCategory = SavedPageHistoryCategory.ALL
            currentHistoryQuery = normalizedQuery
        }
    }

    private fun createHistoryPageState(
        allPages: List<SavedPage>,
        pages: List<SavedPage>,
        query: String?,
        emptyMessage: String
    ): SavedPageHistoryPageState {
        val visiblePages = selectedHistoryCategory.filter(pages)
        selectedHistoryUrls = selectedHistoryUrls
            .filter { selectedUrl -> visiblePages.any { page -> page.url.equals(selectedUrl, ignoreCase = true) } }
            .toSet()
        return SavedPageHistoryPageState(
            allPages = allPages,
            visiblePages = visiblePages,
            query = query,
            selectedCategory = selectedHistoryCategory,
            selectedUrls = selectedHistoryUrls,
            emptyMessage = emptyMessage
        )
    }

    private fun createHistoryPageActions(
        title: String,
        emptyMessage: String,
        query: String?
    ): SavedPageHistoryPageActions {
        return SavedPageHistoryPageActions(
            onSearch = {
                dialogController.showSearchDialog(
                    SavedPageCollection.HISTORY,
                    title,
                    emptyMessage,
                    query
                )
            },
            onClearSearch = {
                showHistoryPage(title, emptyMessage, true, null)
            },
            onSelectCategory = { category ->
                selectedHistoryCategory = category
                selectedHistoryUrls = emptySet()
                showHistoryPage(title, emptyMessage, true, query)
            },
            onOpenPage = { page -> loadUrl(page.url) },
            onToggleSelection = { page ->
                toggleHistorySelection(page.url, title, emptyMessage, query)
            },
            onSelectAll = { visibleHistoryPages ->
                selectedHistoryUrls = visibleHistoryPages.map { page -> page.url }.toSet()
                showHistoryPage(title, emptyMessage, true, query)
            },
            onDeleteSelected = {
                deleteSelectedHistory(title, emptyMessage, query)
            },
            onClearSelection = {
                selectedHistoryUrls = emptySet()
                showHistoryPage(title, emptyMessage, true, query)
            }
        )
    }

    private fun toggleHistorySelection(
        url: String,
        title: String,
        emptyMessage: String,
        query: String?
    ) {
        selectedHistoryUrls = if (selectedHistoryUrls.any { it.equals(url, ignoreCase = true) }) {
            selectedHistoryUrls.filterNot { it.equals(url, ignoreCase = true) }.toSet()
        } else {
            selectedHistoryUrls + url
        }
        showHistoryPage(title, emptyMessage, true, query)
    }

    private fun deleteSelectedHistory(
        title: String,
        emptyMessage: String,
        query: String?
    ) {
        val removedCount = savedPageRepository.removeAll(
            SavedPageCollection.HISTORY,
            selectedHistoryUrls
        )
        selectedHistoryUrls = emptySet()
        if (removedCount > 0) {
            ShortToast.show(activity, R.string.toast_saved_page_removed)
        }
        showHistoryPage(title, emptyMessage, true, query)
    }
}
