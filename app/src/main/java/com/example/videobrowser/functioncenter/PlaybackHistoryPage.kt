package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 PlaybackHistoryPage 可以拆开理解为“Playback History Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
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
