package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 VideoSeekDragCalculator 可以拆开理解为“Video Seek Drag Calculator”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import kotlin.math.roundToLong

object VideoSeekDragCalculator {
    private const val UNKNOWN_DURATION_SEEK_SPAN_MS = 60_000L
    private const val MAX_KNOWN_DURATION_SEEK_SPAN_MS = 10L * 60L * 1000L

    fun seekSpanForDuration(durationMs: Long?): Long {
        return durationMs
            ?.takeIf { it > 0L }
            ?.coerceAtMost(MAX_KNOWN_DURATION_SEEK_SPAN_MS)
            ?: UNKNOWN_DURATION_SEEK_SPAN_MS
    }

    fun offsetForDrag(deltaX: Float, viewWidth: Int, durationMs: Long?): Long {
        if (viewWidth <= 0) return 0L

        val ratio = (deltaX / viewWidth.toFloat()).coerceIn(-1f, 1f)
        return (ratio * seekSpanForDuration(durationMs)).roundToLong()
    }

    fun targetForDrag(
        startPositionMs: Long,
        durationMs: Long?,
        deltaX: Float,
        viewWidth: Int
    ): Long {
        val target = startPositionMs + offsetForDrag(deltaX, viewWidth, durationMs)
        return durationMs
            ?.takeIf { it > 0L }
            ?.let { target.coerceIn(0L, it) }
            ?: target.coerceAtLeast(0L)
    }
}
