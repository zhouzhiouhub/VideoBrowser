package com.example.videobrowser.functioncenter

import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.utils.BrowserDrawableFactory
import com.example.videobrowser.utils.DensityPixelConverter

internal class SavedPageHistorySelectionActionStrip(
    private val host: FunctionCenterPageHost
) {
    private val activity = host.activity

    fun add(
        section: LinearLayout,
        onCopy: () -> Unit,
        onRemove: () -> Unit
    ) {
        section.addView(
            LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                setPadding(0, dp(2), 0, dp(10))
                addView(
                    createButton(
                        text = activity.getString(R.string.action_copy_link),
                        textColor = ContextCompat.getColor(activity, R.color.browser_primary),
                        onClick = onCopy
                    )
                )
                addView(
                    createButton(
                        text = activity.getString(R.string.action_remove),
                        textColor = ContextCompat.getColor(activity, R.color.site_security_insecure),
                        onClick = onRemove
                    ),
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(38)
                    ).apply {
                        marginStart = dp(8)
                    }
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
        textColor: Int,
        onClick: () -> Unit
    ): TextView {
        return TextView(activity).apply {
            this.text = text
            gravity = Gravity.CENTER
            includeFontPadding = false
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColor)
            textSize = 14f
            minHeight = dp(38)
            setPadding(dp(18), 0, dp(18), 0)
            background = BrowserDrawableFactory.roundedBackground(
                ContextCompat.getColor(activity, R.color.browser_surface),
                dp(10).toFloat(),
                strokeWidth = dp(1),
                strokeColor = ContextCompat.getColor(activity, R.color.browser_control_pressed)
            )
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
    }

    private fun dp(value: Int): Int {
        return DensityPixelConverter.truncateDp(value, activity.resources)
    }
}
