package com.example.videobrowser.functioncenter

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.video.PlaybackHistoryRepository
import com.example.videobrowser.video.PlaybackProgress

class PlaybackHistoryPage(
    private val host: FunctionCenterPageHost,
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    private val openPlaybackHistoryItem: (PlaybackProgress) -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity

    fun show(replaceCurrent: Boolean = false) {
        val records = playbackHistoryRepository.records()

        host.showPage(
            title = activity.getString(R.string.title_playback_history),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (records.isNotEmpty()) {
                host.addFunctionSection(
                    content,
                    activity.getString(R.string.function_center_section_actions)
                ) { section ->
                    host.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_clear),
                        summary = activity.getString(R.string.action_clear_playback_history_summary)
                    ) {
                        confirmClearRecords()
                    }
                }
            }

            if (records.isEmpty()) {
                host.addEmptyState(content, activity.getString(R.string.dialog_playback_history_empty))
                return@showPage
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_records)
            ) { section ->
                records.forEach { record ->
                    host.addActionRow(
                        parent = section,
                        title = PlaybackHistoryDisplayText.title(record),
                        summary = PlaybackHistoryDisplayText.summary(record)
                    ) {
                        host.close()
                        openPlaybackHistoryItem(record)
                    }
                }
            }
        }
    }

    private fun confirmClearRecords() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear)
            .setMessage(R.string.dialog_clear_playback_history_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                playbackHistoryRepository.clear()
                Toast.makeText(activity, R.string.toast_playback_history_cleared, Toast.LENGTH_SHORT).show()
                show(replaceCurrent = true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
