package com.example.videobrowser.video

object PlaybackQueueLabelFormatter {
    fun labels(queue: PlaybackQueue, nowPlayingLabel: String): List<String> {
        return queue.items.mapIndexed { index, item ->
            val title = item.title
                ?.takeIf { it.isNotBlank() }
                ?: item.uri.substringAfterLast('/').ifBlank { item.uri }
            val currentLabel = if (index == queue.currentIndex) {
                " - $nowPlayingLabel"
            } else {
                ""
            }
            "${index + 1}. $title$currentLabel"
        }
    }
}
