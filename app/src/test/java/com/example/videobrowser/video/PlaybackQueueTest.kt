package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
