package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueTest {
    @Test
    fun singleCreatesQueueWithCurrentItem() {
        val item = playable("https://cdn.example.com/one.mp4")

        val queue = PlaybackQueue.single(item)

        assertEquals(item, queue.currentItem())
        assertEquals(0, queue.currentIndex)
        assertEquals(listOf(item), queue.items)
    }

    @Test
    fun nextAndPreviousMoveWithinBoundsWithoutRepeat() {
        val first = playable("https://cdn.example.com/one.mp4")
        val second = playable("https://cdn.example.com/two.mp4")
        val queue = PlaybackQueue(items = listOf(first, second))

        assertEquals(second, queue.next().currentItem())
        assertEquals(second, queue.next().next().currentItem())
        assertEquals(first, queue.next().previous().currentItem())
        assertEquals(first, queue.previous().currentItem())
    }

    @Test
    fun repeatAllWrapsManualNextAndPrevious() {
        val first = playable("https://cdn.example.com/one.mp4")
        val second = playable("https://cdn.example.com/two.mp4")
        val queue = PlaybackQueue(
            items = listOf(first, second),
            repeatMode = PlaybackRepeatMode.ALL
        )

        assertEquals(first, queue.next().next().currentItem())
        assertEquals(second, queue.previous().currentItem())
    }

    @Test
    fun selectMovesToValidIndexAndIgnoresInvalidIndex() {
        val first = playable("https://cdn.example.com/one.mp4")
        val second = playable("https://cdn.example.com/two.mp4")
        val third = playable("https://cdn.example.com/three.mp4")
        val queue = PlaybackQueue(items = listOf(first, second, third))

        assertEquals(third, queue.select(2).currentItem())
        assertEquals(first, queue.select(-1).currentItem())
        assertEquals(first, queue.select(3).currentItem())
    }

    @Test
    fun removeBeforeCurrentKeepsSameMediaSelected() {
        val first = playable("https://cdn.example.com/one.mp4")
        val second = playable("https://cdn.example.com/two.mp4")
        val third = playable("https://cdn.example.com/three.mp4")
        val queue = PlaybackQueue(
            items = listOf(first, second, third),
            currentIndex = 2
        )

        val updated = queue.removeAt(0)

        assertEquals(listOf(second, third), updated.items)
        assertEquals(third, updated.currentItem())
        assertEquals(1, updated.currentIndex)
    }

    @Test
    fun removeCurrentSelectsNextItemOrPreviousWhenAtEnd() {
        val first = playable("https://cdn.example.com/one.mp4")
        val second = playable("https://cdn.example.com/two.mp4")
        val third = playable("https://cdn.example.com/three.mp4")

        val middleRemoved = PlaybackQueue(
            items = listOf(first, second, third),
            currentIndex = 1
        ).removeAt(1)
        val lastRemoved = PlaybackQueue(
            items = listOf(first, second, third),
            currentIndex = 2
        ).removeAt(2)

        assertEquals(listOf(first, third), middleRemoved.items)
        assertEquals(third, middleRemoved.currentItem())
        assertEquals(1, middleRemoved.currentIndex)
        assertEquals(listOf(first, second), lastRemoved.items)
        assertEquals(second, lastRemoved.currentItem())
        assertEquals(1, lastRemoved.currentIndex)
    }

    @Test
    fun removeAtIgnoresInvalidIndexAndDoesNotRemoveLastItem() {
        val item = playable("https://cdn.example.com/one.mp4")
        val queue = PlaybackQueue.single(item)

        assertEquals(queue, queue.removeAt(-1))
        assertEquals(queue, queue.removeAt(1))
        assertEquals(queue, queue.removeAt(0))
    }

    @Test
    fun shuffleKeepsCurrentItemFirstAndCanRestoreOriginalOrder() {
        val first = playable("https://cdn.example.com/one.mp4")
        val second = playable("https://cdn.example.com/two.mp4")
        val third = playable("https://cdn.example.com/three.mp4")
        val fourth = playable("https://cdn.example.com/four.mp4")
        val queue = PlaybackQueue(
            items = listOf(first, second, third, fourth),
            currentIndex = 2
        )

        val shuffled = queue.shuffle { it.asReversed() }
        val restored = shuffled.restoreOriginalOrder()

        assertTrue(shuffled.isShuffled)
        assertEquals(listOf(third, fourth, second, first), shuffled.items)
        assertEquals(0, shuffled.currentIndex)
        assertEquals(third, shuffled.currentItem())
        assertFalse(restored.isShuffled)
        assertEquals(listOf(first, second, third, fourth), restored.items)
        assertEquals(2, restored.currentIndex)
        assertEquals(third, restored.currentItem())
    }

    @Test
    fun restoreOriginalOrderKeepsUnshuffledQueueUnchanged() {
        val first = playable("https://cdn.example.com/one.mp4")
        val second = playable("https://cdn.example.com/two.mp4")
        val queue = PlaybackQueue(items = listOf(first, second), currentIndex = 1)

        assertEquals(queue, queue.restoreOriginalOrder())
    }

    @Test
    fun removeFromShuffledQueueAlsoRemovesFromOriginalOrder() {
        val first = playable("https://cdn.example.com/one.mp4")
        val second = playable("https://cdn.example.com/two.mp4")
        val third = playable("https://cdn.example.com/three.mp4")
        val fourth = playable("https://cdn.example.com/four.mp4")
        val queue = PlaybackQueue(
            items = listOf(first, second, third, fourth),
            currentIndex = 2
        ).shuffle { it.asReversed() }

        val updated = queue.removeAt(1).restoreOriginalOrder()

        assertEquals(listOf(first, second, third), updated.items)
        assertEquals(2, updated.currentIndex)
        assertEquals(third, updated.currentItem())
    }

    @Test
    fun invalidCurrentIndexProducesEmptyCurrentItem() {
        val queue = PlaybackQueue(
            items = listOf(playable("https://cdn.example.com/one.mp4")),
            currentIndex = 5
        )

        assertNull(queue.currentItem())
    }

    private fun playable(uri: String): PlayableMediaItem {
        return PlayableMediaItem(
            uri = uri,
            title = uri.substringAfterLast('/'),
            source = PlayableMediaSource.REMOTE_URL
        )
    }
}
