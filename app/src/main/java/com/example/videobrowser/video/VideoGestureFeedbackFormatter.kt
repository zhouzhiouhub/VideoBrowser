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
import kotlin.math.roundToInt
import kotlin.math.roundToLong

object VideoGestureFeedbackFormatter {
    /**
     * 函数 `formatSpeed`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param speed 参数类型为 `Float`，表示函数执行 `speed` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun formatSpeed(speed: Float): String {
        return if (speed == speed.toInt().toFloat()) {
            "${speed.toInt()}x"
        } else {
            "${speed}x"
        }
    }

    /**
     * 函数 `formatSeekSeconds`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param seconds 参数类型为 `Int`，表示函数执行 `seconds` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun formatSeekSeconds(seconds: Int): String {
        return if (seconds > 0) {
            "+${seconds}s"
        } else {
            "${seconds}s"
        }
    }

    fun formatBrightness(brightness: Float): String {
        return "$BRIGHTNESS_ICON ${(brightness.coerceIn(0f, 1f) * 100).roundToInt()}%"
    }

    fun formatVolume(volume: Int, minVolume: Int, maxVolume: Int): String {
        return "$VOLUME_ICON ${FullscreenVideoGestureMath.volumePercent(volume, minVolume, maxVolume)}%"
    }

    /**
     * 函数 `formatSeekPreview`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param offsetMs 参数类型为 `Long`，表示函数执行 `offsetMs` 相关逻辑时需要读取或处理的输入。
     * @param targetMs 参数类型为 `Long?`，表示函数执行 `targetMs` 相关逻辑时需要读取或处理的输入。
     * @param durationMs 参数类型为 `Long?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `formatSeekOffset`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param offsetMs 参数类型为 `Long`，表示函数执行 `offsetMs` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun formatSeekOffset(offsetMs: Long): String {
        val roundedSeconds = (offsetMs / 1000.0).roundToLong()
        if (roundedSeconds != 0L || offsetMs == 0L) {
            return formatSeekSeconds(roundedSeconds.toInt())
        }

        val sign = if (offsetMs > 0L) "+" else "-"
        val seconds = abs(offsetMs) / 1000.0
        return "$sign${String.format(Locale.US, "%.1f", seconds)}s"
    }

    /**
     * 函数 `formatTime`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param timeMs 参数类型为 `Long`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    private const val BRIGHTNESS_ICON = "\u2600"
    private const val VOLUME_ICON = "\ud83d\udd0a"
}
