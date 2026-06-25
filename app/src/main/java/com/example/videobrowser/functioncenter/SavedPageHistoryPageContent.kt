package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.utils.DensityPixelConverter

/**
 * 浏览历史页内容编排器，只负责组合头部、列表和固定选择操作条。
 */
internal class SavedPageHistoryPageContent(
    private val host: FunctionCenterPageHost
) {
    private val activity = host.activity
    private val headerSection = SavedPageHistoryHeaderSection(host)
    private val listSection = SavedPageHistoryListSection(host)
    private val selectionActionStrip = SavedPageHistorySelectionActionStrip(host)

    fun add(
        parent: LinearLayout,
        state: SavedPageHistoryPageState,
        actions: SavedPageHistoryPageActions
    ) {
        parent.setPadding(dp(18), dp(12), dp(18), dp(24))
        parent.setBackgroundColor(ContextCompat.getColor(activity, R.color.browser_surface))
        parent.minimumHeight = activity.resources.displayMetrics.heightPixels - dp(56)

        headerSection.add(parent, state, actions)
        if (state.allPages.isEmpty()) {
            listSection.addEmptyState(parent, state.emptyMessage)
            return
        }

        headerSection.addFilterChip(parent)
        if (state.visiblePages.isEmpty()) {
            listSection.addEmptyState(
                parent,
                activity.getString(R.string.dialog_saved_pages_search_empty)
            )
            return
        }

        listSection.add(parent, state, actions)
    }

    fun addSelectionFooter(
        parent: LinearLayout,
        state: SavedPageHistoryPageState,
        actions: SavedPageHistoryPageActions
    ) {
        if (state.selectedUrls.isNotEmpty()) {
            selectionActionStrip.add(
                section = parent,
                onSelectAll = { actions.onSelectAll(state.visiblePages) },
                onDelete = actions.onDeleteSelected,
                onDone = actions.onClearSelection
            )
        }
    }

    private fun dp(value: Int): Int {
        return DensityPixelConverter.truncateDp(value, activity.resources)
    }
}
