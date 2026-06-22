package com.example.videobrowser.video

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.example.videobrowser.utils.BrowserDrawableFactory

internal class FullscreenVideoFeedbackController(
    context: Context,
    private val feedbackHandler: Handler,
    private val dp: (Int) -> Int
) {
    val view: TextView = TextView(context).apply {
        visibility = View.GONE
        gravity = Gravity.CENTER
        includeFontPadding = false
        minHeight = dp(46)
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        setTextColor(Color.WHITE)
        setTypeface(typeface, Typeface.BOLD)
        textSize = 18f
        setPadding(dp(20), dp(8), dp(20), dp(8))
        background = BrowserDrawableFactory.roundedBackground(
            Color.argb(196, 0, 0, 0),
            dp(20)
        )
    }

    private val hideRunnable = Runnable {
        view.visibility = View.GONE
    }

    fun show(text: String, autoHide: Boolean = true) {
        view.text = text
        view.visibility = View.VISIBLE
        view.bringToFront()
        feedbackHandler.removeCallbacks(hideRunnable)
        if (autoHide) {
            feedbackHandler.postDelayed(hideRunnable, FEEDBACK_DURATION_MS)
        }
    }

    fun hide() {
        feedbackHandler.removeCallbacks(hideRunnable)
        view.visibility = View.GONE
    }

    fun currentText(): String {
        return view.text?.toString().orEmpty()
    }

    private companion object {
        private const val FEEDBACK_DURATION_MS = 900L
    }
}
