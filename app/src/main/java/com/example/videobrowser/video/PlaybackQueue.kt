package com.example.videobrowser.video

data class PlaybackQueue(
    val items: List<PlayableMediaItem>,
    val currentIndex: Int = 0,
    val repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.NONE
) {
    fun currentItem(): PlayableMediaItem? {
        return items.getOrNull(currentIndex)
    }

    fun next(): PlaybackQueue {
        if (items.isEmpty()) {
            return this
        }
        val nextIndex = currentIndex + 1
        return when {
            nextIndex in items.indices -> copy(currentIndex = nextIndex)
            repeatMode == PlaybackRepeatMode.ALL -> copy(currentIndex = 0)
            else -> this
        }
    }

    fun previous(): PlaybackQueue {
        if (items.isEmpty()) {
            return this
        }
        val previousIndex = currentIndex - 1
        return when {
            previousIndex in items.indices -> copy(currentIndex = previousIndex)
            repeatMode == PlaybackRepeatMode.ALL -> copy(currentIndex = items.lastIndex)
            else -> this
        }
    }

    companion object {
        fun single(item: PlayableMediaItem): PlaybackQueue {
            return PlaybackQueue(items = listOf(item))
        }
    }
}

enum class PlaybackRepeatMode {
    NONE,
    ONE,
    ALL
}
