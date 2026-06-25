package com.example.videobrowser.functioncenter

import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.utils.setBoundedSelectableItemBackground

internal class FunctionCenterRowFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int,
    private val surfaceFactory: FunctionCenterSurfaceFactory
) {
    fun createRowText(title: String, summary: String): LinearLayout {
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
            if (summary.isNotBlank()) {
                addView(summaryView)
            }
        }
    }

    fun createSwitchRow(
        title: String,
        summary: String,
        checked: Boolean,
        enabled: Boolean,
        onChanged: (Boolean) -> Unit
    ): View {
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
        return row
    }

    fun createActionRow(
        title: String,
        summary: String,
        enabled: Boolean,
        onLongClick: (() -> Unit)? = null,
        onClick: () -> Unit
    ): View {
        return createRowText(title, summary).apply {
            isClickable = enabled
            isFocusable = enabled
            isEnabled = enabled
            alpha = if (enabled) 1f else 0.48f
            minimumHeight = dp(58)
            setPadding(0, dp(9), 0, dp(9))
            setBoundedSelectableItemBackground()
            if (enabled) {
                setOnClickListener { onClick() }
                onLongClick?.let { handler ->
                    setOnLongClickListener {
                        handler()
                        true
                    }
                }
            }
        }
    }

    fun createHistoryPreviewRow(
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
                    background = surfaceFactory.createRoundedBackground(
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
                    background = surfaceFactory.createRoundedBackground(
                        ContextCompat.getColor(activity, R.color.browser_background),
                        dp(14).toFloat()
                    )
                    setPadding(dp(12), 0, dp(12), 0)
                },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(28))
            )
        }
    }

}
