package com.example.videobrowser.video

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.example.videobrowser.R

/**
 * 初学者阅读提示：
 * 这个文件属于“全屏视频锁定 UI 模块”。
 * 文件名 FullscreenVideoLockUiController 可以拆开理解为“Fullscreen Video Lock UI Controller”，表示它只负责手势层锁定按钮和控制组显隐。
 * 主要职责：维护锁定状态、更新锁定按钮图标/描述、隐藏控制组，并在锁定时通知外层清理手势和关闭倍速弹窗。
 * 阅读顺序：先看 setLocked，再看 update。
 */
internal class FullscreenVideoLockUiController(
    private val context: Context,
    private val lockButton: TextView,
    private val controlsGroup: View,
    private val dismissSpeedPopup: () -> Unit,
    private val clearGestureStateWhenLocked: () -> Unit,
    private val showFeedback: (String) -> Unit
) {
    var locked: Boolean = false
        private set

    fun toggle(announce: Boolean) {
        setLocked(!locked, announce)
    }

    fun setLocked(value: Boolean, announce: Boolean) {
        if (locked == value && announce) {
            showFeedback(feedbackIcon())
            return
        }
        locked = value
        if (locked) {
            clearGestureStateWhenLocked()
        }
        update()
        if (announce) {
            showFeedback(feedbackIcon())
        }
    }

    fun update() {
        val controlLabel = context.getString(
            if (locked) R.string.video_control_unlock else R.string.video_control_lock
        )
        lockButton.text = if (locked) UNLOCKED_ICON else LOCKED_ICON
        lockButton.contentDescription = controlLabel
        ViewCompat.setTooltipText(lockButton, controlLabel)
        controlsGroup.visibility = if (locked) View.GONE else View.VISIBLE
        if (locked) {
            dismissSpeedPopup()
        }
    }

    private fun feedbackIcon(): String {
        return if (locked) LOCKED_ICON else UNLOCKED_ICON
    }

    private companion object {
        private const val LOCKED_ICON = "\ud83d\udd12"
        private const val UNLOCKED_ICON = "\ud83d\udd13"
    }
}
