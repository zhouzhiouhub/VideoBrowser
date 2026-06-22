package com.example.videobrowser.video

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R

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
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.video_queue_title, queue.items.size))
            .setItems(playbackQueueLabels(queue)) { _, index ->
                onSelectQueueItem(index)
            }
            .setPositiveButton(shuffleActionLabel(queue)) { _, _ ->
                onToggleShuffle()
                showMenu()
            }
            .setNeutralButton(R.string.video_queue_remove) { _, _ ->
                showRemoveMenu()
            }
            .show()
    }

    private fun showRemoveMenu() {
        val queue = queueProvider()
        if (!queue.hasMultipleItems) {
            wakePlayerControls()
            return
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.video_queue_remove)
            .setItems(playbackQueueLabels(queue)) { _, index ->
                onRemoveMedia(index)
                showMenu()
            }
            .show()
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
}
