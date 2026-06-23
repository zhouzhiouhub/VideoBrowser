package com.example.videobrowser.video

import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.utils.ActionListDialog
import com.example.videobrowser.utils.DialogAction
import com.example.videobrowser.utils.DialogButtonAction

internal class NativePlaybackQueueDialogController(
    private val activity: AppCompatActivity,
    private val queueProvider: () -> PlaybackQueue,
    private val wakePlayerControls: () -> Unit,
    private val onSelectQueueItem: (Int) -> Unit,
    private val onToggleShuffle: () -> Unit,
    private val onRemoveMedia: (Int) -> Unit
) {
    fun showMenu() {
        val queue = queueProvider()
        if (!queue.hasMultipleItems) {
            wakePlayerControls()
            return
        }

        wakePlayerControls()
        ActionListDialog.show(
            activity = activity,
            title = activity.getString(R.string.video_queue_title, queue.items.size),
            actions = playbackQueueActions(queue) { index -> onSelectQueueItem(index) },
            positiveButton = DialogButtonAction(shuffleActionLabel(queue)) {
                onToggleShuffle()
                showMenu()
            },
            neutralButton = DialogButtonAction(R.string.video_queue_remove) {
                showRemoveMenu()
            }
        )
    }

    private fun showRemoveMenu() {
        val queue = queueProvider()
        if (!queue.hasMultipleItems) {
            wakePlayerControls()
            return
        }

        ActionListDialog.show(
            activity = activity,
            titleRes = R.string.video_queue_remove,
            actions = playbackQueueActions(queue) { index ->
                onRemoveMedia(index)
                showMenu()
            }
        )
    }

    private fun shuffleActionLabel(queue: PlaybackQueue): Int {
        return if (queue.isShuffled) {
            R.string.video_queue_restore_order
        } else {
            R.string.video_queue_shuffle
        }
    }

    private fun playbackQueueLabels(queue: PlaybackQueue): Array<String> {
        return PlaybackQueueLabelFormatter.labels(
            queue = queue,
            nowPlayingLabel = activity.getString(R.string.video_queue_now_playing)
        ).toTypedArray()
    }

    private fun playbackQueueActions(
        queue: PlaybackQueue,
        onSelected: (Int) -> Unit
    ): List<DialogAction> {
        return playbackQueueLabels(queue).mapIndexed { index, label ->
            DialogAction(label) {
                onSelected(index)
            }
        }
    }
}
