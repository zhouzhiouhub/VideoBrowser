package com.example.videobrowser.functioncenter

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.UrlUtils

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
        emptyMessage: String
    ) {
        val pages = savedPageRepository.pages(collection)
        if (pages.isEmpty()) {
            Toast.makeText(activity, emptyMessage, Toast.LENGTH_SHORT).show()
            return
        }

        host.showPage(
            title = title,
            onBack = showRootPage
        ) { content ->
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
                        summary = UrlUtils.displayUrl(page.url)
                    ) {
                        loadUrl(page.url)
                    }
                }
            }
        }
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
                showRootPage()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
