package com.example.videobrowser.functioncenter

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.utils.DensityPixelConverter
import com.example.videobrowser.utils.setBoundedSelectableItemBackground

internal class SavedPageHistorySelectionActionStrip(
    private val host: FunctionCenterPageHost
) {
    private val activity = host.activity

    fun add(
        section: LinearLayout,
        onSelectAll: () -> Unit,
        onDelete: () -> Unit,
        onDone: () -> Unit
    ) {
        section.addView(
            View(activity).apply {
                setBackgroundColor(ContextCompat.getColor(activity, R.color.browser_control_pressed))
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1))
        )
        section.addView(
            LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, dp(8))
                setBackgroundColor(ContextCompat.getColor(activity, R.color.browser_surface))
                addView(
                    createButton(
                        text = activity.getString(R.string.action_select_all),
                        onClick = onSelectAll
                    ),
                    buttonLayoutParams()
                )
                addView(
                    createButton(
                        text = activity.getString(R.string.action_delete),
                        onClick = onDelete
                    ),
                    buttonLayoutParams()
                )
                addView(
                    createButton(
                        text = activity.getString(R.string.action_done),
                        onClick = onDone
                    ),
                    buttonLayoutParams()
                )
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createButton(
        text: String,
        onClick: () -> Unit
    ): TextView {
        return TextView(activity).apply {
            this.text = text
            gravity = Gravity.CENTER
            includeFontPadding = false
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ContextCompat.getColor(activity, R.color.browser_text))
            textSize = 15f
            minHeight = dp(48)
            setBoundedSelectableItemBackground()
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
    }

    private fun buttonLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
    }

    private fun dp(value: Int): Int {
        return DensityPixelConverter.truncateDp(value, activity.resources)
    }
}
