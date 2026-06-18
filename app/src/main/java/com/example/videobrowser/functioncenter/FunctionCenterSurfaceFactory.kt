package com.example.videobrowser.functioncenter

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.utils.BrowserDrawableFactory

internal class FunctionCenterSurfaceFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int
) {
    private val toolbarButtonFactory = FunctionCenterToolbarButtonFactory(activity, dp)

    fun createToolbar(title: String, onBack: () -> Unit): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            elevation = dp(4).toFloat()
            setPadding(dp(4), 0, dp(12), 0)
            setBackgroundColor(ContextCompat.getColor(activity, R.color.browser_surface))

            val pageBackButton = toolbarButtonFactory.createButton(
                iconRes = R.drawable.ic_arrow_back_24,
                labelRes = R.string.action_back,
                onClick = onBack
            )
            addView(
                pageBackButton,
                toolbarButtonFactory.layoutParams()
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
                val backButton = toolbarButtonFactory.createButton(
                    iconRes = R.drawable.ic_arrow_back_24,
                    labelRes = R.string.action_back,
                    onClick = onBack
                )
                addView(
                    backButton,
                    toolbarButtonFactory.layoutParams()
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

            val closeButton = toolbarButtonFactory.createButton(
                iconRes = R.drawable.ic_close_24,
                labelRes = R.string.action_close,
                onClick = onClose
            )
            addView(
                closeButton,
                toolbarButtonFactory.layoutParams()
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
