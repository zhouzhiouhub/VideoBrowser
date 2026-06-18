package com.example.videobrowser.functioncenter

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.videobrowser.R
import com.example.videobrowser.utils.BrowserDrawableFactory

internal class FunctionCenterSurfaceFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int
) {
    fun createToolbar(title: String, onBack: () -> Unit): View {
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

    fun createSheetToolbar(
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

    fun createSheetHandle(): View {
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

    fun createRoundedBackground(color: Int, radius: Float = dp(8).toFloat()): GradientDrawable {
        return BrowserDrawableFactory.roundedBackground(color, radius)
    }

    fun createBottomSheetBackground(color: Int): GradientDrawable {
        return BrowserDrawableFactory.topRoundedBackground(color, dp(18).toFloat())
    }
}
