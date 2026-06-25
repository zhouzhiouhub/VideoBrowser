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
import com.example.videobrowser.utils.BrowserDrawableFactory
import com.example.videobrowser.utils.DensityPixelConverter
import com.example.videobrowser.utils.setBoundedSelectableItemBackground

internal class SavedPageHistoryHeaderSection(
    private val host: FunctionCenterPageHost
) {
    private val activity = host.activity

    fun add(
        parent: LinearLayout,
        state: SavedPageHistoryPageState,
        actions: SavedPageHistoryPageActions
    ) {
        addCollectionSwitch(parent, actions)
        addSearchField(parent, state.query, actions)
        addCategoryTabs(parent, state.selectedCategory, actions)
    }

    fun addFilterChip(parent: LinearLayout) {
        parent.addView(
            TextView(activity).apply {
                text = activity.getString(R.string.history_filter_all)
                includeFontPadding = false
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(ContextCompat.getColor(activity, R.color.browser_provider_selected_stroke))
                textSize = 14f
                background = BrowserDrawableFactory.roundedBackground(
                    ContextCompat.getColor(activity, R.color.browser_provider_circle),
                    dp(8).toFloat()
                )
                setPadding(dp(14), 0, dp(14), 0)
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(34)
            ).apply {
                bottomMargin = dp(16)
            }
        )
    }

    private fun addCollectionSwitch(
        parent: LinearLayout,
        actions: SavedPageHistoryPageActions
    ) {
        parent.addView(
            LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(dp(3), dp(3), dp(3), dp(3))
                background = BrowserDrawableFactory.roundedBackground(
                    ContextCompat.getColor(activity, R.color.browser_control_pressed),
                    dp(18).toFloat()
                )
                addView(
                    createCollectionSwitchItem(
                        text = activity.getString(R.string.history_tab_bookmarks),
                        selected = false,
                        onClick = actions.onShowBookmarks
                    )
                )
                addView(
                    createCollectionSwitchItem(
                        text = activity.getString(R.string.title_history),
                        selected = true,
                        onClick = actions.onShowHistory
                    )
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(42)
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(18)
            }
        )
    }

    private fun createCollectionSwitchItem(
        text: String,
        selected: Boolean,
        onClick: () -> Unit
    ): TextView {
        return TextView(activity).apply {
            this.text = text
            gravity = Gravity.CENTER
            includeFontPadding = false
            typeface = Typeface.DEFAULT_BOLD
            textSize = 16f
            setTextColor(
                ContextCompat.getColor(
                    activity,
                    if (selected) R.color.browser_text else R.color.browser_text_hint
                )
            )
            if (selected) {
                background = BrowserDrawableFactory.roundedBackground(
                    ContextCompat.getColor(activity, R.color.browser_surface),
                    dp(16).toFloat()
                )
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
            setPadding(dp(26), 0, dp(26), 0)
        }
    }

    private fun addSearchField(
        parent: LinearLayout,
        query: String?,
        actions: SavedPageHistoryPageActions
    ) {
        parent.addView(
            TextView(activity).apply {
                text = query?.takeIf { it.isNotBlank() }
                    ?: activity.getString(R.string.history_search_hint)
                gravity = Gravity.CENTER_VERTICAL
                includeFontPadding = false
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                textSize = 16f
                setTextColor(
                    ContextCompat.getColor(
                        activity,
                        if (query.isNullOrBlank()) R.color.browser_text_hint else R.color.browser_text
                    )
                )
                setPadding(dp(18), 0, dp(18), 0)
                background = BrowserDrawableFactory.roundedBackground(
                    ContextCompat.getColor(activity, R.color.browser_control_pressed),
                    dp(10).toFloat()
                )
                isClickable = true
                isFocusable = true
                setOnClickListener { actions.onSearch() }
                setOnLongClickListener {
                    if (!query.isNullOrBlank()) {
                        actions.onClearSearch()
                        true
                    } else {
                        false
                    }
                }
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)
            ).apply {
                bottomMargin = dp(16)
            }
        )
    }

    private fun addCategoryTabs(
        parent: LinearLayout,
        selectedCategory: SavedPageHistoryCategory,
        actions: SavedPageHistoryPageActions
    ) {
        parent.addView(
            LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                SavedPageHistoryCategory.values().forEach { category ->
                    addView(
                        createCategoryTab(
                            category = category,
                            selected = category == selectedCategory,
                            onClick = { actions.onSelectCategory(category) }
                        ),
                        LinearLayout.LayoutParams(0, dp(46), 1f)
                    )
                }
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply {
                bottomMargin = dp(14)
            }
        )
    }

    private fun createCategoryTab(
        category: SavedPageHistoryCategory,
        selected: Boolean,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setBoundedSelectableItemBackground()
            setOnClickListener { onClick() }
            addView(
                TextView(activity).apply {
                    text = activity.getString(category.labelRes)
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    textSize = 16f
                    typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                    setTextColor(
                        ContextCompat.getColor(
                            activity,
                            if (selected) R.color.browser_text else R.color.browser_text_hint
                        )
                    )
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )
            addView(
                View(activity).apply {
                    background = BrowserDrawableFactory.roundedBackground(
                        ContextCompat.getColor(
                            activity,
                            if (selected) R.color.browser_text else android.R.color.transparent
                        ),
                        dp(2).toFloat()
                    )
                },
                LinearLayout.LayoutParams(dp(34), dp(4)).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            )
        }
    }

    private fun dp(value: Int): Int {
        return DensityPixelConverter.truncateDp(value, activity.resources)
    }
}
