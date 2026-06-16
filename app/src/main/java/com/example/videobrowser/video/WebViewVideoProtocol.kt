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

    private fun enhancerCall(functionName: String, vararg arguments: String): String {
        return "(function(){var enhancer=window.VideoBrowserEnhancer;" +
            "if(!enhancer)return;" +
            "if(typeof enhancer.$functionName==='function'){" +
            "enhancer.$functionName(${arguments.joinToString()});" +
            "}})();"
    }

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

    private fun secondsArgument(durationMs: Long): String {
        return String.format(Locale.US, "%.3f", durationMs / 1000.0)
    }

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
