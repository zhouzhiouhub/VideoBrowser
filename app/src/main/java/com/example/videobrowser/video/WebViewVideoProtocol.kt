package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 WebViewVideoProtocol 可以拆开理解为“Web View Video Protocol”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import java.util.Locale

/**
 * 网页播放器时间轴。
 *
 * JavaScript 通过原生桥传回 Double，这里把非法值过滤掉，再转换成 Kotlin 侧更好处理的 Long 毫秒。
 */
data class WebViewVideoTimeline(
    val positionMs: Long?,
    val durationMs: Long?
) {
    companion object {
        /**
         * 函数 `fromBridge`：封装 `from Bridge` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param positionMs 参数类型为 `Double`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @param durationMs 参数类型为 `Double`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun fromBridge(positionMs: Double, durationMs: Double): WebViewVideoTimeline {
            return WebViewVideoTimeline(
                positionMs = positionMs
                    .takeIf { it.isFinite() && it >= 0.0 }
                    ?.toLong(),
                durationMs = durationMs
                    .takeIf { it.isFinite() && it > 0.0 }
                    ?.toLong()
            )
        }
    }
}

sealed class WebViewVideoCommand {
    // sealed class 让所有网页视频命令集中在一个类型里，when 分支能覆盖完整命令集合。
    object WakeControls : WebViewVideoCommand()
    object RequestTimeline : WebViewVideoCommand()
    object TogglePlayPause : WebViewVideoCommand()
    object ExitFullscreen : WebViewVideoCommand()
    data class SeekBy(val offsetMs: Long) : WebViewVideoCommand()
    data class SeekTo(val positionMs: Long) : WebViewVideoCommand()
    data class SetPlaybackSpeed(val speed: Float) : WebViewVideoCommand()
    data class StartDirectionalPlayback(val direction: Int) : WebViewVideoCommand()
    object StopDirectionalPlayback : WebViewVideoCommand()

    /**
     * 函数 `toJavascript`：封装 `to Javascript` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun toJavascript(): String {
        // 命令只在这里转换成 JavaScript，调用方不需要知道网页增强脚本的函数名细节。
        return when (this) {
            WakeControls -> enhancerCall("wakeControls")
            RequestTimeline -> enhancerCall("reportPlaybackTimeline")
            TogglePlayPause -> togglePlayPauseScript()
            ExitFullscreen -> enhancerCall("exitFullscreen")
            is SeekBy -> enhancerCall("seekBy", secondsArgument(offsetMs))
            is SeekTo -> enhancerCall("seekTo", secondsArgument(positionMs.coerceAtLeast(0L)))
            is SetPlaybackSpeed -> enhancerCall("setPlaybackSpeed", speedArgument(speed))
            is StartDirectionalPlayback -> enhancerCall(
                "startDirectionalPlayback",
                if (direction < 0) "-1" else "1"
            )
            StopDirectionalPlayback -> enhancerCall("stopDirectionalPlayback")
        }
    }

    /**
     * 函数 `enhancerCall`：封装 `enhancer Call` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param functionName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param arguments 参数类型为 `String`，表示函数执行 `arguments` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun enhancerCall(functionName: String, vararg arguments: String): String {
        return "(function(){var enhancer=window.VideoBrowserEnhancer;" +
            "if(!enhancer)return;" +
            "if(typeof enhancer.$functionName==='function'){" +
            "enhancer.$functionName(${arguments.joinToString()});" +
            "}})();"
    }

    /**
     * 函数 `togglePlayPauseScript`：封装 `toggle Play Pause Script` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun togglePlayPauseScript(): String {
        return "(function(){var enhancer=window.VideoBrowserEnhancer;" +
            "if(!enhancer)return;" +
            "if(typeof enhancer.togglePlayPause==='function'){" +
            "enhancer.togglePlayPause();" +
            "}" +
            "if(typeof enhancer.wakeControls==='function'){" +
            "enhancer.wakeControls();" +
            "}" +
            "})();"
    }

    /**
     * 函数 `secondsArgument`：封装 `seconds Argument` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param durationMs 参数类型为 `Long`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun secondsArgument(durationMs: Long): String {
        return String.format(Locale.US, "%.3f", durationMs / 1000.0)
    }

    /**
     * 函数 `speedArgument`：封装 `speed Argument` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param speed 参数类型为 `Float`，表示函数执行 `speed` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun speedArgument(speed: Float): String {
        val normalizedSpeed = if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            DEFAULT_PLAYBACK_SPEED
        }
        return String.format(Locale.US, "%.2f", normalizedSpeed)
    }

    private companion object {
        private const val DEFAULT_PLAYBACK_SPEED = 1f
    }
}
