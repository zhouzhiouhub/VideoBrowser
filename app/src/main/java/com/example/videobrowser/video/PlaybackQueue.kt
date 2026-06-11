package com.example.videobrowser.video

import kotlin.random.Random

data class PlaybackQueue(
    val items: List<PlayableMediaItem>,
    val currentIndex: Int = 0,
    val repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.NONE,
    val originalItems: List<PlayableMediaItem>? = null
) {
    val isShuffled: Boolean
        get() = originalItems != null

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

    fun select(index: Int): PlaybackQueue {
        return if (index in items.indices) {
            copy(currentIndex = index)
        } else {
            this
        }
    }

    fun removeAt(index: Int): PlaybackQueue {
        if (index !in items.indices || items.size <= 1) {
            return this
        }
        val removedItem = items[index]
        val updatedItems = items.toMutableList().apply {
            removeAt(index)
        }
        val updatedIndex = when {
            index < currentIndex -> currentIndex - 1
            index == currentIndex -> currentIndex.coerceAtMost(updatedItems.lastIndex)
            else -> currentIndex
        }
        return copy(
            items = updatedItems,
            currentIndex = updatedIndex.coerceIn(0, updatedItems.lastIndex),
            originalItems = originalItems
                ?.filterNot { it == removedItem }
                ?.takeIf { updatedItems.size > 1 }
        )
    }

    fun shuffle(random: Random = Random.Default): PlaybackQueue {
        return shuffle { tail -> tail.shuffled(random) }
    }

    fun shuffle(reorderTail: (List<PlayableMediaItem>) -> List<PlayableMediaItem>): PlaybackQueue {
        if (items.size <= 1) {
            return this
        }
        val current = currentItem() ?: return this
        val originalOrder = originalItems ?: items
        val tail = items.filterIndexed { index, _ -> index != currentIndex }
        val reorderedTail = reorderTail(tail)
        val uniqueTail = reorderedTail
            .filter { item -> item in tail }
            .distinct()
        val missingTail = tail.filterNot { item -> item in uniqueTail }
        return copy(
            items = listOf(current) + uniqueTail + missingTail,
            currentIndex = 0,
            originalItems = originalOrder
        )
    }

    fun restoreOriginalOrder(): PlaybackQueue {
        val originalOrder = originalItems ?: return this
        val current = currentItem()
        val restoredIndex = current
            ?.let { originalOrder.indexOf(it) }
            ?.takeIf { it >= 0 }
            ?: currentIndex.coerceIn(0, originalOrder.lastIndex)
        return copy(
            items = originalOrder,
            currentIndex = restoredIndex,
            originalItems = null
        )
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
