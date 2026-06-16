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

    /**
     * 函数 `seekSpanForDuration`：封装 `seek Span For Duration` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param durationMs 参数类型为 `Long?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun seekSpanForDuration(durationMs: Long?): Long {
        return durationMs
            ?.takeIf { it > 0L }
            ?.coerceAtMost(MAX_KNOWN_DURATION_SEEK_SPAN_MS)
            ?: UNKNOWN_DURATION_SEEK_SPAN_MS
    }

    /**
     * 函数 `offsetForDrag`：封装 `offset For Drag` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param deltaX 参数类型为 `Float`，表示函数执行 `deltaX` 相关逻辑时需要读取或处理的输入。
     * @param viewWidth 参数类型为 `Int`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param durationMs 参数类型为 `Long?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun offsetForDrag(deltaX: Float, viewWidth: Int, durationMs: Long?): Long {
        if (viewWidth <= 0) return 0L

        val ratio = (deltaX / viewWidth.toFloat()).coerceIn(-1f, 1f)
        return (ratio * seekSpanForDuration(durationMs)).roundToLong()
    }

    /**
     * 函数 `targetForDrag`：封装 `target For Drag` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param startPositionMs 参数类型为 `Long`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param durationMs 参数类型为 `Long?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param deltaX 参数类型为 `Float`，表示函数执行 `deltaX` 相关逻辑时需要读取或处理的输入。
     * @param viewWidth 参数类型为 `Int`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
