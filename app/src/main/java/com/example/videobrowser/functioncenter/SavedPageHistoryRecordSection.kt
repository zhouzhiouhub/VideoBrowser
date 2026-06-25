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
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.utils.BrowserDrawableFactory
import com.example.videobrowser.utils.DensityPixelConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 历史记录专用列表渲染器，负责日期胶囊、时间列和长按选中态。
 */
internal class SavedPageHistoryRecordSection(
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

    fun add(
        section: LinearLayout,
        pages: List<SavedPage>,
        title: String,
        emptyMessage: String,
        query: String?,
        expandedUrl: String?
    ) {
        addHistoryFilterChip(section)
        groupedByDate(pages).forEach { group ->
            addDateChip(section, group.dateLabel)
            group.pages.forEach { page ->
                val selected = page.url.equals(expandedUrl, ignoreCase = true)
                addHistoryRow(
                    section = section,
                    page = page,
                    selected = selected,
                    title = title,
                    emptyMessage = emptyMessage,
                    query = query
                )
            }
        }
    }

    private fun addHistoryRow(
        section: LinearLayout,
        page: SavedPage,
        selected: Boolean,
        title: String,
        emptyMessage: String,
        query: String?
    ) {
        section.addView(
            createHistoryRow(page, selected, title, emptyMessage, query),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        if (selected) {
            inlineActionController.addActions(
                section = section,
                collection = SavedPageCollection.HISTORY,
                page = page,
                title = title,
                emptyMessage = emptyMessage,
                query = query
            )
        }
    }

    private fun createHistoryRow(
        page: SavedPage,
        selected: Boolean,
        title: String,
        emptyMessage: String,
        query: String?
    ): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(62)
            isClickable = true
            isFocusable = true
            setPadding(if (selected) dp(8) else 0, dp(6), 0, dp(6))
            if (selected) {
                background = BrowserDrawableFactory.roundedBackground(
                    ContextCompat.getColor(activity, R.color.browser_control_pressed),
                    dp(8).toFloat()
                )
                addView(createSelectedMarker(), LinearLayout.LayoutParams(dp(18), dp(18)))
            }
            addView(
                createTitle(page),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = if (selected) dp(12) else 0
                }
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
            setOnClickListener { openPage(page) }
            setOnLongClickListener {
                showExpandedPage(
                    SavedPageCollection.HISTORY,
                    title,
                    emptyMessage,
                    query,
                    if (selected) null else page.url
                )
                true
            }
        }
    }

    private fun addHistoryFilterChip(section: LinearLayout) {
        section.addView(
            TextView(activity).apply {
                text = activity.getString(R.string.history_filter_all)
                includeFontPadding = false
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(ContextCompat.getColor(activity, R.color.browser_primary))
                textSize = 13f
                background = BrowserDrawableFactory.roundedBackground(
                    ContextCompat.getColor(activity, R.color.browser_provider_circle),
                    dp(8).toFloat()
                )
                setPadding(dp(12), 0, dp(12), 0)
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(34)
            ).apply {
                topMargin = dp(4)
                bottomMargin = dp(12)
            }
        )
    }

    private fun addDateChip(section: LinearLayout, dateLabel: String) {
        section.addView(
            TextView(activity).apply {
                text = dateLabel
                includeFontPadding = false
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(activity, R.color.browser_text_hint))
                textSize = 13f
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
                topMargin = dp(10)
                bottomMargin = dp(4)
            }
        )
    }

    private fun createSelectedMarker(): View {
        return View(activity).apply {
            background = BrowserDrawableFactory.roundedBackground(
                ContextCompat.getColor(activity, R.color.browser_primary),
                dp(5).toFloat()
            )
        }
    }

    private fun createTitle(page: SavedPage): TextView {
        return TextView(activity).apply {
            text = page.title.ifBlank { page.url }
            setTextColor(ContextCompat.getColor(activity, R.color.browser_text))
            textSize = 15f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
    }

    private fun createTime(page: SavedPage): TextView {
        return TextView(activity).apply {
            text = formatTime(page.updatedAtMillis)
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(activity, R.color.browser_text_hint))
            textSize = 13f
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
