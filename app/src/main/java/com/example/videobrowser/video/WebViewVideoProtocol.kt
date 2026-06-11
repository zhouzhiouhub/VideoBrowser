package com.example.videobrowser.video

import java.util.Locale

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
