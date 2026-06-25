package com.example.videobrowser.functioncenter

import com.example.videobrowser.storage.SavedPage

internal data class SavedPageHistoryPageState(
    val allPages: List<SavedPage>,
    val visiblePages: List<SavedPage>,
    val query: String?,
    val selectedCategory: SavedPageHistoryCategory,
    val selectedUrls: Set<String>,
    val emptyMessage: String
)

internal data class SavedPageHistoryPageActions(
    val onShowBookmarks: () -> Unit,
    val onShowHistory: () -> Unit,
    val onSearch: () -> Unit,
    val onClearSearch: () -> Unit,
    val onSelectCategory: (SavedPageHistoryCategory) -> Unit,
    val onOpenPage: (SavedPage) -> Unit,
    val onToggleSelection: (SavedPage) -> Unit,
    val onSelectAll: (List<SavedPage>) -> Unit,
    val onDeleteSelected: () -> Unit,
    val onClearSelection: () -> Unit
)
