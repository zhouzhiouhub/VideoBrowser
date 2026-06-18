package com.example.videobrowser.functioncenter

import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R

internal class FunctionCenterGridFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int
) {
    fun addActionGrid(
        parent: LinearLayout,
        actions: List<FunctionCenterGridAction>
    ) {
        FunctionCenterActionGridLayout.rows(actions.size).forEachIndexed { rowIndex, rowSlots ->
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
            rowSlots.forEach { actionIndex ->
                row.addView(
                    actionIndex?.let { createGridActionView(actions[it]) } ?: View(activity),
                    LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        marginStart = dp(2)
                        marginEnd = dp(2)
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
