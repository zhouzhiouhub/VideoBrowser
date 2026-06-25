package com.example.videobrowser.functioncenter

import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.utils.BrowserDrawableFactory
import com.example.videobrowser.utils.DensityPixelConverter
import com.example.videobrowser.utils.setBoundedSelectableItemBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 浏览历史页记录列表：日期分组、单行排版、时间列和选择框。
 */
internal class SavedPageHistoryListSection(
    private val host: FunctionCenterPageHost
) {
    private val activity = host.activity

    fun add(
        parent: LinearLayout,
        state: SavedPageHistoryPageState,
        actions: SavedPageHistoryPageActions
    ) {
        groupedByDate(state.visiblePages).forEach { group ->
            addDateChip(parent, group.dateLabel)
            group.pages.forEach { page ->
                addHistoryRow(parent, page, state, actions)
            }
        }
    }

    fun addEmptyState(parent: LinearLayout, message: String) {
        parent.addView(
            TextView(activity).apply {
                text = message
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(activity, R.color.browser_text_hint))
                textSize = 15f
                setPadding(dp(16), dp(36), dp(16), dp(36))
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addDateChip(parent: LinearLayout, dateLabel: String) {
        parent.addView(
            TextView(activity).apply {
                text = dateLabel
                includeFontPadding = false
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(activity, R.color.browser_text_hint))
                textSize = 14f
                background = BrowserDrawableFactory.roundedBackground(
                    ContextCompat.getColor(activity, R.color.browser_control_pressed),
                    dp(8).toFloat()
                )
                setPadding(dp(12), 0, dp(12), 0)
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(30)
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
        )
    }

    private fun addHistoryRow(
        parent: LinearLayout,
        page: SavedPage,
        state: SavedPageHistoryPageState,
        actions: SavedPageHistoryPageActions
    ) {
        val selected = state.selectedUrls.containsPage(page)
        val selectionActive = state.selectedUrls.isNotEmpty()
        parent.addView(
            LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                addView(
                    LinearLayout(activity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        minimumHeight = dp(64)
                        setPadding(0, dp(8), 0, dp(8))
                        isClickable = true
                        isFocusable = true
                        setBoundedSelectableItemBackground()
                        setOnClickListener {
                            if (selectionActive) {
                                actions.onToggleSelection(page)
                            } else {
                                actions.onOpenPage(page)
                            }
                        }
                        setOnLongClickListener {
                            actions.onToggleSelection(page)
                            true
                        }
                        if (selectionActive) {
                            addView(
                                createSelectionBox(selected).apply {
                                    setOnClickListener { actions.onToggleSelection(page) }
                                },
                                LinearLayout.LayoutParams(dp(28), dp(28)).apply {
                                    marginStart = dp(2)
                                    marginEnd = dp(24)
                                }
                            )
                        }
                        addView(
                            createTitle(page),
                            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        )
                        addView(
                            createTime(page),
                            LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                marginStart = dp(12)
                            }
                        )
                    },
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
                addView(
                    View(activity).apply {
                        setBackgroundColor(
                            ContextCompat.getColor(activity, R.color.browser_control_pressed)
                        )
                    },
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)).apply {
                        marginStart = if (selectionActive) dp(74) else 0
                    }
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createSelectionBox(selected: Boolean): TextView {
        return TextView(activity).apply {
            text = if (selected) "\u2713" else ""
            gravity = Gravity.CENTER
            includeFontPadding = false
            typeface = Typeface.DEFAULT_BOLD
            textSize = 16f
            setTextColor(ContextCompat.getColor(activity, R.color.browser_surface))
            background = BrowserDrawableFactory.roundedBackground(
                ContextCompat.getColor(
                    activity,
                    if (selected) R.color.browser_text else R.color.browser_control_pressed
                ),
                dp(6).toFloat(),
                strokeWidth = if (selected) 0 else dp(1),
                strokeColor = ContextCompat.getColor(activity, R.color.browser_control_pressed)
            )
            isClickable = true
            isFocusable = true
        }
    }

    private fun createTitle(page: SavedPage): TextView {
        return TextView(activity).apply {
            text = page.title.ifBlank { page.url }
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(activity, R.color.browser_text))
            textSize = 17f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
    }

    private fun createTime(page: SavedPage): TextView {
        return TextView(activity).apply {
            text = formatTime(page.updatedAtMillis)
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(activity, R.color.browser_text_hint))
            textSize = 14f
        }
    }

    private fun groupedByDate(pages: List<SavedPage>): List<HistoryDateGroup> {
        return pages
            .groupBy { page -> formatDateChip(page.updatedAtMillis) }
            .map { (dateLabel, groupPages) -> HistoryDateGroup(dateLabel, groupPages) }
    }

    private fun formatDateChip(timestampMillis: Long): String {
        return if (timestampMillis > 0L) {
            dateFormatter.format(Date(timestampMillis))
        } else {
            activity.getString(R.string.history_date_unknown)
        }
    }

    private fun formatTime(timestampMillis: Long): String {
        return if (timestampMillis > 0L) {
            timeFormatter.format(Date(timestampMillis))
        } else {
            ""
        }
    }

    private fun Set<String>.containsPage(page: SavedPage): Boolean {
        return any { url -> url.equals(page.url, ignoreCase = true) }
    }

    private fun dp(value: Int): Int {
        return DensityPixelConverter.truncateDp(value, activity.resources)
    }

    private data class HistoryDateGroup(
        val dateLabel: String,
        val pages: List<SavedPage>
    )

    private companion object {
        private val dateFormatter = SimpleDateFormat("yyyy-MM-dd E", Locale.CHINA)
        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.CHINA)
    }
}
