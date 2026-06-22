package com.example.videobrowser.video

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.ViewCompat
import com.example.videobrowser.R
import com.example.videobrowser.utils.BrowserDrawableFactory

internal class FullscreenVideoExitButtonController(
    private val context: Context,
    private val exitButton: ImageButton,
    private val dp: (Int) -> Int,
    private val notifyUserInteraction: () -> Unit,
    private val exitFullscreen: () -> Unit
) {
    fun attachTo(parent: ViewGroup) {
        configureButton()
        parent.addView(
            exitButton,
            FrameLayout.LayoutParams(
                dp(EXIT_BUTTON_SIZE_DP),
                dp(EXIT_BUTTON_SIZE_DP)
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                topMargin = dp(EXIT_BUTTON_MARGIN_DP)
                leftMargin = dp(EXIT_BUTTON_MARGIN_DP)
            }
        )
    }

    private fun configureButton() {
        val label = context.getString(R.string.video_control_exit_fullscreen)
        exitButton.apply {
            background = BrowserDrawableFactory.roundedBackground(
                Color.argb(178, 0, 0, 0),
                dp(EXIT_BUTTON_CORNER_RADIUS_DP)
            )
            contentDescription = label
            isClickable = true
            isFocusable = true
            scaleType = ImageView.ScaleType.CENTER
            setColorFilter(Color.WHITE)
            setImageResource(R.drawable.ic_close_24)
            setPadding(
                dp(EXIT_BUTTON_PADDING_DP),
                dp(EXIT_BUTTON_PADDING_DP),
                dp(EXIT_BUTTON_PADDING_DP),
                dp(EXIT_BUTTON_PADDING_DP)
            )
            setOnClickListener {
                notifyUserInteraction()
                exitFullscreen()
            }
        }
        ViewCompat.setTooltipText(exitButton, label)
    }

    private companion object {
        private const val EXIT_BUTTON_SIZE_DP = 44
        private const val EXIT_BUTTON_MARGIN_DP = 14
        private const val EXIT_BUTTON_CORNER_RADIUS_DP = 20
        private const val EXIT_BUTTON_PADDING_DP = 10
    }
}
