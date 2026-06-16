package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 VideoGestureFeedbackFormatter 可以拆开理解为“Video Gesture Feedback Formatter”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

object VideoGestureFeedbackFormatter {
    fun formatSpeed(speed: Float): String {
        return if (speed == speed.toInt().toFloat()) {
            "${speed.toInt()}x"
        } else {
            "${speed}x"
        }
    }

    fun formatSeekSeconds(seconds: Int): String {
        return if (seconds > 0) {
            "+${seconds}s"
        } else {
            "${seconds}s"
        }
    }

    fun formatSeekPreview(offsetMs: Long, targetMs: Long?, durationMs: Long?): String {
        val offsetText = formatSeekOffset(offsetMs)
        return if (targetMs != null && durationMs != null && durationMs > 0L) {
            "$offsetText\n${formatTime(targetMs)} / ${formatTime(durationMs)}"
        } else if (targetMs != null) {
            "$offsetText\n${formatTime(targetMs)}"
        } else {
            offsetText
        }
    }

    private fun formatSeekOffset(offsetMs: Long): String {
        val roundedSeconds = (offsetMs / 1000.0).roundToLong()
        if (roundedSeconds != 0L || offsetMs == 0L) {
            return formatSeekSeconds(roundedSeconds.toInt())
        }

        val sign = if (offsetMs > 0L) "+" else "-"
        val seconds = abs(offsetMs) / 1000.0
        return "$sign${String.format(Locale.US, "%.1f", seconds)}s"
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0L) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }
}
