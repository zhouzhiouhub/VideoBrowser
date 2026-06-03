package com.example.videobrowser.functioncenter

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage

data class FunctionCenterGridAction(
    val title: String,
    val summary: String,
    val iconResId: Int,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

class FunctionCenterViewFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int
) {
    fun createPage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ): View {
        val page = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true
            setBackgroundColor(
                ContextCompat.getColor(activity, R.color.browser_background)
            )
        }
        page.addView(
            createToolbar(title, onBack),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
            )
        )

        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(24))
        }
        buildContent(content)

        val scrollView = ScrollView(activity).apply {
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        page.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        return page
    }

    fun createBottomSheetPage(
        title: String,
        onBack: (() -> Unit)?,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ): View {
        val overlay = FrameLayout(activity).apply {
            isClickable = true
            isFocusable = true
            setBackgroundColor(ContextCompat.getColor(activity, R.color.browser_sheet_scrim))
            setOnClickListener { onClose() }
        }

        val sheet = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true
            elevation = dp(12).toFloat()
            background = createBottomSheetBackground(
                ContextCompat.getColor(activity, R.color.browser_surface)
            )
        }

        sheet.addView(
            createSheetHandle(),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(18)
            )
        )
        sheet.addView(
            createSheetToolbar(title, onBack, onClose),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
            )
        )

        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), 0, dp(14), dp(20))
        }
        buildContent(content)

        val scrollView = ScrollView(activity).apply {
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        sheet.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val displayHeight = activity.resources.displayMetrics.heightPixels
        val sheetHeight = (displayHeight * 0.54f)
            .toInt()
            .coerceAtLeast(dp(360))
            .coerceAtMost(displayHeight - dp(24))
        overlay.addView(
            sheet,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                sheetHeight
            ).apply {
                gravity = Gravity.BOTTOM
            }
        )
        return overlay
    }

    fun addProfileHeader(
        parent: LinearLayout,
        title: String,
        summary: String,
        onClick: () -> Unit
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = createRoundedBackground(
                ContextCompat.getColor(activity, R.color.browser_card_surface),
                dp(12).toFloat()
            )
            setOnClickListener { onClick() }
        }
        row.addView(
            ImageView(activity).apply {
                setImageResource(R.drawable.ic_settings_24)
                setColorFilter(ContextCompat.getColor(activity, R.color.browser_primary))
                background = ContextCompat.getDrawable(activity, R.drawable.bg_profile_avatar)
                setPadding(dp(9), dp(9), dp(9), dp(9))
            },
            LinearLayout.LayoutParams(dp(44), dp(44))
        )
        row.addView(
            createRowText(title, summary),
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(12)
            }
        )
        parent.addView(
            row,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        )
    }

    fun addBenefitStrip(parent: LinearLayout, leftTitle: String, leftSummary: String, rightTitle: String, rightSummary: String) {
        val strip = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = createRoundedBackground(Color.parseColor("#FFF8DF"), dp(12).toFloat())
        }
        strip.addView(createRowText(leftTitle, leftSummary), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        strip.addView(
            View(activity).apply {
                setBackgroundColor(Color.parseColor("#F1E3B8"))
            },
            LinearLayout.LayoutParams(dp(1), dp(36)).apply {
                marginStart = dp(10)
                marginEnd = dp(10)
            }
        )
        strip.addView(createRowText(rightTitle, rightSummary), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        parent.addView(
            strip,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )
    }

    fun addHistoryPreview(
        parent: LinearLayout,
        title: String,
        emptyMessage: String,
        pages: List<SavedPage>,
        onOpenPage: (SavedPage) -> Unit,
        onShowHistory: () -> Unit
    ) {
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(8))
            background = createRoundedBackground(
                ContextCompat.getColor(activity, R.color.browser_card_surface),
                dp(12).toFloat()
            )
        }
        val header = TextView(activity).apply {
            text = title
            setTextColor(ContextCompat.getColor(activity, R.color.browser_text))
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
            isClickable = true
            isFocusable = true
            setOnClickListener { onShowHistory() }
        }
        card.addView(header)
        if (pages.isEmpty()) {
            card.addView(
                TextView(activity).apply {
                    text = emptyMessage
                    setTextColor(ContextCompat.getColor(activity, R.color.browser_text_hint))
                    textSize = 13f
                    setPadding(0, dp(14), 0, dp(10))
                }
            )
        } else {
            pages.take(2).forEach { page ->
                card.addView(createHistoryPreviewRow(page, onOpenPage))
            }
        }
        parent.addView(
            card,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        )
    }

    fun addFunctionSection(
        parent: LinearLayout,
        title: String,
        buildContent: (LinearLayout) -> Unit
    ) {
        if (title.isNotBlank()) {
            addSectionTitle(parent, title)
        }
        val section = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(4), dp(14), dp(4))
            background = createRoundedBackground(
                ContextCompat.getColor(activity, R.color.browser_surface)
            )
        }
        buildContent(section)
        parent.addView(
            section,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    fun addInfoRow(
        parent: LinearLayout,
        title: String,
        summary: String
    ) {
        val row = createRowText(title, summary).apply {
            minimumHeight = dp(58)
            setPadding(0, dp(9), 0, dp(9))
        }
        parent.addView(
            row,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    fun addFunctionMessage(parent: LinearLayout, message: String) {
        parent.addView(
            TextView(activity).apply {
                text = message
                setTextColor(ContextCompat.getColor(activity, R.color.browser_text))
                textSize = 15f
                setLineSpacing(dp(2).toFloat(), 1f)
                setPadding(dp(16), dp(16), dp(16), dp(16))
                background = createRoundedBackground(
                    ContextCompat.getColor(activity, R.color.browser_surface)
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )
    }

    fun addEmptyState(parent: LinearLayout, message: String) {
        parent.addView(
            TextView(activity).apply {
                text = message
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(activity, R.color.browser_text_hint))
                textSize = 14f
                setPadding(dp(16), dp(28), dp(16), dp(28))
                background = createRoundedBackground(
                    ContextCompat.getColor(activity, R.color.browser_surface)
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(18)
            }
        )
    }

    fun addFunctionActionButton(
        parent: LinearLayout,
        title: String,
        backgroundColor: Int? = null,
        onClick: () -> Unit
    ) {
        val resolvedBackgroundColor =
            backgroundColor ?: ContextCompat.getColor(activity, R.color.browser_primary)
        parent.addView(
            TextView(activity).apply {
                text = title
                gravity = Gravity.CENTER
                includeFontPadding = false
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                textSize = 15f
                isClickable = true
                isFocusable = true
                background = createRoundedBackground(resolvedBackgroundColor)
                setOnClickListener { onClick() }
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
        )
    }

    fun addActionGrid(
        parent: LinearLayout,
        actions: List<FunctionCenterGridAction>
    ) {
        actions.chunked(5).forEachIndexed { rowIndex, rowActions ->
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            rowActions.forEachIndexed { columnIndex, action ->
                row.addView(
                    createGridActionView(action),
                    LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        if (columnIndex == 0) {
                            marginEnd = dp(2)
                        } else {
                            marginStart = dp(2)
                        }
                    }
                )
            }
            if (rowActions.size == 1) {
                row.addView(
                    View(activity),
                    LinearLayout.LayoutParams(0, dp(1), 1f).apply {
                        marginStart = dp(2)
                    }
                )
            }
            parent.addView(
                row,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (rowIndex > 0) {
                        topMargin = dp(4)
                    }
                }
            )
        }
    }

    fun addSwitchRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        checked: Boolean,
        enabled: Boolean = true,
        onChanged: (Boolean) -> Unit
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = enabled
            isFocusable = enabled
            isEnabled = enabled
            minimumHeight = dp(62)
            setPadding(0, dp(8), 0, dp(8))
            setBoundedSelectableItemBackground()
        }
        val labels = createRowText(title, summary).apply {
            isEnabled = enabled
            alpha = if (enabled) 1f else 0.48f
        }
        row.addView(
            labels,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        val switchView = SwitchCompat(activity).apply {
            isChecked = checked
            isEnabled = enabled
        }
        row.addView(
            switchView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        switchView.setOnCheckedChangeListener { _, isChecked -> onChanged(isChecked) }
        row.setOnClickListener {
            if (enabled) {
                switchView.isChecked = !switchView.isChecked
            }
        }
        parent.addView(
            row,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    fun addActionRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        val row = createRowText(title, summary).apply {
            isClickable = enabled
            isFocusable = enabled
            isEnabled = enabled
            alpha = if (enabled) 1f else 0.48f
            minimumHeight = dp(58)
            setPadding(0, dp(9), 0, dp(9))
            setBoundedSelectableItemBackground()
            if (enabled) {
                setOnClickListener { onClick() }
            }
        }
        parent.addView(
            row,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    fun addDivider(parent: LinearLayout) {
        parent.addView(
            View(activity).apply {
                setBackgroundColor(
                    ContextCompat.getColor(activity, R.color.browser_control_pressed)
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
        )
    }

    private fun createToolbar(title: String, onBack: () -> Unit): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            elevation = dp(4).toFloat()
            setPadding(dp(4), 0, dp(12), 0)
            setBackgroundColor(ContextCompat.getColor(activity, R.color.browser_surface))

            val pageBackButton = ImageButton(activity).apply {
                setImageResource(R.drawable.ic_arrow_back_24)
                setColorFilter(ContextCompat.getColor(activity, R.color.browser_icon))
                background = ContextCompat.getDrawable(activity, R.drawable.bg_icon_button)
                contentDescription = activity.getString(R.string.action_back)
                scaleType = ImageView.ScaleType.CENTER
                setPadding(dp(16), dp(16), dp(16), dp(16))
                setOnClickListener { onBack() }
            }
            ViewCompat.setTooltipText(pageBackButton, activity.getString(R.string.action_back))
            addView(
                pageBackButton,
                LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.MATCH_PARENT)
            )

            val titleView = TextView(activity).apply {
                text = title
                setTextColor(ContextCompat.getColor(activity, R.color.browser_text))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER_VERTICAL
                includeFontPadding = false
            }
            addView(
                titleView,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            )
        }
    }

    private fun createSheetToolbar(
        title: String,
        onBack: (() -> Unit)?,
        onClose: () -> Unit
    ): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(if (onBack == null) dp(18) else dp(4), 0, dp(4), 0)

            if (onBack != null) {
                val backButton = ImageButton(activity).apply {
                    setImageResource(R.drawable.ic_arrow_back_24)
                    setColorFilter(ContextCompat.getColor(activity, R.color.browser_icon))
                    background = ContextCompat.getDrawable(activity, R.drawable.bg_icon_button)
                    contentDescription = activity.getString(R.string.action_back)
                    scaleType = ImageView.ScaleType.CENTER
                    setPadding(dp(16), dp(16), dp(16), dp(16))
                    setOnClickListener { onBack() }
                }
                ViewCompat.setTooltipText(backButton, activity.getString(R.string.action_back))
                addView(
                    backButton,
                    LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.MATCH_PARENT)
                )
            }

            val titleView = TextView(activity).apply {
                text = title
                setTextColor(ContextCompat.getColor(activity, R.color.browser_text))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER_VERTICAL
                includeFontPadding = false
            }
            addView(
                titleView,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            )

            val closeButton = ImageButton(activity).apply {
                setImageResource(R.drawable.ic_close_24)
                setColorFilter(ContextCompat.getColor(activity, R.color.browser_icon))
                background = ContextCompat.getDrawable(activity, R.drawable.bg_icon_button)
                contentDescription = activity.getString(R.string.action_close)
                scaleType = ImageView.ScaleType.CENTER
                setPadding(dp(16), dp(16), dp(16), dp(16))
                setOnClickListener { onClose() }
            }
            ViewCompat.setTooltipText(closeButton, activity.getString(R.string.action_close))
            addView(
                closeButton,
                LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }
    }

    private fun createGridActionView(action: FunctionCenterGridAction): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            minimumHeight = dp(72)
            setPadding(dp(4), dp(8), dp(4), dp(6))
            isClickable = action.enabled
            isFocusable = action.enabled
            isEnabled = action.enabled
            alpha = if (action.enabled) 1f else 0.48f
            setBoundedSelectableItemBackground()
            if (action.enabled) {
                setOnClickListener { action.onClick() }
            }

            addView(
                ImageView(activity).apply {
                    setImageResource(action.iconResId)
                    setColorFilter(ContextCompat.getColor(activity, R.color.browser_icon))
                    setPadding(dp(6), dp(6), dp(6), dp(6))
                },
                LinearLayout.LayoutParams(dp(34), dp(34))
            )
            addView(
                TextView(activity).apply {
                    text = action.title
                    setTextColor(ContextCompat.getColor(activity, R.color.browser_text))
                    textSize = 12f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(3)
                }
            )
        }
    }

    private fun createSheetHandle(): View {
        return FrameLayout(activity).apply {
            addView(
                View(activity).apply {
                    background = createRoundedBackground(
                        ContextCompat.getColor(activity, R.color.browser_control_pressed)
                    )
                },
                FrameLayout.LayoutParams(dp(36), dp(4)).apply {
                    gravity = Gravity.CENTER
                }
            )
        }
    }

    private fun addSectionTitle(parent: LinearLayout, title: String) {
        parent.addView(
            TextView(activity).apply {
                text = title
                setTextColor(ContextCompat.getColor(activity, R.color.browser_text_hint))
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(18)
                bottomMargin = dp(8)
                marginStart = dp(4)
                marginEnd = dp(4)
            }
        )
    }

    private fun createRoundedBackground(color: Int, radius: Float = dp(8).toFloat()): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
    }

    private fun createBottomSheetBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            setCornerRadii(
                floatArrayOf(
                    dp(18).toFloat(),
                    dp(18).toFloat(),
                    dp(18).toFloat(),
                    dp(18).toFloat(),
                    0f,
                    0f,
                    0f,
                    0f
                )
            )
        }
    }

    private fun createRowText(title: String, summary: String): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            val titleView = TextView(activity).apply {
                text = title
                setTextColor(ContextCompat.getColor(activity, R.color.browser_text))
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
            val summaryView = TextView(activity).apply {
                text = summary
                setTextColor(ContextCompat.getColor(activity, R.color.browser_text_hint))
                textSize = 12f
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
            }
            addView(titleView)
            addView(summaryView)
        }
    }

    private fun createHistoryPreviewRow(
        page: SavedPage,
        onOpenPage: (SavedPage) -> Unit
    ): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setPadding(0, dp(10), 0, dp(8))
            setBoundedSelectableItemBackground()
            setOnClickListener { onOpenPage(page) }

            addView(
                ImageView(activity).apply {
                    setImageResource(R.drawable.ic_history_24)
                    setColorFilter(ContextCompat.getColor(activity, R.color.browser_primary))
                    background = createRoundedBackground(
                        ContextCompat.getColor(activity, R.color.browser_soft_blue),
                        dp(10).toFloat()
                    )
                    setPadding(dp(8), dp(8), dp(8), dp(8))
                },
                LinearLayout.LayoutParams(dp(42), dp(42))
            )
            addView(
                createRowText(page.title.ifBlank { page.url }, page.url),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(10)
                }
            )
            addView(
                TextView(activity).apply {
                    text = activity.getString(R.string.action_open_history_item)
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    setTextColor(ContextCompat.getColor(activity, R.color.browser_primary))
                    textSize = 12f
                    background = createRoundedBackground(
                        ContextCompat.getColor(activity, R.color.browser_background),
                        dp(14).toFloat()
                    )
                    setPadding(dp(12), 0, dp(12), 0)
                },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(28))
            )
        }
    }

    private fun View.setBoundedSelectableItemBackground() {
        val outValue = TypedValue()
        activity.theme.resolveAttribute(
            android.R.attr.selectableItemBackground,
            outValue,
            true
        )
        setBackgroundResource(outValue.resourceId)
    }
}
