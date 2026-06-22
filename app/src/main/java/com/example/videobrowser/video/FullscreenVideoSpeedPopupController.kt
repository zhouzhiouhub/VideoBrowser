package com.example.videobrowser.video

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.example.videobrowser.utils.BrowserDrawableFactory
import kotlin.math.abs

/**
 * 初学者阅读提示：
 * 这个文件属于“全屏视频倍速弹窗模块”。
 * 文件名 FullscreenVideoSpeedPopupController 可以拆开理解为“Fullscreen Video Speed Popup Controller”，表示它只负责全屏手势层里的倍速菜单。
 * 主要职责：创建、显示和关闭倍速 PopupWindow，并把用户选择回调给手势层。
 * 阅读顺序：先看 show，再看 createSpeedRow 和 dismiss。
 */
internal class FullscreenVideoSpeedPopupController(
    private val context: Context,
    private val anchorView: View,
    private val speedOptions: List<Float>,
    private val dp: (Int) -> Int,
    private val currentPlaybackSpeed: () -> Float,
    private val notifyUserInteraction: () -> Unit,
    private val setPlaybackSpeed: (Float) -> Unit,
    private val onPlaybackSpeedSelected: (Float) -> Unit,
    private val showFeedback: (String) -> Unit
) {
    private var speedPopup: PopupWindow? = null

    fun show() {
        dismiss()
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(6), 0, dp(6))
            background = BrowserDrawableFactory.roundedBackground(
                Color.argb(235, 18, 18, 18),
                dp(8)
            )
        }

        speedOptions.forEach { speed ->
            content.addView(createSpeedRow(speed), LinearLayout.LayoutParams(dp(96), dp(38)))
        }

        speedPopup = PopupWindow(
            content,
            dp(96),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = dp(8).toFloat()
            }
        }
        speedPopup?.showAsDropDown(anchorView, 0, dp(6))
    }

    fun dismiss() {
        speedPopup?.dismiss()
        speedPopup = null
    }

    private fun createSpeedRow(speed: Float): TextView {
        val selected = abs(speed - currentPlaybackSpeed()) < 0.01f
        return TextView(context).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            minHeight = dp(38)
            text = VideoGestureFeedbackFormatter.formatSpeed(speed)
            setTextColor(if (selected) Color.WHITE else Color.LTGRAY)
            setTypeface(typeface, if (selected) Typeface.BOLD else Typeface.NORMAL)
            textSize = 14f
            setOnClickListener {
                notifyUserInteraction()
                setPlaybackSpeed(speed)
                onPlaybackSpeedSelected(speed)
                dismiss()
                showFeedback(VideoGestureFeedbackFormatter.formatSpeed(speed))
            }
        }
    }
}
