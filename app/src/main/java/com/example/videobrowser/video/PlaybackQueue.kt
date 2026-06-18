package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 PlaybackQueue 可以拆开理解为“Playback Queue”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import kotlin.random.Random

/**
 * 原生播放器播放队列。
 *
 * 这个类是不可变数据模型：每次上一集、下一集、删除、随机播放都会返回新的 PlaybackQueue。
 * 不可变模型更容易测试，也避免 UI 和播放器同时修改同一个列表。
 */
data class PlaybackQueue(
    val items: List<PlayableMediaItem>,
    val currentIndex: Int = 0,
    val repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.NONE,
    val originalItems: List<PlayableMediaItem>? = null
) {
    val isShuffled: Boolean
        get() = originalItems != null

    /**
     * 函数 `currentItem`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun currentItem(): PlayableMediaItem? {
        return items.getOrNull(currentIndex)
    }

    /**
     * 函数 `next`：封装 `next` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun next(): PlaybackQueue {
        // repeatMode.ALL 会在最后一项之后回到第一项；NONE 则停在当前队列末尾。
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

    /**
     * 函数 `previous`：封装 `previous` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `select`：封装 `select` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param index 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun select(index: Int): PlaybackQueue {
        return if (index in items.indices) {
            copy(currentIndex = index)
        } else {
            this
        }
    }

    /**
     * 函数 `removeAt`：封装 `remove At` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param index 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `shuffle`：封装 `shuffle` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param random 参数类型为 `Random`，表示函数执行 `random` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun shuffle(random: Random = Random.Default): PlaybackQueue {
        return shuffle { tail -> tail.shuffled(random) }
    }

    /**
     * 函数 `shuffle`：封装 `shuffle` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param reorderTail 参数类型为 `(List<PlayableMediaItem>) -> List<PlayableMediaItem>`，表示函数执行 `reorderTail` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun shuffle(reorderTail: (List<PlayableMediaItem>) -> List<PlayableMediaItem>): PlaybackQueue {
        // 当前正在播放的项目固定在第一位，只打乱后面的队列，避免用户开启随机后立刻跳走。
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

    fun toggleShuffle(random: Random = Random.Default): PlaybackQueue {
        return if (isShuffled) {
            restoreOriginalOrder()
        } else {
            shuffle(random)
        }
    }

    fun toggleShuffle(reorderTail: (List<PlayableMediaItem>) -> List<PlayableMediaItem>): PlaybackQueue {
        return if (isShuffled) {
            restoreOriginalOrder()
        } else {
            shuffle(reorderTail)
        }
    }

    /**
     * 函数 `restoreOriginalOrder`：封装 `restore Original Order` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
        /**
         * 函数 `single`：封装 `single` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param item 参数类型为 `PlayableMediaItem`，表示函数执行 `item` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun single(item: PlayableMediaItem): PlaybackQueue {
            return PlaybackQueue(items = listOf(item))
        }
    }
}

enum class PlaybackRepeatMode {
    NONE,
    ONE,
    ALL;

    fun next(): PlaybackRepeatMode {
        return when (this) {
            NONE -> ONE
            ONE -> ALL
            ALL -> NONE
        }
    }
}
