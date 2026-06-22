package com.example.videobrowser.functioncenter

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R

internal class FunctionCenterContentFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int,
    private val surfaceFactory: FunctionCenterSurfaceFactory,
    private val rowFactory: FunctionCenterRowFactory
) {
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
            background = surfaceFactory.createRoundedBackground(
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
        val row = rowFactory.createRowText(title, summary).apply {
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
                background = surfaceFactory.createRoundedBackground(
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
                background = surfaceFactory.createRoundedBackground(
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
                background = surfaceFactory.createRoundedBackground(resolvedBackgroundColor)
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

    fun addSwitchRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        checked: Boolean,
        enabled: Boolean = true,
        onChanged: (Boolean) -> Unit
    ) {
        parent.addView(
            rowFactory.createSwitchRow(
                title = title,
                summary = summary,
                checked = checked,
                enabled = enabled,
                onChanged = onChanged
            ),
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
        parent.addView(
            rowFactory.createActionRow(
                title = title,
                summary = summary,
                enabled = enabled,
                onClick = onClick
            ),
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
}
