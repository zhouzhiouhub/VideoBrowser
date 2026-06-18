package com.example.videobrowser.functioncenter

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage

internal class FunctionCenterHeaderFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int,
    private val surfaceFactory: FunctionCenterSurfaceFactory,
    private val rowFactory: FunctionCenterRowFactory
) {
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
            background = surfaceFactory.createRoundedBackground(
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
            rowFactory.createRowText(title, summary),
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

    fun addBenefitStrip(
        parent: LinearLayout,
        leftTitle: String,
        leftSummary: String,
        rightTitle: String,
        rightSummary: String
    ) {
        val strip = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = surfaceFactory.createRoundedBackground(Color.parseColor("#FFF8DF"), dp(12).toFloat())
        }
        strip.addView(
            rowFactory.createRowText(leftTitle, leftSummary),
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        strip.addView(
            View(activity).apply {
                setBackgroundColor(Color.parseColor("#F1E3B8"))
            },
            LinearLayout.LayoutParams(dp(1), dp(36)).apply {
                marginStart = dp(10)
                marginEnd = dp(10)
            }
        )
        strip.addView(
            rowFactory.createRowText(rightTitle, rightSummary),
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
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
            background = surfaceFactory.createRoundedBackground(
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
                card.addView(rowFactory.createHistoryPreviewRow(page, onOpenPage))
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
}
