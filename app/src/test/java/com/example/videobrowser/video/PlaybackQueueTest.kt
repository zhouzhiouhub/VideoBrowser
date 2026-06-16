package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Playback Queue Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueTest {
    /**
     * 测试函数 `singleCreatesQueueWithCurrentItem`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `single Creates Queue With Current Item` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun singleCreatesQueueWithCurrentItem() {
        val item = playable("https://cdn.example.com/one.mp4")

        val queue = PlaybackQueue.single(item)

        assertEquals(item, queue.currentItem())
        assertEquals(0, queue.currentIndex)
        assertEquals(listOf(item), queue.items)
    }

    /**
     * 测试函数 `nextAndPreviousMoveWithinBoundsWithoutRepeat`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `next And Previous Move Within Bounds Without Repeat` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `repeatAllWrapsManualNextAndPrevious`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `repeat All Wraps Manual Next And Previous` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `selectMovesToValidIndexAndIgnoresInvalidIndex`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `select Moves To Valid Index And Ignores Invalid Index` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `removeBeforeCurrentKeepsSameMediaSelected`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove Before Current Keeps Same Media Selected` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `removeCurrentSelectsNextItemOrPreviousWhenAtEnd`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove Current Selects Next Item Or Previous When At End` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `removeAtIgnoresInvalidIndexAndDoesNotRemoveLastItem`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove At Ignores Invalid Index And Does Not Remove Last Item` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun removeAtIgnoresInvalidIndexAndDoesNotRemoveLastItem() {
        val item = playable("https://cdn.example.com/one.mp4")
        val queue = PlaybackQueue.single(item)

        assertEquals(queue, queue.removeAt(-1))
        assertEquals(queue, queue.removeAt(1))
        assertEquals(queue, queue.removeAt(0))
    }

    /**
     * 测试函数 `shuffleKeepsCurrentItemFirstAndCanRestoreOriginalOrder`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `shuffle Keeps Current Item First And Can Restore Original Order` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `restoreOriginalOrderKeepsUnshuffledQueueUnchanged`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `restore Original Order Keeps Unshuffled Queue Unchanged` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun restoreOriginalOrderKeepsUnshuffledQueueUnchanged() {
        val first = playable("https://cdn.example.com/one.mp4")
        val second = playable("https://cdn.example.com/two.mp4")
        val queue = PlaybackQueue(items = listOf(first, second), currentIndex = 1)

        assertEquals(queue, queue.restoreOriginalOrder())
    }

    /**
     * 测试函数 `removeFromShuffledQueueAlsoRemovesFromOriginalOrder`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove From Shuffled Queue Also Removes From Original Order` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `invalidCurrentIndexProducesEmptyCurrentItem`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `invalid Current Index Produces Empty Current Item` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun invalidCurrentIndexProducesEmptyCurrentItem() {
        val queue = PlaybackQueue(
            items = listOf(playable("https://cdn.example.com/one.mp4")),
            currentIndex = 5
        )

        assertNull(queue.currentItem())
    }

    /**
     * 测试函数 `playable`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `playable` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param uri 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun playable(uri: String): PlayableMediaItem {
        return PlayableMediaItem(
            uri = uri,
            title = uri.substringAfterLast('/'),
            source = PlayableMediaSource.REMOTE_URL
        )
    }
}
